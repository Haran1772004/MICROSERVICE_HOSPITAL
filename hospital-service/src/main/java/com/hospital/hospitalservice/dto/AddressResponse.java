package com.hospital.hospitalservice.dto;

import com.hospital.common.enums.AddressType;
import com.hospital.hospitalservice.entity.PatientAddress;

/**
 * A patient address as sent to the caller.
 *
 * @param addressId   id of the address
 * @param patientId   id of the patient
 * @param state       state
 * @param district    district
 * @param pincode     postal code
 * @param addressType type of the address
 */
public record AddressResponse(int addressId, int patientId, String state, String district,
                              String pincode, AddressType addressType) {

    /**
     * Builds the response from an entity.
     *
     * @param address the address
     * @return the response
     */
    public static AddressResponse from(PatientAddress address) {
        return new AddressResponse(address.getAddressId(), address.getPatientId(),
                address.getState(), address.getDistrict(), address.getPincode(),
                address.getAddressType());
    }
}
