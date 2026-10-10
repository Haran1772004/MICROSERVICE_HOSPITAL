package com.hospital.appointment.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospital.appointment.entity.Appointment;
import com.hospital.common.enums.AppointmentStatus;

/**
 * Data access for appointments.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    /**
     * Returns appointments ordered by date and time.
     *
     * @return all appointments in chronological order
     */
    List<Appointment> findAllByOrderByAppointmentDateAscAppointmentTimeAsc();

    /**
     * Returns appointments for one patient, ordered by date and time.
     *
     * @param patientId the patient ID
     * @return the patient's appointments
     */
    List<Appointment> findByPatientIdOrderByAppointmentDateAscAppointmentTimeAsc(int patientId);

    /**
     * Returns appointments for one doctor, ordered by date and time.
     *
     * @param doctorId the doctor ID
     * @return the doctor's appointments
     */
    List<Appointment> findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(int doctorId);

    /**
     * Finds an appointment by its ID.
     *
     * @param appointmentId the appointment ID
     * @return the appointment, if found
     */
    Optional<Appointment> findByAppointmentId(int appointmentId);

    /**
     * Returns a doctor's appointments on a date, ordered by time.
     *
     * @param doctorId the doctor ID
     * @param appointmentDate the appointment date
     * @return matching appointments
     */
    List<Appointment> findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(
            int doctorId, LocalDate appointmentDate);

    /**
     * Returns all appointments on a date, ordered by time.
     *
     * @param appointmentDate the appointment date
     * @return matching appointments
     */
    List<Appointment> findByAppointmentDateOrderByAppointmentTimeAsc(LocalDate appointmentDate);

    /**
     * Checks whether a doctor has an appointment at a date and time with the given status.
     *
     * @param doctorId the doctor ID
     * @param appointmentDate the appointment date
     * @param appointmentTime the appointment time
     * @param status the appointment status
     * @return {@code true} if a matching appointment exists
     */
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
            int doctorId, LocalDate appointmentDate, LocalTime appointmentTime,
            AppointmentStatus status);

    /**
     * Checks whether a patient has an appointment at a date and time with the given status.
     *
     * @param patientId the patient ID
     * @param appointmentDate the appointment date
     * @param appointmentTime the appointment time
     * @param status the appointment status
     * @return {@code true} if a matching appointment exists
     */
    boolean existsByPatientIdAndAppointmentDateAndAppointmentTimeAndStatus(
            int patientId, LocalDate appointmentDate, LocalTime appointmentTime,
            AppointmentStatus status);
}
