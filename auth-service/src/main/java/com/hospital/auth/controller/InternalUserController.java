package com.hospital.auth.controller;

import com.hospital.auth.dto.StatusUpdateRequest;
import com.hospital.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for other services only. They need the {@code X-Internal-Secret} header;
 * a user JWT does not open them (see {@code InternalSecretFilter}).
 */
@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;

    /**
     * Creates the controller.
     *
     * @param userService user logic
     */
    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Sets the status of a user. hospital-service calls this when a doctor is approved
     * (ACTIVE) or rejected (REJECTED).
     *
     * @param userId  the user id
     * @param request the new status
     */
    @PatchMapping("/{userId}/status")
    public void updateStatus(@PathVariable int userId,
                             @Valid @RequestBody StatusUpdateRequest request) {
        userService.updateStatusById(userId, request.status());
    }

    /**
     * Deletes a user. Only used to undo a failed registration.
     *
     * @param userId the user id
     */
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable int userId) {
        userService.removeUserById(userId);
    }
}
