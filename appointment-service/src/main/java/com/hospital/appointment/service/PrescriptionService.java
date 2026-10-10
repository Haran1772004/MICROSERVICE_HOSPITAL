package com.hospital.appointment.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hospital.appointment.dto.PrescriptionRequest;
import com.hospital.appointment.dto.PrescriptionResponse;
import com.hospital.appointment.entity.Prescription;
import com.hospital.appointment.repository.MedicalRecordRepository;
import com.hospital.appointment.repository.PrescriptionRepository;
import com.hospital.common.exception.ResourceNotFoundException;

/**
 * Business logic for prescriptions.
 */
@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    /**
     * Creates the service.
     *
     * @param prescriptionRepository repository for prescriptions
     * @param medicalRecordRepository repository for medical records
     */
    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                              MedicalRecordRepository medicalRecordRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    /**
     * Creates a prescription.
     *
     * @param request the request
     * @return the created prescription
     */
    public PrescriptionResponse createPrescription(PrescriptionRequest request) {
        medicalRecordRepository.findByRecordId(request.recordId())
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found."));

        Prescription prescription = new Prescription(
                request.recordId(),
                request.medicineName().trim(),
                request.dosage().trim(),
                request.duration().trim());
        return PrescriptionResponse.from(prescriptionRepository.save(prescription));
    }

    /**
     * Reads all prescriptions for one medical record.
     *
     * @param recordId id of the record
     * @return list of prescriptions
     */
    public List<PrescriptionResponse> getPrescriptionsByRecord(int recordId) {
        medicalRecordRepository.findByRecordId(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found."));
        return prescriptionRepository.findByRecordIdOrderByPrescriptionIdAsc(recordId).stream()
                .map(PrescriptionResponse::from)
                .toList();
    }
}
