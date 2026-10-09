package com.hospital.appointment.repository;

import com.hospital.appointment.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access for prescriptions.
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Integer> {

    List<Prescription> findByRecordIdOrderByPrescriptionIdAsc(int recordId);

    Optional<Prescription> findByPrescriptionId(int prescriptionId);
}
