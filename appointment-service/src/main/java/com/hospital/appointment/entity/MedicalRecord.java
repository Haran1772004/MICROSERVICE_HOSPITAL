package com.hospital.appointment.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    /**
     * Returns the medical record ID.
     *
     * @return the record ID
     */
    public Integer getRecordId() {
        return recordId;
    }

    /**
     * Returns the ID of the associated appointment.
     *
     * @return the appointment ID
     */
    public int getAppointmentId() {
        return appointmentId;
    }

    /**
     * Sets the ID of the associated appointment.
     *
     * @param appointmentId the appointment ID
     */
    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    /**
     * Returns the diagnosis.
     *
     * @return the diagnosis
     */
    public String getDiagnosis() {
        return diagnosis;
    }

    /**
     * Sets the diagnosis.
     *
     * @param diagnosis the diagnosis
     */
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    /**
     * Returns the treatment notes.
     *
     * @return the treatment notes
     */
    public String getTreatmentNotes() {
        return treatmentNotes;
    }

    /**
     * Sets the treatment notes.
     *
     * @param treatmentNotes the treatment notes
     */
    public void setTreatmentNotes(String treatmentNotes) {
        this.treatmentNotes = treatmentNotes;
    }

    /**
     * Returns the medical record date.
     *
     * @return the record date
     */
    public LocalDate getRecordDate() {
        return recordDate;
    }

    /**
     * Sets the medical record date.
     *
     * @param recordDate the record date
     */
    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }
}
