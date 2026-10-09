package com.hospital.hospitalservice.dto;

import com.hospital.common.enums.AccountStatus;
import com.hospital.hospitalservice.entity.Patient;

/**
 * Short patient data for other services.
 *
 * @param patientId id of the patient
 * @param userId    id of the login account in auth-service
 * @param name      full name
 * @param status    account status
 */
public record PatientInfo(int patientId, Integer userId, String name, AccountStatus status) {

    /**
     * Builds the data from an entity.
     *
     * @param patient the patient
     * @return the short data
     */
    public static PatientInfo from(Patient patient) {
        return new PatientInfo(patient.getPatientId(), patient.getUserId(), patient.getName(),
                patient.getStatus());
    }
}
