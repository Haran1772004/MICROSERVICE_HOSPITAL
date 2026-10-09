package com.hospital.hospitalservice.entity;

import com.hospital.common.enums.AddressType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * An address of a patient. A patient can have one address of each type.
 * This entity is never returned from the API; use {@code AddressResponse}.
 */
@Entity
@Table(name = "patient_addresses")
public class PatientAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private int addressId;

    @Column(name = "patient_id", nullable = false)
    private int patientId;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "district", nullable = false, length = 100)
    private String district;

    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 9)
    private AddressType addressType;

    /**
     * Creates an empty address. Required by JPA.
     */
    protected PatientAddress() {
    }

    /**
     * Creates a new address that is not saved yet.
     *
     * @param patientId   id of the patient who owns the address
     * @param state       state
     * @param district    district
     * @param pincode     postal code
     * @param addressType type of the address
     */
    public PatientAddress(int patientId, String state, String district, String pincode,
                          AddressType addressType) {
        this.patientId = patientId;
        this.state = state;
        this.district = district;
        this.pincode = pincode;
        this.addressType = addressType;
    }

    /**
     * Returns the id.
     *
     * @return the address id
     */
    public int getAddressId() {
        return addressId;
    }

    /**
     * Returns the id of the patient.
     *
     * @return the patient id
     */
    public int getPatientId() {
        return patientId;
    }

    /**
     * Returns the state.
     *
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state.
     *
     * @param state the new state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Returns the district.
     *
     * @return the district
     */
    public String getDistrict() {
        return district;
    }

    /**
     * Sets the district.
     *
     * @param district the new district
     */
    public void setDistrict(String district) {
        this.district = district;
    }

    /**
     * Returns the postal code.
     *
     * @return the pincode
     */
    public String getPincode() {
        return pincode;
    }

    /**
     * Sets the postal code.
     *
     * @param pincode the new pincode
     */
    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    /**
     * Returns the address type.
     *
     * @return the address type
     */
    public AddressType getAddressType() {
        return addressType;
    }

    /**
     * Sets the address type.
     *
     * @param addressType the new address type
     */
    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }
}
