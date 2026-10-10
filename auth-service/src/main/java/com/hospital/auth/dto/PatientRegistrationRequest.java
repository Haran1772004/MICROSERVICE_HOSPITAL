package com.hospital.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /auth/register/patient}. The address fields are optional, but if one of
 * state, district or pincode is given, all three are required.
 *
 * @param username login name, 3-20 letters, numbers or underscore
 * @param password plain password, 6+ characters with one capital letter
 * @param name full name, 2-60 characters
 * @param dob date of birth as {@code yyyy-MM-dd}
 * @param gender MALE, FEMALE or OTHER
 * @param phone phone number, 7-15 digits, optional leading +
 * @param email email address
 * @param state optional address state
 * @param district optional address district
 * @param pincode optional address pincode
 * @param addressType optional address type, HOME when missing
 */
public record PatientRegistrationRequest(
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
        @NotBlank(message = "Patient name is required.")
                @Size(
                        min = 2,
                        max = MAX_NAME_LENGTH,
                        groups = FormatChecks.class,
                        message = "Patient name must be 2-60 characters.")
                String name,
        @NotBlank(message = "Date of birth is required.") String dob,
        @NotBlank(message = "Gender is required.") String gender,
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
        String state,
        String district,
        String pincode,
        String addressType) {

    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int MAX_NAME_LENGTH = 60;

    /** Trims the name so that the length rule ignores spaces around it. */
    public PatientRegistrationRequest {
        name = name == null ? null : name.trim();
    }
}
