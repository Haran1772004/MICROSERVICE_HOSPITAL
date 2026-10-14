package com.hospital.appointment.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.hospital.common.enums.AppointmentStatus;

/**
 * An appointment booked for a patient and doctor at one date and time.
 */
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Integer appointmentId;

    @Column(name = "patient_id", nullable = false)
    private int patientId;

    @Column(name = "doctor_id", nullable = false)
    private int doctorId;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    /**
     * Default constructor for JPA.
     */
    public Appointment() {
    }

    /**
     * Creates a new appointment.
     *
     * @param patientId       id of the patient
     * @param doctorId        id of the doctor
     * @param appointmentDate date of the appointment
     * @param appointmentTime time of the appointment
     * @param status          initial appointment status
     */
    public Appointment(int patientId, int doctorId, LocalDate appointmentDate,
                       LocalTime appointmentTime, AppointmentStatus status) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    /**
     * Returns the appointment ID.
     *
     * @return the appointment ID
     */
    public Integer getAppointmentId() {
        return appointmentId;
    }

    /**
     * Returns the ID of the patient.
     *
     * @return the patient ID
     */
    public int getPatientId() {
        return patientId;
    }

    /**
     * Sets the ID of the patient.
     *
     * @param patientId the patient ID
     */
    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    /**
     * Returns the ID of the doctor.
     *
     * @return the doctor ID
     */
    public int getDoctorId() {
        return doctorId;
    }

    /**
     * Sets the ID of the doctor.
     *
     * @param doctorId the doctor ID
     */
    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    /**
     * Returns the appointment date.
     *
     * @return the appointment date
     */
    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    /**
     * Sets the appointment date.
     *
     * @param appointmentDate the appointment date
     */
    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    /**
     * Returns the appointment time.
     *
     * @return the appointment time
     */
    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    /**
     * Sets the appointment time.
     *
     * @param appointmentTime the appointment time
     */
    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    /**
     * Returns the appointment status.
     *
     * @return the appointment status
     */
    public AppointmentStatus getStatus() {
        return status;
    }

    /**
     * Sets the appointment status.
     *
     * @param status the appointment status
     */
    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
}
