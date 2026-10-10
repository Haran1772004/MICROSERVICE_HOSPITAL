package com.hospital.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /auth/register/doctor}.
 *
 * @param username login name, 3-20 letters, numbers or underscore
 * @param password plain password, 6+ characters with one capital letter
 * @param name full name, 2-60 characters
 * @param specialization medical specialization, at least 2 characters
 * @param phone phone number, 7-15 digits, optional leading +
 * @param email email address
 * @param departmentId id of an ACTIVE department in hospital-service
 */
public record DoctorRegistrationRequest(
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
                @Pattern(
                        regexp = ".*[A-Z].*",
                        groups = FormatChecks.class,
                        message = "Password must contain at least one uppercase letter.")
                String password,
        @NotBlank(message = "Doctor name is required.")
                @Size(
                        min = 2,
                        max = MAX_NAME_LENGTH,
                        groups = FormatChecks.class,
                        message = "Doctor name must be 2-60 characters.")
                String name,
        @NotBlank(message = "Specialization is required.")
                @Size(
                        min = 2,
                        groups = FormatChecks.class,
                        message = "Specialization must be at least 2 characters.")
                String specialization,
        @NotBlank(message = "Phone number is required.")
                @Pattern(
                        regexp = "^\\+?[0-9]{7,15}$",
                        groups = FormatChecks.class,
                        message = "Invalid phone number format.")
                String phone,
        @NotBlank(message = "Email is required.")
                @Pattern(
                        regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$",
                        groups = FormatChecks.class,
                        message = "Invalid email format.")
                String email,
        @NotNull(message = "A valid department ID is required.")
                @Positive(
                        groups = FormatChecks.class,
                        message = "A valid department ID is required.")
                Integer departmentId) {

    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int MAX_NAME_LENGTH = 60;

    /** Trims the name and specialization so that the length rules ignore outer spaces. */
    public DoctorRegistrationRequest {
        name = name == null ? null : name.trim();
        specialization = specialization == null ? null : specialization.trim();
    }
}
