package com.hospital.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Short patient data returned by hospital-service.
 *
 * @param patientId id of the patient
 * @param name      name of the patient
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PatientInfo(int patientId, String name) {
}
