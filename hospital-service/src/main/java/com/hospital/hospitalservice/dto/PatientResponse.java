package com.hospital.hospitalservice.dto;

import java.io.Serializable;
import java.time.LocalDate;

import com.hospital.common.enums.AccountStatus;
import com.hospital.common.enums.Gender;
import com.hospital.hospitalservice.entity.Patient;

/**
 * A patient as sent to the caller.
 *
 * @param patientId  id of the patient
 * @param userId     id of the login account in auth-service (may be null)
 * @param name       full name
 * @param dob        date of birth
 * @param gender     gender
 * @param phone      phone number
 * @param email      email address
 * @param status     account status
 * @param statusName the status as text
 */
public record PatientResponse(int patientId, Integer userId, String name, LocalDate dob,
                              Gender gender, String phone, String email, AccountStatus status,
                              String statusName) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Builds the response from an entity.
     *
     * @param patient the patient
     * @return the response
     */
    public static PatientResponse from(Patient patient) {
        AccountStatus status = patient.getStatus();
        return new PatientResponse(patient.getPatientId(), patient.getUserId(),
                patient.getName(), patient.getDob(), patient.getGender(), patient.getPhone(),
                patient.getEmail(), status, status == null ? null : status.name());
    }
}
