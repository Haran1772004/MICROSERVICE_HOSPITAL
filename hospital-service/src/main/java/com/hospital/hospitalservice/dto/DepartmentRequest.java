package com.hospital.hospitalservice.dto;

/**
 * Data to create or update a department.
 *
 * @param name        unique department name
 * @param description short text about the department (optional)
 */
public record DepartmentRequest(String name, String description) {
}
