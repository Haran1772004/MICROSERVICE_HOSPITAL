package com.hospital.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Message sent by appointment-service on Kafka.
 * Date and time are plain text like "2026-10-20" and "10:30".
 *
 * @param appointmentId   id of the appointment
 * @param patientId       id of the patient
 * @param doctorId        id of the doctor
 * @param appointmentDate appointment date
 * @param appointmentTime appointment time
 * @param type            BOOKED or CANCELLED
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AppointmentEvent(int appointmentId, int patientId, int doctorId,
                               String appointmentDate, String appointmentTime, String type) {
}
