package com.hospital.hospitalservice.dto;

import com.hospital.common.enums.AddressType;

/**
 * Data to create or update a patient address.
 *
 * @param patientId   id of the patient (needed for staff; ignored for a patient's own calls)
 * @param state       state
 * @param district    district
 * @param pincode     postal code
 * @param addressType HOME, WORK, BILLING or EMERGENCY (HOME if not sent)
 */
public record AddressRequest(Integer patientId, String state, String district, String pincode,
                             AddressType addressType) {
}
