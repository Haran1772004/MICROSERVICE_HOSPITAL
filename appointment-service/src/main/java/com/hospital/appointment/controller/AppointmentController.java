package com.hospital.appointment.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
//import org.springframework.http.ResponseEntity;

import com.hospital.appointment.dto.AppointmentRequest;
import com.hospital.appointment.dto.AppointmentResponse;
import com.hospital.appointment.security.CurrentUser;
import com.hospital.appointment.service.AppointmentService;
import com.hospital.common.security.JwtUser;

/**
 * Appointment endpoints.
 */
@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Creates the controller.
     *
     * @param appointmentService business logic for appointments
     */
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Creates a new appointment.
     *
     * @param request the appointment request
     * @return the created appointment
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'PATIENT')")
    public AppointmentResponse createAppointment(@Valid @RequestBody AppointmentRequest request) {
        JwtUser currentUser = CurrentUser.require();
        return appointmentService.createAppointment(request, currentUser);
    }

    /**
     * Returns all appointments.
     *
     * @return all appointments
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    /**
     * Returns appointments for today.
     *
     * @return today's appointments
     */
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public List<AppointmentResponse> getAppointmentsToday() {
        return appointmentService.getAppointmentsForDate(LocalDate.now());
    }

    /**
     * Returns one appointment by id.
     *
     * @param appointmentId the id of the appointment
     * @return the appointment
     */
    @GetMapping("/{appointmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public AppointmentResponse getAppointment(@PathVariable int appointmentId) {
        return appointmentService.getAppointment(appointmentId);
    }

    /**
     * Returns all appointments for a patient.
     *
     * @param patientId patient id
     * @return matching appointments
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public List<AppointmentResponse> getByPatient(@PathVariable int patientId) {
        return appointmentService.getAppointmentsByPatient(patientId);
    }

    /**
     * Returns all appointments for a doctor.
     *
     * @param doctorId doctor id
     * @return matching appointments
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public List<AppointmentResponse> getByDoctor(@PathVariable int doctorId) {
        return appointmentService.getAppointmentsByDoctor(doctorId);
    }

    /**
     * Returns all appointments for a doctor on a given date.
     *
     * @param doctorId doctor id
     * @param date     date to inspect
     * @return matching appointments
     */
    @GetMapping("/doctor/{doctorId}/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public List<AppointmentResponse> getByDoctorToday(
            @PathVariable int doctorId,
            @RequestParam(value = "date", required = false) LocalDate date) {
        return appointmentService.getAppointmentsByDoctorOnDate(doctorId,
                date == null ? LocalDate.now() : date);
    }

    /**
     * Returns available slots for a doctor on a date.
     *
     * @param doctorId doctor id
     * @param date     date to inspect
     * @return available time slots
     */
    @GetMapping("/doctor/{doctorId}/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public List<LocalTime> getAvailableSlots(@PathVariable int doctorId,
                                            @RequestParam(value = "date", required = false)
                                            LocalDate date) {
        return appointmentService.getAvailableSlots(
                doctorId, date == null ? LocalDate.now() : date);
    }

    /**
     * Cancels an appointment.
     *
     * @param appointmentId id of the appointment
     * @return the updated appointment
     */
    @PatchMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT', 'DOCTOR')")
    public AppointmentResponse cancelAppointment(@PathVariable int appointmentId) {
        return appointmentService.cancelAppointment(appointmentId);
    }
}
