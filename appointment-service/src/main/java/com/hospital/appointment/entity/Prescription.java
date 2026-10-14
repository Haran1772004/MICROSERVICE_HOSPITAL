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

    private static final int MEDICINE_NAME_COLUMN_LENGTH = 150;
    private static final int DOSAGE_COLUMN_LENGTH = 100;
    private static final int DURATION_COLUMN_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id")
    private Integer prescriptionId;

    @Column(name = "record_id", nullable = false)
    private int recordId;

    @Column(name = "medicine_name", nullable = false, length = MEDICINE_NAME_COLUMN_LENGTH)
    private String medicineName;

    @Column(nullable = false, length = DOSAGE_COLUMN_LENGTH)
    private String dosage;

    @Column(nullable = false, length = DURATION_COLUMN_LENGTH)
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

    /**
     * Returns the prescription ID.
     *
     * @return the prescription ID
     */
    public Integer getPrescriptionId() {
        return prescriptionId;
    }

    /**
     * Returns the ID of the associated medical record.
     *
     * @return the medical record ID
     */
    public int getRecordId() {
        return recordId;
    }

    /**
     * Sets the ID of the associated medical record.
     *
     * @param recordId the medical record ID
     */
    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    /**
     * Returns the medicine name.
     *
     * @return the medicine name
     */
    public String getMedicineName() {
        return medicineName;
    }

    /**
     * Sets the medicine name.
     *
     * @param medicineName the medicine name
     */
    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    /**
     * Returns the dosage instructions.
     *
     * @return the dosage instructions
     */
    public String getDosage() {
        return dosage;
    }

    /**
     * Sets the dosage instructions.
     *
     * @param dosage the dosage instructions
     */
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    /**
     * Returns the treatment duration.
     *
     * @return the treatment duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     * Sets the treatment duration.
     *
     * @param duration the treatment duration
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }
}
