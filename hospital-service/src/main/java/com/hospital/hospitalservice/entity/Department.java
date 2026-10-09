package com.hospital.hospitalservice.entity;

import com.hospital.common.enums.AccountStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A hospital department, for example Cardiology. Doctors belong to a department.
 * This entity is never returned from the API; use {@code DepartmentResponse}.
 */
@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private int departmentId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 8)
    private AccountStatus status;

    /**
     * Creates an empty department. Required by JPA.
     */
    protected Department() {
    }

    /**
     * Creates a new department that is not saved yet.
     *
     * @param name        unique department name
     * @param description short text about the department (may be null)
     * @param status      account status
     */
    public Department(String name, String description, AccountStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    /**
     * Returns the id.
     *
     * @return the department id
     */
    public int getDepartmentId() {
        return departmentId;
    }

    /**
     * Returns the name.
     *
     * @return the department name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description.
     *
     * @return the description, or null
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the status.
     *
     * @return the account status
     */
    public AccountStatus getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the new status
     */
    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}
