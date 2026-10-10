package com.hospital.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.hospital.common.enums.AccountStatus;

/**
 * Body of {@code POST /users} (admin creates a login, for example a receptionist).
 *
 * @param username login name, 3-20 letters, numbers or underscore
 * @param password plain password, at least 6 characters
 * @param role ADMIN, RECEPTIONIST, DOCTOR or PATIENT (not case sensitive)
 * @param status optional account status, ACTIVE when missing
 */
public record CreateUserRequest(
        @NotBlank(message = "Username is required.")
                @Pattern(
                        regexp = "^[a-zA-Z0-9_]{3,20}$",
                        groups = FormatChecks.class,
                        message =
                                "Username must be 3-20 characters and contain only letters, "
                                        + "numbers, and underscores.")
                String username,
        @NotBlank(message = "Password is required.")
                @Size(
                        min = PASSWORD_MIN_LENGTH,
                        groups = FormatChecks.class,
                        message = "Password must be at least 6 characters.")
                String password,
        @NotBlank(message = "Role is required.") String role,
        AccountStatus status) {

    private static final int PASSWORD_MIN_LENGTH = 6;

}
