package com.hospital.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

/**
 * Request to create a new appointment.
 *
 * @param patientId        id of the patient (required for staff; ignored for patients)
 * @param doctorId         id of the doctor
 * @param appointmentDate  date of the appointment
 * @param appointmentTime  time of the appointment
 */
public record AppointmentRequest(Integer patientId,
                                 @NotNull(message = "doctorId is required") Integer doctorId,
                                 @NotNull(message = "appointmentDate is required")
                                 LocalDate appointmentDate,
                                 @NotNull(message = "appointmentTime is required")
                                 LocalTime appointmentTime) {
}
