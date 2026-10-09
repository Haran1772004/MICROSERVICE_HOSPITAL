package com.hospital.appointment.dto;

import com.hospital.common.enums.AccountStatus;

/**
 * Short doctor data returned by hospital-service.
 *
 * @param doctorId     id of the doctor
 * @param userId       id of the login account in auth-service
 * @param name         name of the doctor
 * @param departmentId id of the department
 * @param status       current account status
 */
public record DoctorInfo(int doctorId, Integer userId, String name, int departmentId,
                         AccountStatus status) {
}
