package com.hospital.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hospital.auth.security.CurrentUser;
import com.hospital.auth.service.UserService;

/** Admin actions on users. Replaces the old hard delete. */
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private static final Logger LOG = LoggerFactory.getLogger(AdminUserController.class);

    private final UserService userService;

    /**
      * Creates the controller.
      *
      * @param userService user logic
      */
    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    /**
      * Deactivates a user (status INACTIVE). The user can no longer log in.
      *
      * @param userId the user id
      */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}/deactivate")
    public void deactivateUser(@PathVariable int userId) {
        userService.deactivate(userId);
        LOG.info(
                "Admin {} deactivated user id {}",
                CurrentUser.get().map(user -> user.username()).orElse("unknown"),
                userId);
    }
}
