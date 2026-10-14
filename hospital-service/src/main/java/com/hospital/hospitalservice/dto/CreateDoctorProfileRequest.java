package com.hospital.hospitalservice.dto;

/**
 * Sent by auth-service when a doctor registers.
 *
 * @param userId         id of the new user in auth-service
 * @param name           full name
 * @param specialization medical specialization
 * @param phone          phone number
 * @param email          email address
 * @param departmentId   id of the department
 */
public record CreateDoctorProfileRequest(Integer userId, String name, String specialization,
                                         String phone, String email, Integer departmentId) {
}
