package com.hospital.hospitalservice.repository;

import com.hospital.common.enums.AccountStatus;
import com.hospital.hospitalservice.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Database access for doctors.
 */
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

    /**
     * Checks if an email is already used by a doctor.
     *
     * @param email the email
     * @return true if used
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a phone number is already used by a doctor.
     *
     * @param phone the phone number
     * @return true if used
     */
    boolean existsByPhone(String phone);

    /**
     * Checks if an email is used by another doctor.
     *
     * @param email    the email
     * @param doctorId id of the doctor to ignore
     * @return true if another doctor uses it
     */
    boolean existsByEmailAndDoctorIdNot(String email, int doctorId);

    /**
     * Checks if a phone number is used by another doctor.
     *
     * @param phone    the phone number
     * @param doctorId id of the doctor to ignore
     * @return true if another doctor uses it
     */
    boolean existsByPhoneAndDoctorIdNot(String phone, int doctorId);

    /**
     * Finds the doctor of a login account.
     *
     * @param userId id of the user in auth-service
     * @return the doctor, if any
     */
    Optional<Doctor> findByUserId(int userId);

    /**
     * Lists doctors with a status.
     *
     * @param status the status
     * @return the doctors
     */
    List<Doctor> findByStatus(AccountStatus status);
}
