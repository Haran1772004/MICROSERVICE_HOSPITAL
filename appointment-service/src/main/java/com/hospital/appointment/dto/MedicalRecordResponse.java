package com.hospital.appointment.dto;

import com.hospital.appointment.entity.MedicalRecord;

import java.time.LocalDate;

/**
 * Response body for a medical record.
 *
 * @param recordId        id of the record
 * @param appointmentId   id of the appointment
 * @param patientId       id of the patient
 * @param doctorId        id of the doctor
 * @param diagnosis       diagnosis text
 * @param treatmentNotes  treatment notes
 * @param recordDate      date of the record
 */
public record MedicalRecordResponse(int recordId, int appointmentId, int patientId, int doctorId,
                                   String diagnosis, String treatmentNotes, LocalDate recordDate) {

    /**
     * Builds a response from an entity and appointment context.
     *
     * @param record     the record entity
     * @param patientId  patient id from the appointment
     * @param doctorId   doctor id from the appointment
     * @return the response data
     */
    public static MedicalRecordResponse from(MedicalRecord record, int patientId, int doctorId) {
        return new MedicalRecordResponse(record.getRecordId(), record.getAppointmentId(),
                patientId, doctorId, record.getDiagnosis(), record.getTreatmentNotes(),
                record.getRecordDate());
    }
}
