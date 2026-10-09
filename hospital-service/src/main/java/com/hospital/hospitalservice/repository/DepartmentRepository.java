package com.hospital.hospitalservice.repository;

import com.hospital.hospitalservice.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Database access for departments.
 */
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    /**
     * Checks if a department name is already used.
     *
     * @param name the name
     * @return true if a department has this name
     */
    boolean existsByName(String name);

    /**
     * Checks if a department name is used by another department.
     *
     * @param name         the name
     * @param departmentId id of the department to ignore
     * @return true if another department has this name
     */
    boolean existsByNameAndDepartmentIdNot(String name, int departmentId);
}
