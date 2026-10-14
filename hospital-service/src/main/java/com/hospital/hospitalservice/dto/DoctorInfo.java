package com.hospital.hospitalservice.dto;

import com.hospital.common.enums.AccountStatus;
import com.hospital.hospitalservice.entity.Doctor;

/**
 * Short doctor data for other services.
 *
 * @param doctorId     id of the doctor
 * @param userId       id of the login account in auth-service
 * @param name         full name
 * @param departmentId id of the department
 * @param status       account status
 */
public record DoctorInfo(int doctorId, Integer userId, String name, int departmentId,
                         AccountStatus status) {

    /**
     * Builds the data from an entity.
     *
     * @param doctor the doctor
     * @return the short data
     */
    public static DoctorInfo from(Doctor doctor) {
        return new DoctorInfo(doctor.getDoctorId(), doctor.getUserId(), doctor.getName(),
                doctor.getDepartment().getDepartmentId(), doctor.getStatus());
    }
}
