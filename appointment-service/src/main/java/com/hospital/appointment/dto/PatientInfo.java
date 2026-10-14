package com.hospital.appointment.dto;

import com.hospital.common.enums.AccountStatus;

/**
 * Short patient data returned by hospital-service.
 *
 * @param patientId id of the patient
 * @param userId    id of the login account in auth-service
 * @param name      name of the patient
 * @param status    current account status
 */
public record PatientInfo(int patientId, Integer userId, String name, AccountStatus status) {
}
