package com.hospital.auth.dto;
/**
 * Body sent to hospital-service {@code POST /internal/patients}. The address fields are null when
 * the patient gave no address.
 *
 * @param userId id of the new user in auth-service
 * @param name full name
 * @param dob date of birth as {@code yyyy-MM-dd}
 * @param gender MALE, FEMALE or OTHER
 * @param phone phone number
 * @param email email address
 * @param state address state, or null
 * @param district address district, or null
 * @param pincode address pincode, or null
 * @param addressType HOME, WORK, BILLING or EMERGENCY, or null when there is no address
 */
public record CreatePatientProfileRequest(
        int userId,
        String name,
        String dob,
        String gender,
        String phone,
        String email,
        String state,
        String district,
        String pincode,
        String addressType) { }
