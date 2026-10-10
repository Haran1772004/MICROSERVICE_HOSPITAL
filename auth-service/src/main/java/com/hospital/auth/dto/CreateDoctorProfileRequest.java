package com.hospital.auth.dto;
/**
 * Body sent to hospital-service {@code POST /internal/doctors}. The doctor is created there with
 * status PENDING.
 *
 * @param userId id of the new user in auth-service
 * @param name full name
 * @param specialization medical specialization
 * @param phone phone number
 * @param email email address
 * @param departmentId id of the department
 */
public record CreateDoctorProfileRequest(
        int userId,
        String name,
        String specialization,
        String phone,
        String email,
        int departmentId) { }
