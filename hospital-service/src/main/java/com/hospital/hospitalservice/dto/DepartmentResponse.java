package com.hospital.hospitalservice.dto;

import com.hospital.common.enums.AccountStatus;
import com.hospital.hospitalservice.entity.Department;

import java.io.Serializable;

/**
 * A department as sent to the caller and stored in the local cache.
 *
 * @param departmentId id of the department
 * @param name         department name
 * @param description  short text about the department
 * @param status       account status
 * @param statusName   the status as text
 */
public record DepartmentResponse(int departmentId, String name, String description,
                                 AccountStatus status, String statusName)
        implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Builds the response from an entity.
     *
     * @param department the department
     * @return the response
     */
    public static DepartmentResponse from(Department department) {
        AccountStatus status = department.getStatus();
        return new DepartmentResponse(department.getDepartmentId(), department.getName(),
                department.getDescription(), status, status == null ? null : status.name());
    }
}
