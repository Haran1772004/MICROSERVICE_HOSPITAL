package com.hospital.appointment.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.appointment.client.HospitalServiceClient;
import com.hospital.appointment.dto.AppointmentRequest;
import com.hospital.appointment.dto.AppointmentResponse;
import com.hospital.appointment.dto.DoctorInfo;
import com.hospital.appointment.dto.PatientInfo;
import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.common.enums.AccountStatus;
import com.hospital.common.enums.AppointmentStatus;
import com.hospital.common.enums.Role;
import com.hospital.common.exception.BadRequestException;
import com.hospital.common.exception.ConflictException;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.common.security.JwtUser;

/**
 * Business logic for appointments.
 */
@Service
public class AppointmentService {

    private static final List<LocalTime> AVAILABLE_SLOTS = List.of(
            LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0), LocalTime.of(10, 30),
            LocalTime.of(11, 0), LocalTime.of(11, 30), LocalTime.of(12, 0),
            LocalTime.of(12, 30), LocalTime.of(13, 0), LocalTime.of(13, 30),
            LocalTime.of(14, 0), LocalTime.of(14, 30), LocalTime.of(15, 0),
            LocalTime.of(15, 30), LocalTime.of(16, 0), LocalTime.of(16, 30),
            LocalTime.of(17, 0), LocalTime.of(17, 30));

    private final AppointmentRepository appointmentRepository;
    private final HospitalServiceClient hospitalServiceClient;

    /**
     * Creates the service.
     *
     * @param appointmentRepository repository for appointments
     * @param hospitalServiceClient remote access to hospital-service
     */
    public AppointmentService(AppointmentRepository appointmentRepository,
                              HospitalServiceClient hospitalServiceClient) {
        this.appointmentRepository = appointmentRepository;
        this.hospitalServiceClient = hospitalServiceClient;
    }

    /**
     * Creates a new appointment.
     *
     * @param request the appointment request
     * @param currentUser the authenticated user
     * @return the created appointment
     */
    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request, JwtUser currentUser) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }
        if (request.doctorId() == null || request.appointmentDate() == null
                || request.appointmentTime() == null) {
            throw new BadRequestException(
                    "doctorId, appointmentDate and appointmentTime are required.");
        }

        int patientId = resolvePatientId(request, currentUser);
        ensurePatientIsActive(patientId);
        ensureDoctorIsActive(request.doctorId());
        ensureTimeSlotIsFree(patientId, request.doctorId(), request.appointmentDate(),
                request.appointmentTime());

        Appointment appointment = new Appointment(patientId, request.doctorId(),
                request.appointmentDate(), request.appointmentTime(), AppointmentStatus.SCHEDULED);
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    /**
     * Reads all appointments.
     *
     * @return all appointments sorted by date and time
     */
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAllByOrderByAppointmentDateAscAppointmentTimeAsc().stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    /**
     * Reads one appointment.
     *
     * @param appointmentId id of the appointment
     * @return the appointment
     */
    public AppointmentResponse getAppointment(int appointmentId) {
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found."));
        return AppointmentResponse.from(appointment);
    }

    /**
     * Returns all appointments for a patient.
     *
     * @param patientId id of the patient
     * @return appointments for that patient
     */
    public List<AppointmentResponse> getAppointmentsByPatient(int patientId) {
        return appointmentRepository
                .findByPatientIdOrderByAppointmentDateAscAppointmentTimeAsc(patientId)
                .stream().map(AppointmentResponse::from).toList();
    }

    /**
     * Returns all appointments for a doctor.
     *
     * @param doctorId id of the doctor
     * @return appointments for that doctor
     */
    public List<AppointmentResponse> getAppointmentsByDoctor(int doctorId) {
        return appointmentRepository
                .findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(doctorId)
                .stream().map(AppointmentResponse::from).toList();
    }

    /**
     * Returns all appointments for a doctor on one date.
     *
     * @param doctorId id of the doctor
     * @param date     appointment date
     * @return appointments on that date
     */
    public List<AppointmentResponse> getAppointmentsByDoctorOnDate(int doctorId, LocalDate date) {
        return appointmentRepository
                .findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(doctorId, date)
                .stream().map(AppointmentResponse::from).toList();
    }

    /**
     * Returns all appointments for one date.
     *
     * @param date the appointment date
     * @return appointments for that date
     */
    public List<AppointmentResponse> getAppointmentsForDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDateOrderByAppointmentTimeAsc(date)
                .stream().map(AppointmentResponse::from).toList();
    }

    /**
     * Returns the available time slots for a doctor on a date.
     *
     * @param doctorId id of the doctor
     * @param date     appointment date
     * @return the list of open slots
     */
    public List<LocalTime> getAvailableSlots(int doctorId, LocalDate date) {
        ensureDoctorIsActive(doctorId);
        List<LocalTime> booked = appointmentRepository
                .findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(doctorId, date)
                .stream()
                .map(Appointment::getAppointmentTime)
                .toList();
        return AVAILABLE_SLOTS.stream().filter(slot -> !booked.contains(slot)).toList();
    }

    /**
     * Cancels a scheduled appointment.
     *
     * @param appointmentId id of the appointment
     * @return the updated appointment
     */
    @Transactional
    public AppointmentResponse cancelAppointment(int appointmentId) {
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found."));
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ConflictException("Only scheduled appointments can be cancelled.");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    private int resolvePatientId(AppointmentRequest request, JwtUser currentUser) {
        if (currentUser.role() == Role.PATIENT) {
            PatientInfo patient = hospitalServiceClient.getPatientByUser(currentUser.userId());
            if (patient == null) {
                throw new ResourceNotFoundException("Patient not found.");
            }
            return patient.patientId();
        }

        if (request.patientId() == null) {
            throw new BadRequestException("patientId is required for reception staff.");
        }
        return request.patientId();
    }

    private void ensurePatientIsActive(int patientId) {
        PatientInfo patient = hospitalServiceClient.getPatient(patientId);
        if (patient.status() != AccountStatus.ACTIVE) {
            throw new ConflictException("Patient is not active.");
        }
    }

    private void ensureDoctorIsActive(int doctorId) {
        DoctorInfo doctor = hospitalServiceClient.getDoctor(doctorId);
        if (doctor.status() != AccountStatus.ACTIVE) {
            throw new ConflictException("Doctor is not active.");
        }
    }

    private void ensureTimeSlotIsFree(int patientId, int doctorId, LocalDate date, LocalTime time) {
        boolean doctorBusy = appointmentRepository
                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
                        doctorId, date, time, AppointmentStatus.SCHEDULED);
        if (doctorBusy) {
            throw new ConflictException("Doctor is already booked for this date and time.");
        }

        boolean patientBusy = appointmentRepository
                .existsByPatientIdAndAppointmentDateAndAppointmentTimeAndStatus(
                        patientId, date, time, AppointmentStatus.SCHEDULED);
        if (patientBusy) {
            throw new ConflictException(
                    "Patient already has a scheduled appointment at this date and time.");
        }
    }
}
