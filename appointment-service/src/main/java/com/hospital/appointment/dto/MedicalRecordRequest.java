package com.hospital.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request to create a medical record.
 *
 * @param appointmentId  id of the appointment
 * @param diagnosis      diagnosis text
 * @param treatmentNotes notes for treatment
 * @param recordDate     date of the record
 */
public record MedicalRecordRequest(@NotNull(message = "appointmentId is required") Integer appointmentId,
                                  @NotBlank(message = "diagnosis is required") String diagnosis,
                                  @NotBlank(message = "treatmentNotes is required") String treatmentNotes,
                                  @NotNull(message = "recordDate is required") LocalDate recordDate) {
}
