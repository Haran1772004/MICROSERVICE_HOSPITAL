package com.hospital.hospitalservice.dto;

/**
 * Body of the call that changes a user status in auth-service.
 *
 * @param status the new status name, for example ACTIVE
 */
public record StatusBody(String status) {
}
