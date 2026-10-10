package com.hospital.appointment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospital.appointment.entity.Prescription;

/**
 * Data access for prescriptions.
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Integer> {

    /**
     * Returns a record's prescriptions ordered by ID.
     *
     * @param recordId the medical record ID
     * @return the prescriptions for that record
     */
    List<Prescription> findByRecordIdOrderByPrescriptionIdAsc(int recordId);

    /**
     * Finds a prescription by its ID.
     *
     * @param prescriptionId the prescription ID
     * @return the prescription, if found
     */
    Optional<Prescription> findByPrescriptionId(int prescriptionId);
}
