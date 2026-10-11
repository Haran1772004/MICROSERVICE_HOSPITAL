package com.hospital.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Short doctor data returned by hospital-service.
 *
 * @param doctorId id of the doctor
 * @param name     name of the doctor
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DoctorInfo(int doctorId, String name) {
}
