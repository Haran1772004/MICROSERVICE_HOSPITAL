package com.hospital.hospitalservice.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.hospitalservice.dto.DoctorResponse;
import com.hospital.hospitalservice.dto.MessageResponse;
import com.hospital.hospitalservice.service.AdminService;

/**
 * Admin endpoints to approve or reject doctors who registered.
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * Creates the controller.
     *
     * @param adminService admin business logic
     */
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Returns the doctors that wait for approval.
     *
     * @return the pending doctors
     */
    @GetMapping("/doctors/pending")
    public List<DoctorResponse> getPendingDoctors() {
        return adminService.getPendingDoctors();
    }

    /**
     * Approves a doctor.
     *
     * @param doctorId id of the doctor
     * @return a message for the admin
     */
    @PostMapping("/doctors/{doctorId}/approve")
    public MessageResponse approveDoctor(@PathVariable int doctorId) {
        return adminService.approveDoctor(doctorId);
    }

    /**
     * Rejects a doctor.
     *
     * @param doctorId id of the doctor
     * @return a message for the admin
     */
    @PostMapping("/doctors/{doctorId}/reject")
    public MessageResponse rejectDoctor(@PathVariable int doctorId) {
        return adminService.rejectDoctor(doctorId);
    }
}
