package com.hospital.auth.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.hospital.auth.dto.CreateUserRequest;
import com.hospital.auth.dto.OrderedChecks;
import com.hospital.auth.dto.UserResponse;
import com.hospital.auth.service.UserService;
import com.hospital.common.enums.AccountStatus;

/** User management for admins. */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
      * Creates the controller.
      *
      * @param userService user logic
      */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
      * Lists all users.
      *
      * @return all users
      */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
      * Returns one user.
      *
      * @param username the login name
      * @return the user
      */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{username}")
    public UserResponse getUser(@PathVariable String username) {
        return userService.getUser(username);
    }

    /**
      * Lists users with status PENDING and a given role.
      *
      * @param role the role name, for example DOCTOR
      * @return the pending users
      */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending/{role}")
    public List<UserResponse> getPendingUsersByRole(@PathVariable String role) {
        return userService.getPendingUsersByRole(role);
    }

    /**
      * Creates a login (for example a receptionist or another admin).
      *
      * @param request username, password, role and optional status
      */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public void createUser(@Validated(OrderedChecks.class) @RequestBody CreateUserRequest request) {
        userService.createUser(request);
    }

    /**
      * Changes the status of a user.
      *
      * @param username the login name
      * @param status PENDING, ACTIVE, INACTIVE or REJECTED
      */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{username}/status")
    public void updateStatus(@PathVariable String username, @RequestParam AccountStatus status) {
        userService.updateStatus(username, status);
    }
}
