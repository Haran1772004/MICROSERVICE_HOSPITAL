package com.hospital.appointment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Medical record created after a consultation.
 */
@Entity
@Table(name = "medical_records")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Integer recordId;

    @Column(name = "appointment_id", nullable = false, unique = true)
    private int appointmentId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnosis;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String treatmentNotes;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    /**
     * Default constructor for JPA.
     */
    public MedicalRecord() {
    }

    /**
     * Creates a record.
     *
     * @param appointmentId  id of the appointment
     * @param diagnosis      diagnosis text
     * @param treatmentNotes notes for the treatment
     * @param recordDate     date of the record
     */
    public MedicalRecord(int appointmentId, String diagnosis, String treatmentNotes,
                        LocalDate recordDate) {
        this.appointmentId = appointmentId;
        this.diagnosis = diagnosis;
        this.treatmentNotes = treatmentNotes;
        this.recordDate = recordDate;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatmentNotes() {
        return treatmentNotes;
    }

    public void setTreatmentNotes(String treatmentNotes) {
        this.treatmentNotes = treatmentNotes;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }
}
