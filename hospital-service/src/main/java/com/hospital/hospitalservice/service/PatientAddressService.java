package com.hospital.hospitalservice.service;

import com.hospital.common.enums.AddressType;
import com.hospital.common.exception.BadRequestException;
import com.hospital.common.exception.ConflictException;
import com.hospital.common.exception.ForbiddenException;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.hospitalservice.dto.AddressRequest;
import com.hospital.hospitalservice.dto.AddressResponse;
import com.hospital.hospitalservice.entity.PatientAddress;
import com.hospital.hospitalservice.repository.PatientAddressRepository;
import com.hospital.hospitalservice.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for patient addresses.
 *
 * <p>Some methods take {@code onlyPatientId}. For staff it is {@code null}. For a patient it
 * is the patient's own id, and the patient can then only use own addresses.
 */
@Service
public class PatientAddressService {

    private static final String NOT_YOURS = "Address not found or access denied.";

    private final PatientAddressRepository addressRepository;
    private final PatientRepository patientRepository;

    /**
     * Creates the service.
     *
     * @param addressRepository database access for addresses
     * @param patientRepository database access for patients
     */
    public PatientAddressService(PatientAddressRepository addressRepository,
                                 PatientRepository patientRepository) {
        this.addressRepository = addressRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Adds an address to a patient.
     *
     * @param patientId id of the patient
     * @param request   the address data (HOME if no type is sent)
     * @return the saved address
     * @throws BadRequestException       if the data is wrong
     * @throws ResourceNotFoundException if the patient does not exist
     * @throws ConflictException         if the patient already has an address of this type
     */
    @Transactional
    public AddressResponse addAddress(int patientId, AddressRequest request) {
        String state = Validation.clean(request.state());
        String district = Validation.clean(request.district());
        String pincode = Validation.clean(request.pincode());
        Validation.address(state, district, pincode);
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
        }
        AddressType type = typeOrHome(request);
        if (addressRepository.existsByPatientIdAndAddressType(patientId, type)) {
            throw new ConflictException("Patient already has an address of type: " + type);
        }
        PatientAddress address = new PatientAddress(patientId, state, district, pincode, type);
        return AddressResponse.from(addressRepository.save(address));
    }

    /**
     * Changes an address. The owner of the address cannot be changed.
     *
     * @param addressId     id of the address
     * @param request       the new data
     * @param onlyPatientId id of the patient who may use it, or null for staff
     * @return the saved address
     * @throws ResourceNotFoundException if the address does not exist
     * @throws ForbiddenException        if it belongs to another patient
     * @throws BadRequestException       if the data is wrong
     * @throws ConflictException         if the patient already has another address of this type
     */
    @Transactional
    public AddressResponse updateAddress(int addressId, AddressRequest request,
                                         Integer onlyPatientId) {
        PatientAddress address = findFor(addressId, onlyPatientId);
        String state = Validation.clean(request.state());
        String district = Validation.clean(request.district());
        String pincode = Validation.clean(request.pincode());
        Validation.address(state, district, pincode);
        AddressType type = typeOrHome(request);
        if (addressRepository.existsByPatientIdAndAddressTypeAndAddressIdNot(
                address.getPatientId(), type, addressId)) {
            throw new ConflictException("Patient already has an address of type: " + type);
        }
        address.setState(state);
        address.setDistrict(district);
        address.setPincode(pincode);
        address.setAddressType(type);
        return AddressResponse.from(addressRepository.save(address));
    }

    /**
     * Deletes an address.
     *
     * @param addressId     id of the address
     * @param onlyPatientId id of the patient who may use it, or null for staff
     * @throws ResourceNotFoundException if the address does not exist
     * @throws ForbiddenException        if it belongs to another patient
     */
    @Transactional
    public void removeAddress(int addressId, Integer onlyPatientId) {
        PatientAddress address = findFor(addressId, onlyPatientId);
        addressRepository.delete(address);
    }

    /**
     * Returns one address.
     *
     * @param addressId     id of the address
     * @param onlyPatientId id of the patient who may use it, or null for staff
     * @return the address
     * @throws ResourceNotFoundException if the address does not exist
     * @throws ForbiddenException        if it belongs to another patient
     */
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(int addressId, Integer onlyPatientId) {
        return AddressResponse.from(findFor(addressId, onlyPatientId));
    }

    /**
     * Returns the addresses of a patient.
     *
     * @param patientId id of the patient
     * @return the addresses, ordered by type
     */
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddressesByPatient(int patientId) {
        return addressRepository.findByPatientIdOrderByAddressType(patientId).stream()
                .map(AddressResponse::from)
                .toList();
    }

    /**
     * Returns all addresses.
     *
     * @return all addresses
     */
    @Transactional(readOnly = true)
    public List<AddressResponse> getAllAddresses() {
        return addressRepository.findAll().stream().map(AddressResponse::from).toList();
    }

    /**
     * Returns the default address of a patient: the HOME address, or else the first one.
     *
     * @param patientId id of the patient
     * @return the address, or null if the patient has none
     */
    @Transactional(readOnly = true)
    public AddressResponse getDefaultAddress(int patientId) {
        return addressRepository
                .findFirstByPatientIdAndAddressTypeOrderByAddressIdAsc(patientId,
                        AddressType.HOME)
                .or(() -> addressRepository.findFirstByPatientIdOrderByAddressIdAsc(patientId))
                .map(AddressResponse::from)
                .orElse(null);
    }

    private PatientAddress findFor(int addressId, Integer onlyPatientId) {
        PatientAddress address = addressRepository.findById(addressId).orElse(null);
        if (onlyPatientId != null
                && (address == null || address.getPatientId() != onlyPatientId)) {
            throw new ForbiddenException(NOT_YOURS);
        }
        if (address == null) {
            throw new ResourceNotFoundException("Address not found with ID: " + addressId);
        }
        return address;
    }

    private AddressType typeOrHome(AddressRequest request) {
        return request.addressType() == null ? AddressType.HOME : request.addressType();
    }
}
