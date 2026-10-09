package com.hospital.hospitalservice.controller;

import com.hospital.hospitalservice.dto.DoctorRequest;
import com.hospital.hospitalservice.dto.DoctorResponse;
import com.hospital.hospitalservice.service.DoctorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints for doctors. Everyone who is logged in can read. Only an admin can change.
 * Activate and deactivate accept PATCH (new) and PUT (old project).
 */
@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    /**
     * Creates the controller.
     *
     * @param doctorService business logic for doctors
     */
    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    /**
     * Creates an ACTIVE doctor without a login account.
     *
     * @param request the doctor data
     * @return the saved doctor
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public DoctorResponse addDoctor(@RequestBody DoctorRequest request) {
        return doctorService.addDoctor(request);
    }

    /**
     * Changes a doctor.
     *
     * @param doctorId id of the doctor
     * @param request  the new data
     * @return the saved doctor
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{doctorId}")
    public DoctorResponse updateDoctor(@PathVariable int doctorId,
                                       @RequestBody DoctorRequest request) {
        return doctorService.updateDoctor(doctorId, request);
    }

    /**
     * Sets a doctor to INACTIVE.
     *
     * @param doctorId id of the doctor
     */
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/{doctorId}/deactivate",
            method = {RequestMethod.PATCH, RequestMethod.PUT})
    public void deactivateDoctor(@PathVariable int doctorId) {
        doctorService.deactivateDoctor(doctorId);
    }

    /**
     * Sets a doctor to ACTIVE.
     *
     * @param doctorId id of the doctor
     */
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/{doctorId}/activate",
            method = {RequestMethod.PATCH, RequestMethod.PUT})
    public void activateDoctor(@PathVariable int doctorId) {
        doctorService.activateDoctor(doctorId);
    }

    /**
     * Returns one doctor.
     *
     * @param doctorId id of the doctor
     * @return the doctor
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    @GetMapping("/{doctorId}")
    public DoctorResponse getDoctorById(@PathVariable int doctorId) {
        return doctorService.getDoctorById(doctorId);
    }

    /**
     * Returns all doctors.
     *
     * @return the doctors
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    @GetMapping
    public List<DoctorResponse> getAllDoctors() {
        return doctorService.getAllDoctors();
    }
}
