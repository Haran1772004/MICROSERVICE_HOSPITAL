package com.hospital.appointment.repository;

import com.hospital.appointment.entity.Appointment;
import com.hospital.common.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Data access for appointments.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    List<Appointment> findAllByOrderByAppointmentDateAscAppointmentTimeAsc();

    List<Appointment> findByPatientIdOrderByAppointmentDateAscAppointmentTimeAsc(int patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(int doctorId);

    Optional<Appointment> findByAppointmentId(int appointmentId);

    List<Appointment> findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(
            int doctorId, LocalDate appointmentDate);

    List<Appointment> findByAppointmentDateOrderByAppointmentTimeAsc(LocalDate appointmentDate);

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
            int doctorId, LocalDate appointmentDate, LocalTime appointmentTime,
            AppointmentStatus status);

    boolean existsByPatientIdAndAppointmentDateAndAppointmentTimeAndStatus(
            int patientId, LocalDate appointmentDate, LocalTime appointmentTime,
            AppointmentStatus status);
}
