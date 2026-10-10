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

import com.hospital.hospitalservice.dto.DepartmentRequest;
import com.hospital.hospitalservice.dto.DepartmentResponse;
import com.hospital.hospitalservice.service.DepartmentService;

/**
 * Endpoints for departments. Everyone who is logged in can read. Only an admin can change.
 * Activate and deactivate accept PATCH (new) and PUT (old project).
 */
@RestController
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * Creates the controller.
     *
     * @param departmentService business logic for departments
     */
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * Creates a department.
     *
     * @param request the department data
     * @return the saved department
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public DepartmentResponse addDepartment(@RequestBody DepartmentRequest request) {
        return departmentService.addDepartment(request);
    }

    /**
     * Changes a department.
     *
     * @param departmentId id of the department
     * @param request      the new data
     * @return the saved department
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{departmentId}")
    public DepartmentResponse updateDepartment(@PathVariable int departmentId,
                                               @RequestBody DepartmentRequest request) {
        return departmentService.updateDepartment(departmentId, request);
    }

    /**
     * Sets a department to INACTIVE.
     *
     * @param departmentId id of the department
     */
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/{departmentId}/deactivate",
            method = {RequestMethod.PATCH, RequestMethod.PUT})
    public void deactivateDepartment(@PathVariable int departmentId) {
        departmentService.deactivateDepartment(departmentId);
    }

    /**
     * Sets a department to ACTIVE.
     *
     * @param departmentId id of the department
     */
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/{departmentId}/activate",
            method = {RequestMethod.PATCH, RequestMethod.PUT})
    public void activateDepartment(@PathVariable int departmentId) {
        departmentService.activateDepartment(departmentId);
    }

    /**
     * Returns one department.
     *
     * @param departmentId id of the department
     * @return the department
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    @GetMapping("/{departmentId}")
    public DepartmentResponse getDepartmentById(@PathVariable int departmentId) {
        return departmentService.getDepartmentById(departmentId);
    }

    /**
     * Returns all departments.
     *
     * @return the departments
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    @GetMapping
    public List<DepartmentResponse> getAllDepartments() {
        return departmentService.getAllDepartments();
    }
}
