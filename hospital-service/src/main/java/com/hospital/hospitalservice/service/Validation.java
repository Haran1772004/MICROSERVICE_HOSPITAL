package com.hospital.hospitalservice.service;

import com.hospital.common.enums.Gender;
import com.hospital.common.exception.BadRequestException;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Input checks shared by the services. The messages are the same as in the old project.
 */
final class Validation {

    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{7,15}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validation() {
    }

    /**
     * Removes spaces around a text.
     *
     * @param value the text, may be null
     * @return the trimmed text, or null if the text was null
     */
    static String clean(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * Checks that a text is not empty.
     *
     * @param value   the text
     * @param message the error message if it is empty
     * @throws BadRequestException if the text is null or blank
     */
    static void required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
    }

    /**
     * Checks the data of a patient.
     *
     * @param name   full name
     * @param dob    date of birth, may be null
     * @param gender gender
     * @param phone  phone number
     * @param email  email address
     * @throws BadRequestException if something is wrong
     */
    static void patient(String name, LocalDate dob, Gender gender, String phone, String email) {
        required(name, "Patient name is required");
        if (name.length() < 2 || name.length() > 60) {
            throw new BadRequestException("Patient name must be 2-60 characters");
        }
        if (dob != null && dob.isAfter(LocalDate.now())) {
            throw new BadRequestException("Date of birth cannot be in the future");
        }
        if (gender == null) {
            throw new BadRequestException("Gender is required");
        }
        phone(phone);
        email(email);
    }

    /**
     * Checks the data of a doctor.
     *
     * @param name           full name
     * @param specialization medical specialization
     * @param phone          phone number
     * @param email          email address
     * @throws BadRequestException if something is wrong
     */
    static void doctor(String name, String specialization, String phone, String email) {
        required(name, "Doctor name is required");
        if (name.length() < 2 || name.length() > 60) {
            throw new BadRequestException("Doctor name must be 2-60 characters");
        }
        required(specialization, "Specialization is required");
        if (specialization.length() > 100) {
            throw new BadRequestException("Specialization must be at most 100 characters");
        }
        phone(phone);
        email(email);
    }

    /**
     * Checks the data of an address.
     *
     * @param state    state
     * @param district district
     * @param pincode  postal code
     * @throws BadRequestException if something is wrong
     */
    static void address(String state, String district, String pincode) {
        required(state, "State is required");
        required(district, "District is required");
        required(pincode, "Pincode is required");
        if (state.length() > 100 || district.length() > 100) {
            throw new BadRequestException("State and district must be at most 100 characters");
        }
        if (pincode.length() > 10) {
            throw new BadRequestException("Pincode must be at most 10 characters");
        }
    }

    private static void phone(String phone) {
        required(phone, "Phone number is required");
        if (!PHONE.matcher(phone).matches()) {
            throw new BadRequestException("Invalid phone number format");
        }
    }

    private static void email(String email) {
        required(email, "Email is required");
        if (email.length() > 100 || !EMAIL.matcher(email).matches()) {
            throw new BadRequestException("Invalid email format");
        }
    }
}
