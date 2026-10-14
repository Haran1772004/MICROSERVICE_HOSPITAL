package com.hospital.hospitalservice.dto;

/**
 * Data to create or update a doctor. The department can be sent as
 * {@code "departmentId": 1} or, like in the old API, as {@code "department": {"departmentId": 1}}.
 *
 * @param name           full name
 * @param specialization medical specialization
 * @param phone          phone number
 * @param email          email address
 * @param departmentId   id of the department
 * @param department     the department in the old nested form (optional)
 */
public record DoctorRequest(String name, String specialization, String phone, String email,
                            Integer departmentId, DepartmentRef department) {

    /**
     * The department in the old nested form.
     *
     * @param departmentId id of the department
     */
    public record DepartmentRef(Integer departmentId) {
    }

    /**
     * Returns the department id from either form.
     *
     * @return the department id, or 0 if none was sent
     */
    public int resolveDepartmentId() {
        if (departmentId != null) {
            return departmentId;
        }
        if (department != null && department.departmentId() != null) {
            return department.departmentId();
        }
        return 0;
    }
}
