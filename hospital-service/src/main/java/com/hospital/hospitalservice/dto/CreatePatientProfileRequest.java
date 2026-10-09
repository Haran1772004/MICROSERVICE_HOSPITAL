package com.hospital.hospitalservice.dto;

import com.hospital.common.enums.AddressType;
import com.hospital.common.enums.Gender;

import java.time.LocalDate;

/**
 * Sent by auth-service when a patient registers: the patient and the first address.
 *
 * @param userId      id of the new user in auth-service
 * @param name        full name
 * @param dob         date of birth, format yyyy-MM-dd
 * @param gender      gender
 * @param phone       phone number
 * @param email       email address
 * @param state       state
 * @param district    district
 * @param pincode     postal code
 * @param addressType type of the address (HOME if not sent)
 */
public record CreatePatientProfileRequest(Integer userId, String name, LocalDate dob,
                                          Gender gender, String phone, String email,
                                          String state, String district, String pincode,
                                          AddressType addressType) {
}
