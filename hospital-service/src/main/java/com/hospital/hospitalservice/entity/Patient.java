package com.hospital.hospitalservice.entity;

import com.hospital.common.enums.AccountStatus;
import com.hospital.common.enums.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * A patient profile. {@code userId} is the id of the login account in auth-service. It is a
 * plain number: the user is in another database, so there is no foreign key.
 * This entity is never returned from the API; use {@code PatientResponse}.
 */
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private int patientId;

    @Column(name = "user_id", unique = true)
    private Integer userId;

    @Column(name = "name", nullable = false, length = 60)
    private String name;

    @Column(name = "dob")
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 6)
    private Gender gender;

    @Column(name = "phone", nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 8)
    private AccountStatus status;

    /**
     * Creates an empty patient. Required by JPA.
     */
    protected Patient() {
    }

    /**
     * Creates a new patient that is not saved yet.
     *
     * @param userId id of the login account, or null if the patient has no login
     * @param name   full name
     * @param dob    date of birth (may be null)
     * @param gender gender
     * @param phone  phone number
     * @param email  email address
     * @param status account status
     */
    public Patient(Integer userId, String name, LocalDate dob, Gender gender, String phone,
                   String email, AccountStatus status) {
        this.userId = userId;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.status = status;
    }

    /**
     * Returns the id.
     *
     * @return the patient id
     */
    public int getPatientId() {
        return patientId;
    }

    /**
     * Returns the id of the login account.
     *
     * @return the user id, or null if the patient has no login
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
     * Returns the date of birth.
     *
     * @return the date of birth, or null
     */
    public LocalDate getDob() {
        return dob;
    }

    /**
     * Sets the date of birth.
     *
     * @param dob the new date of birth
     */
    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    /**
     * Returns the gender.
     *
     * @return the gender
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Sets the gender.
     *
     * @param gender the new gender
     */
    public void setGender(Gender gender) {
        this.gender = gender;
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
