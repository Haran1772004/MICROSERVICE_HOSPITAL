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
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * A doctor profile. {@code userId} is the id of the login account in auth-service. It is a
 * plain number: the user is in another database, so there is no foreign key.
 * This entity is never returned from the API; use {@code DoctorResponse}.
 */
@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctor_id")
    private int doctorId;

    @Column(name = "user_id", unique = true)
    private Integer userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "specialization", nullable = false, length = 100)
    private String specialization;

    @Column(name = "phone", nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 8)
    private AccountStatus status;

    /**
     * Creates an empty doctor. Required by JPA.
     */
    protected Doctor() {
    }

    /**
     * Creates a new doctor that is not saved yet.
     *
     * @param userId         id of the login account, or null if the doctor has no login
     * @param name           full name
     * @param specialization medical specialization
     * @param phone          phone number
     * @param email          email address
     * @param department     department of the doctor
     * @param status         account status
     */
    public Doctor(Integer userId, String name, String specialization, String phone,
                  String email, Department department, AccountStatus status) {
        this.userId = userId;
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
        this.department = department;
        this.status = status;
    }

    /**
     * Returns the id.
     *
     * @return the doctor id
     */
    public int getDoctorId() {
        return doctorId;
    }

    /**
     * Returns the id of the login account.
     *
     * @return the user id, or null if the doctor has no login
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * Returns the name.
     *
     * @return the full name
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
     * Returns the specialization.
     *
     * @return the specialization
     */
    public String getSpecialization() {
        return specialization;
    }

    /**
     * Sets the specialization.
     *
     * @param specialization the new specialization
     */
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    /**
     * Returns the phone number.
     *
     * @return the phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number.
     *
     * @param phone the new phone number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address.
     *
     * @param email the new email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the department.
     *
     * @return the department of the doctor
     */
    public Department getDepartment() {
        return department;
    }

    /**
     * Sets the department.
     *
     * @param department the new department
     */
    public void setDepartment(Department department) {
        this.department = department;
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
