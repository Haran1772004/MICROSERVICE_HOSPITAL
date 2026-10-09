package com.hospital.hospitalservice.dto;

import com.hospital.common.enums.Gender;

import java.time.LocalDate;

/**
 * Data to create or update a patient.
 *
 * @param name   full name
 * @param dob    date of birth, format yyyy-MM-dd (optional)
 * @param gender gender: MALE, FEMALE or OTHER
 * @param phone  phone number
 * @param email  email address
 */
public record PatientRequest(String name, LocalDate dob, Gender gender, String phone,
                             String email) {
}
