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

import com.hospital.appointment.dto.PrescriptionRequest;
import com.hospital.appointment.dto.PrescriptionResponse;
import com.hospital.appointment.service.PrescriptionService;

/**
 * Prescription endpoints.
 */
@RestController
@RequestMapping("/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    /**
     * Creates the controller.
     *
     * @param prescriptionService business logic for prescriptions
     */
    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    /**
     * Creates a prescription.
     *
     * @param request the prescription request
     * @return the created prescription
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST')")
    public PrescriptionResponse createPrescription(
            @Valid @RequestBody PrescriptionRequest request) {
        return prescriptionService.createPrescription(request);
    }

    /**
     * Returns prescriptions for one medical record.
     *
     * @param recordId id of the medical record
     * @return the collected prescriptions
     */
    @GetMapping("/record/{recordId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public List<PrescriptionResponse> getPrescriptionsByRecord(@PathVariable int recordId) {
        return prescriptionService.getPrescriptionsByRecord(recordId);
    }
}
