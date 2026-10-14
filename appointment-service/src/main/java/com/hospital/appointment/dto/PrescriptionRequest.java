package com.hospital.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request to create a prescription.
 *
 * @param recordId      id of the medical record
 * @param medicineName medicine name
 * @param dosage       dosage description
 * @param duration     duration description
 */
public record PrescriptionRequest(
        @NotNull(message = "recordId is required") Integer recordId,
        @NotBlank(message = "medicineName is required") String medicineName,
        @NotBlank(message = "dosage is required") String dosage,
        @NotBlank(message = "duration is required") String duration) {
}
