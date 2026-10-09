package com.hospital.hospitalservice.repository;

import com.hospital.common.enums.AddressType;
import com.hospital.hospitalservice.entity.PatientAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Database access for patient addresses.
 */
public interface PatientAddressRepository extends JpaRepository<PatientAddress, Integer> {

    /**
     * Lists the addresses of a patient, ordered by address type.
     *
     * @param patientId id of the patient
     * @return the addresses
     */
    List<PatientAddress> findByPatientIdOrderByAddressType(int patientId);

    /**
     * Finds the first address of a patient with a given type.
     *
     * @param patientId   id of the patient
     * @param addressType the type
     * @return the address, if any
     */
    Optional<PatientAddress> findFirstByPatientIdAndAddressTypeOrderByAddressIdAsc(
            int patientId, AddressType addressType);

    /**
     * Finds the first address of a patient, whatever the type.
     *
     * @param patientId id of the patient
     * @return the address, if any
     */
    Optional<PatientAddress> findFirstByPatientIdOrderByAddressIdAsc(int patientId);

    /**
     * Checks if a patient already has an address of a type.
     *
     * @param patientId   id of the patient
     * @param addressType the type
     * @return true if one exists
     */
    boolean existsByPatientIdAndAddressType(int patientId, AddressType addressType);

    /**
     * Checks if a patient has another address of a type.
     *
     * @param patientId   id of the patient
     * @param addressType the type
     * @param addressId   id of the address to ignore
     * @return true if another one exists
     */
    boolean existsByPatientIdAndAddressTypeAndAddressIdNot(
            int patientId, AddressType addressType, int addressId);

    /**
     * Deletes all addresses of a patient.
     *
     * @param patientId id of the patient
     */
    void deleteByPatientId(int patientId);
}
