package com.hospital.hospitalservice.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.common.enums.AccountStatus;
import com.hospital.common.enums.Role;
import com.hospital.common.exception.BadRequestException;
import com.hospital.common.exception.ForbiddenException;
import com.hospital.common.security.JwtUser;
import com.hospital.hospitalservice.dto.PatientRequest;
import com.hospital.hospitalservice.dto.PatientResponse;
import com.hospital.hospitalservice.security.CurrentUser;
import com.hospital.hospitalservice.service.PatientService;

/**
 * Endpoints for patients. Staff manage all patients. A patient can only see and change the
 * own profile. Activate and deactivate accept PATCH (new) and PUT (old project).
 */
@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;

    /**
     * Creates the controller.
     *
     * @param patientService business logic for patients
     */
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Creates a patient without a login account.
     *
     * @param request the patient data
     * @return the saved patient
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @PostMapping
    public PatientResponse addPatient(@RequestBody PatientRequest request) {
        return patientService.addPatient(request);
    }

    /**
     * Changes a patient.
     *
     * @param patientId id of the patient
     * @param request   the new data
     * @return the saved patient
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @PutMapping("/{patientId}")
    public PatientResponse updatePatient(@PathVariable int patientId,
                                         @RequestBody PatientRequest request) {
        return patientService.updatePatient(patientId, request);
    }

    /**
     * Sets a patient to INACTIVE.
     *
     * @param patientId id of the patient
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @RequestMapping(value = "/{patientId}/deactivate",
            method = {RequestMethod.PATCH, RequestMethod.PUT})
    public void deactivatePatient(@PathVariable int patientId) {
        patientService.deactivatePatient(patientId);
    }

    /**
     * Sets a patient to ACTIVE.
     *
     * @param patientId id of the patient
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @RequestMapping(value = "/{patientId}/activate",
            method = {RequestMethod.PATCH, RequestMethod.PUT})
    public void activatePatient(@PathVariable int patientId) {
        patientService.activatePatient(patientId);
    }

    /**
     * Returns the profile of the logged-in patient.
     *
     * @return the patient
     */
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me")
    public PatientResponse getMyPersonalDetails() {
        return patientService.getOwnPatient(CurrentUser.require().userId());
    }

    /**
     * Changes the profile of the logged-in patient.
     *
     * @param request the new data
     * @return the saved patient
     */
    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/me")
    public PatientResponse updateMyPersonalDetails(@RequestBody PatientRequest request) {
        return patientService.updateOwnPatient(CurrentUser.require().userId(), request);
    }

    /**
     * Returns one patient. A patient can only see the own profile.
     *
     * @param patientId id of the patient
     * @return the patient
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    @GetMapping("/{patientId}")
    public PatientResponse getPatientById(@PathVariable int patientId) {
        JwtUser user = CurrentUser.require();
        if (user.role() == Role.PATIENT
                && patientService.getOwnPatientId(user.userId()) != patientId) {
            throw new ForbiddenException(
                    "Access denied: you can only view your own patient profile.");
        }
        return patientService.getPatientById(patientId);
    }

    /**
     * Returns all patients.
     *
     * @return the patients
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @GetMapping
    public List<PatientResponse> getAllPatients() {
        return patientService.getAllPatients();
    }

    /**
     * Returns the patients with a status.
     *
     * @param status PENDING, ACTIVE, INACTIVE or REJECTED
     * @return the patients
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @GetMapping("/status/{status}")
    public List<PatientResponse> getPatientsByStatus(@PathVariable String status) {
        AccountStatus accountStatus;
        try {
            accountStatus = AccountStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(
                    "Invalid status. Accepted: PENDING, ACTIVE, INACTIVE, REJECTED");
        }
        return patientService.getPatientsByStatus(accountStatus);
    }
}
