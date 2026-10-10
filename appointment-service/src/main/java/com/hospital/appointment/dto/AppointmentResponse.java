package com.hospital.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.hospital.appointment.entity.Appointment;
import com.hospital.common.enums.AppointmentStatus;

/**
 * Data returned for an appointment.
 *
 * @param appointmentId   id of the appointment
 * @param patientId       id of the patient
 * @param doctorId        id of the doctor
 * @param appointmentDate appointment date
 * @param appointmentTime appointment time
 * @param status          status of the appointment
 */
public record AppointmentResponse(int appointmentId, int patientId, int doctorId,
                                  LocalDate appointmentDate, LocalTime appointmentTime,
                                  AppointmentStatus status) {

    /**
     * Builds a response from an entity.
     *
     * @param appointment the appointment entity
     * @return the response data
     */
    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getAppointmentId(),
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getStatus());
    }
}
