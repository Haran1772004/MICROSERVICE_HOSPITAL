package com.hospital.hospitalservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.common.enums.AccountStatus;
import com.hospital.hospitalservice.entity.Patient;

/**
 * Database access for patients.
 */
public interface PatientRepository extends JpaRepository<Patient, Integer> {

    /**
     * Checks if an email is already used by a patient.
     *
     * @param email the email
     * @return true if used
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a phone number is already used by a patient.
     *
     * @param phone the phone number
     * @return true if used
     */
    boolean existsByPhone(String phone);

    /**
     * Checks if an email is used by another patient.
     *
     * @param email     the email
     * @param patientId id of the patient to ignore
     * @return true if another patient uses it
     */
    boolean existsByEmailAndPatientIdNot(String email, int patientId);

    /**
     * Checks if a phone number is used by another patient.
     *
     * @param phone     the phone number
     * @param patientId id of the patient to ignore
     * @return true if another patient uses it
     */
    boolean existsByPhoneAndPatientIdNot(String phone, int patientId);

    /**
     * Finds the patient of a login account.
     *
     * @param userId id of the user in auth-service
     * @return the patient, if any
     */
    Optional<Patient> findByUserId(int userId);

    /**
     * Lists patients with a status.
     *
     * @param status the status
     * @return the patients
     */
    List<Patient> findByStatus(AccountStatus status);
}
