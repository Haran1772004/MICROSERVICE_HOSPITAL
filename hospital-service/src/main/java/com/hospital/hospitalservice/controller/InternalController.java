package com.hospital.hospitalservice.controller;

import com.hospital.hospitalservice.dto.CreateDoctorProfileRequest;
import com.hospital.hospitalservice.dto.CreatePatientProfileRequest;
import com.hospital.hospitalservice.dto.DoctorInfo;
import com.hospital.hospitalservice.dto.PatientInfo;
import com.hospital.hospitalservice.service.InternalProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for other services (auth-service and appointment-service). They are not for
 * users: the security filter only lets a caller in who sends the right
 * {@code X-Internal-Secret} header.
 */
@RestController
@RequestMapping("/internal")
public class InternalController {

    private final InternalProfileService profileService;

    /**
     * Creates the controller.
     *
     * @param profileService business logic for the internal endpoints
     */
    public InternalController(InternalProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Creates the patient and the first address after a registration.
     *
     * @param request the data sent by auth-service
     * @return the short patient data, with status 201
     */
    @PostMapping("/patients")
    public ResponseEntity<PatientInfo> createPatient(
            @RequestBody CreatePatientProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createPatient(request));
    }

    /**
     * Creates a PENDING doctor after a registration.
     *
     * @param request the data sent by auth-service
     * @return the short doctor data, with status 201
     */
    @PostMapping("/doctors")
    public ResponseEntity<DoctorInfo> createDoctor(
            @RequestBody CreateDoctorProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createDoctor(request));
    }

    /**
     * Deletes the profile of a user. It answers 200 even if there is no profile.
     *
     * @param userId id of the user in auth-service
     * @return an empty 200 answer
     */
    @DeleteMapping("/profiles/by-user/{userId}")
    public ResponseEntity<Void> deleteProfileByUser(@PathVariable int userId) {
        profileService.deleteProfileByUser(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Returns short data of a patient.
     *
     * @param patientId id of the patient
     * @return the short data
     */
    @GetMapping("/patients/{patientId}")
    public PatientInfo getPatient(@PathVariable int patientId) {
        return profileService.getPatient(patientId);
    }

    /**
     * Returns short data of the patient of a login account.
     *
     * @param userId id of the user in auth-service
     * @return the short data
     */
    @GetMapping("/patients/by-user/{userId}")
    public PatientInfo getPatientByUser(@PathVariable int userId) {
        return profileService.getPatientByUser(userId);
    }

    /**
     * Returns short data of a doctor.
     *
     * @param doctorId id of the doctor
     * @return the short data
     */
    @GetMapping("/doctors/{doctorId}")
    public DoctorInfo getDoctor(@PathVariable int doctorId) {
        return profileService.getDoctor(doctorId);
    }
}
