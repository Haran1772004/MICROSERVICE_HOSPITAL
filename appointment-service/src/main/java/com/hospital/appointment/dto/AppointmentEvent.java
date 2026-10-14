package com.hospital.appointment.dto;

/**
 * Message sent to Kafka when an appointment is booked or cancelled.
 * Date and time are plain text like "2026-10-20" and "10:30".
 *
 * @param appointmentId   id of the appointment
 * @param patientId       id of the patient
 * @param doctorId        id of the doctor
 * @param appointmentDate appointment date
 * @param appointmentTime appointment time
 * @param type            BOOKED or CANCELLED
 */
public record AppointmentEvent(int appointmentId, int patientId, int doctorId,
                               String appointmentDate, String appointmentTime, String type) {
}