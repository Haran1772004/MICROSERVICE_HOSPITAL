package com.hospital.appointment.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.appointment.dto.MedicalRecordRequest;
import com.hospital.appointment.dto.MedicalRecordResponse;
import com.hospital.appointment.service.MedicalRecordService;

/**
 * Medical record endpoints.
 */
@RestController
@RequestMapping("/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    /**
     * Creates the controller.
     *
     * @param medicalRecordService business logic for records
     */
    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    /**
     * Creates a medical record.
     *
     * @param request the record request
     * @return the created record
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    public MedicalRecordResponse createMedicalRecord(
            @Valid @RequestBody MedicalRecordRequest request) {
        return medicalRecordService.createMedicalRecord(request);
    }

    /**
     * Returns all records.
     *
     * @return all records
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public List<MedicalRecordResponse> getAllMedicalRecords() {
        return medicalRecordService.getAllMedicalRecords();
    }

    /**
     * Returns one record.
     *
     * @param recordId id of the record
     * @return the record
     */
    @GetMapping("/{recordId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public MedicalRecordResponse getMedicalRecord(@PathVariable int recordId) {
        return medicalRecordService.getMedicalRecord(recordId);
    }

    /**
     * Returns the record for one appointment.
     *
     * @param appointmentId id of the appointment
     * @return the record
     */
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public MedicalRecordResponse getMedicalRecordByAppointment(@PathVariable int appointmentId) {
        return medicalRecordService.getMedicalRecordByAppointment(appointmentId);
    }

    /**
     * Returns records for one patient.
     *
     * @param patientId id of the patient
     * @return records for that patient
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public List<MedicalRecordResponse> getMedicalRecordsByPatient(@PathVariable int patientId) {
        return medicalRecordService.getMedicalRecordsByPatient(patientId);
    }

    /**
     * Returns records for one doctor.
     *
     * @param doctorId id of the doctor
     * @return records for that doctor
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public List<MedicalRecordResponse> getMedicalRecordsByDoctor(@PathVariable int doctorId) {
        return medicalRecordService.getMedicalRecordsByDoctor(doctorId);
    }
}
