package com.hospital.appointment.dto;

import com.hospital.appointment.entity.Prescription;

/**
 * Response body for a prescription.
 *
 * @param prescriptionId id of the prescription
 * @param recordId       id of the medical record
 * @param medicineName   medicine name
 * @param dosage         dosage description
 * @param duration       duration description
 */
public record PrescriptionResponse(int prescriptionId, int recordId, String medicineName,
                                  String dosage, String duration) {

    /**
     * Builds a response from an entity.
     *
     * @param prescription the entity
     * @return the response data
     */
    public static PrescriptionResponse from(Prescription prescription) {
        return new PrescriptionResponse(
                prescription.getPrescriptionId(),
                prescription.getRecordId(),
                prescription.getMedicineName(),
                prescription.getDosage(),
                prescription.getDuration());
    }
}
