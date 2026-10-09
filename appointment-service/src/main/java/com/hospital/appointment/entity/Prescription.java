package com.hospital.appointment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A prescription attached to a medical record.
 */
@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id")
    private Integer prescriptionId;

    @Column(name = "record_id", nullable = false)
    private int recordId;

    @Column(name = "medicine_name", nullable = false, length = 150)
    private String medicineName;

    @Column(nullable = false, length = 100)
    private String dosage;

    @Column(nullable = false, length = 100)
    private String duration;

    /**
     * Default constructor for JPA.
     */
    public Prescription() {
    }

    /**
     * Creates a prescription.
     *
     * @param recordId     id of the medical record
     * @param medicineName name of the medicine
     * @param dosage      dosage description
     * @param duration    treatment duration
     */
    public Prescription(int recordId, String medicineName, String dosage, String duration) {
        this.recordId = recordId;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.duration = duration;
    }

    public Integer getPrescriptionId() {
        return prescriptionId;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
