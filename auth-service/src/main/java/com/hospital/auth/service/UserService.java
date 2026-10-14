package com.hospital.auth.service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hospital.auth.dto.CreateUserRequest;
import com.hospital.auth.dto.UserResponse;
import com.hospital.auth.entity.User;
import com.hospital.auth.repository.UserRepository;
import com.hospital.common.enums.AccountStatus;
import com.hospital.common.enums.Role;
import com.hospital.common.exception.BadRequestException;
import com.hospital.common.exception.ConflictException;
import com.hospital.common.exception.ResourceNotFoundException;

/**
 * Business logic for login accounts: create, read, change status, deactivate and delete. Patient
 * and doctor profiles are not handled here; they belong to hospital-service.
 */
@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
      * Creates the service.
      *
      * @param userRepository database access for users
      * @param passwordEncoder BCrypt encoder
      */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
      * Creates the login of a self-registered user. The password is always hashed with BCrypt. The
      * input must already be validated.
      *
      * @param username login name
      * @param rawPassword plain password
      * @param role role of the user
      * @param status first account status
      * @return the saved user
      * @throws ConflictException if the username is already taken
      */
    public User registerAccount(
            String username, String rawPassword, Role role, AccountStatus status) {
        return saveNewUser(username, passwordEncoder.encode(rawPassword), role, status);
    }

    /**
      * Creates a login on request of an admin (for example a receptionist). If the password already
      * looks like a BCrypt hash it is stored as it is (same as the old project); otherwise it is
      * hashed.
      *
      * @param request the validated request
      * @throws BadRequestException if the role is not valid or os pateint or doctor
      * @throws ConflictException if the username is already taken
      */
    public void createUser(CreateUserRequest request) {
        Role role = parseRole(request.role());

        if (role == Role.PATIENT || role == Role.DOCTOR) {
            throw new BadRequestException(
                    "Use /auth/register/patient or /auth/register/doctor for this role.");
        }
        String password = request.password();
        if (!looksLikeBcryptHash(password)) {
            password = passwordEncoder.encode(password);
        }
        AccountStatus status = request.status() != null ? request.status() : AccountStatus.ACTIVE;
        saveNewUser(request.username(), password, role, status);
    }

    /**
      * Returns all users.
      *
      * @return all users (without passwords)
      */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    /**
      * Returns one user by login name.
      *
      * @param username the login name
      * @return the user (without password)
      * @throws ResourceNotFoundException if there is no such user
      */
    public UserResponse getUser(String username) {
        return UserResponse.from(findByUsername(username));
    }

    /**
      * Returns the users with status PENDING that have a role.
      *
      * @param role the role name, not case sensitive
      * @return the pending users; an empty list if the role name is unknown
      */
    public List<UserResponse> getPendingUsersByRole(String role) {
        Role parsed;
        try {
            parsed = Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return List.of();
        }
        return userRepository.findByRoleAndStatus(parsed, AccountStatus.PENDING).stream()
                .map(UserResponse::from)
                .toList();
    }

    /**
      * Changes the status of a user, found by login name.
      *
      * @param username the login name
      * @param status the new status
      * @throws ResourceNotFoundException if there is no such user
      */
    @Transactional
    public void updateStatus(String username, AccountStatus status) {
        User user = findByUsername(username);
        user.setStatus(status);
        userRepository.save(user);
        LOG.info("Status of user {} changed to {}", username, status);
    }

    /**
      * Changes the status of the user with the given ID. Hospital-service uses this method when a
      * doctor is approved or rejected.
      *
      * @param userId the user id
      * @param status the new status
      * @throws ResourceNotFoundException if there is no such user
      */
    @Transactional
    public void updateStatusById(int userId, AccountStatus status) {
        User user = findById(userId);
        user.setStatus(status);
        userRepository.save(user);
        LOG.info("Status of user id {} changed to {}", userId, status);
    }

    /**
      * Deactivates a user (status INACTIVE). The user cannot log in any more. The row is kept.
      *
      * @param userId the user id
      * @throws ResourceNotFoundException if there is no such user
      */
    @Transactional
    public void deactivate(int userId) {
        updateStatusById(userId, AccountStatus.INACTIVE);
    }

    /**
      * Deletes a user by id. Used to undo a failed registration.
      *
      * @param userId the user id
      * @throws ResourceNotFoundException if there is no such user
      */
    @Transactional
    public void removeUserById(int userId) {
        User user = findById(userId);
        userRepository.delete(user);
        LOG.info("User id {} deleted", userId);
    }

    private User saveNewUser(
            String username, String storedPassword, Role role, AccountStatus status) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username is already taken: " + username);
        }
        try {
            return userRepository.saveAndFlush(new User(username, storedPassword, role, status));
        } catch (DataIntegrityViolationException exception) {
            // Two requests with the same username at the same moment: the database unique key wins.
            throw new ConflictException("Username is already taken: " + username);
        }
    }

    private User findByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private User findById(int userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "User not found with ID: " + userId));
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            String accepted =
                    Arrays.stream(Role.values())
                            .map(Role::name)
                            .sorted()
                            .collect(Collectors.joining(", "));
            throw new BadRequestException("Invalid role. Accepted: " + accepted);
        }
    }

    private boolean looksLikeBcryptHash(String password) {
        return password.startsWith("$2a$")
                || password.startsWith("$2b$")
                || password.startsWith("$2y$");
    }
}
