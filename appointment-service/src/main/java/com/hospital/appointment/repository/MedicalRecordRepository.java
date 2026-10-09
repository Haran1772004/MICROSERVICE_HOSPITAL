package com.hospital.appointment.repository;

import com.hospital.appointment.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Data access for medical records.
 */
@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Integer> {

    Optional<MedicalRecord> findByRecordId(int recordId);

    Optional<MedicalRecord> findByAppointmentId(int appointmentId);

    boolean existsByAppointmentId(int appointmentId);

    List<MedicalRecord> findByAppointmentIdIn(Collection<Integer> appointmentIds);

    List<MedicalRecord> findAllByOrderByRecordDateDescRecordIdDesc();
}
