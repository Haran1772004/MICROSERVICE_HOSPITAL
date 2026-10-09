package com.hospital.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Body of {@code POST /auth/login}.
 *
 * @param username the login name
 * @param password the plain password
 */
public record LoginRequest(
        @NotBlank(message = "Username is required.") String username,
        @NotBlank(message = "Password is required.") String password) {
}
