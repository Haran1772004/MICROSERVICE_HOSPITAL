package com.hospital.hospitalservice.dto;

import com.hospital.common.enums.AccountStatus;
import com.hospital.hospitalservice.entity.Doctor;

import java.io.Serializable;

/**
 * A doctor as sent to the caller. It is {@link Serializable} because it can be stored in
 * the Redis cache.
 *
 * @param doctorId       id of the doctor
 * @param userId         id of the login account in auth-service (may be null)
 * @param name           full name
 * @param specialization medical specialization
 * @param phone          phone number
 * @param email          email address
 * @param department     the department of the doctor
 * @param status         account status
 * @param statusName     the status as text
 */
public record DoctorResponse(int doctorId, Integer userId, String name, String specialization,
                             String phone, String email, DepartmentResponse department,
                             AccountStatus status, String statusName)
        implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Builds the response from an entity.
     *
     * @param doctor the doctor
     * @return the response
     */
    public static DoctorResponse from(Doctor doctor) {
        AccountStatus status = doctor.getStatus();
        return new DoctorResponse(doctor.getDoctorId(), doctor.getUserId(), doctor.getName(),
                doctor.getSpecialization(), doctor.getPhone(), doctor.getEmail(),
                DepartmentResponse.from(doctor.getDepartment()), status,
                status == null ? null : status.name());
    }
}
