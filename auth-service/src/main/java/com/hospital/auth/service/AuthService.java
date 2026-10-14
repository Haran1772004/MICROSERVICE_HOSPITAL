package com.hospital.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import com.hospital.auth.entity.User;
import com.hospital.auth.repository.UserRepository;
import com.hospital.common.enums.AccountStatus;
import com.hospital.common.exception.UnauthorizedException;
import com.hospital.common.security.JwtUtil;

/** Login: checks the username and password, checks the account status and creates the JWT. */
@Service
public class AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    private static final String INVALID_LOGIN = "Invalid username or password.";

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
      * Creates the service.
      *
      * @param userRepository database access for users
      * @param jwtUtil helper that creates tokens
      * @param authenticationManager checks username and password
      */
    public AuthService(
            UserRepository userRepository,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
      * Logs a user in.
      *
      * @param username the login name
      * @param password the plain password
      * @return a JWT token
      * @throws UnauthorizedException if the login is wrong, or the account is PENDING, REJECTED or
      *     INACTIVE
      */
    public String login(String username, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (org.springframework.security.core.AuthenticationException exception) {
            throw new UnauthorizedException(INVALID_LOGIN);
        }

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new UnauthorizedException(INVALID_LOGIN));

        AccountStatus status = user.getStatus();
        if (status == AccountStatus.PENDING) {
            throw new UnauthorizedException(
                    "Your account is awaiting admin approval. Please try again later.");
        }
        if (status == AccountStatus.REJECTED) {
            throw new UnauthorizedException("Your account registration was rejected.");
        }
        if (status == AccountStatus.INACTIVE) {
            // New rule (the old project let INACTIVE users log in).
            throw new UnauthorizedException("Your account is inactive. Please contact an admin.");
        }

        LOG.info("User {} logged in", username);
        return jwtUtil.generateToken(user.getUsername(), user.getUserId(), user.getRole());
    }
}
