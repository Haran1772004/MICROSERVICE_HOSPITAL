package com.hospital.appointment.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospital.appointment.entity.MedicalRecord;

/**
 * Data access for medical records.
 */
@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Integer> {

    /**
     * Finds a medical record by its ID.
     *
     * @param recordId the record ID
     * @return the record, if found
     */
    Optional<MedicalRecord> findByRecordId(int recordId);

    /**
     * Finds the medical record for an appointment.
     *
     * @param appointmentId the appointment ID
     * @return the record, if found
     */
    Optional<MedicalRecord> findByAppointmentId(int appointmentId);

    /**
     * Checks whether a medical record exists for an appointment.
     *
     * @param appointmentId the appointment ID
     * @return {@code true} if a record exists
     */
    boolean existsByAppointmentId(int appointmentId);

    /**
     * Returns records for the specified appointments.
     *
     * @param appointmentIds the appointment IDs
     * @return matching medical records
     */
    List<MedicalRecord> findByAppointmentIdIn(Collection<Integer> appointmentIds);

    /**
     * Returns all records ordered from newest to oldest.
     *
     * @return all records in descending date order
     */
    List<MedicalRecord> findAllByOrderByRecordDateDescRecordIdDesc();
}
