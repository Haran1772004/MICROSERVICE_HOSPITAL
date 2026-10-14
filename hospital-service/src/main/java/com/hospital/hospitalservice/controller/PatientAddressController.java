package com.hospital.hospitalservice.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.common.enums.Role;
import com.hospital.common.exception.BadRequestException;
import com.hospital.common.exception.ForbiddenException;
import com.hospital.common.security.JwtUser;
import com.hospital.hospitalservice.dto.AddressRequest;
import com.hospital.hospitalservice.dto.AddressResponse;
import com.hospital.hospitalservice.security.CurrentUser;
import com.hospital.hospitalservice.service.PatientAddressService;
import com.hospital.hospitalservice.service.PatientService;

/**
 * Endpoints for patient addresses. Staff can manage the addresses of any patient. A patient
 * can only manage the own addresses.
 */
@RestController
@RequestMapping("/patient-addresses")
public class PatientAddressController {

    private static final String ONLY_OWN = "Access denied: you can only manage your own addresses.";

    private final PatientAddressService addressService;
    private final PatientService patientService;

    /**
     * Creates the controller.
     *
     * @param addressService business logic for addresses
     * @param patientService business logic for patients (to find the own patient)
     */
    public PatientAddressController(PatientAddressService addressService,
                                    PatientService patientService) {
        this.addressService = addressService;
        this.patientService = patientService;
    }

    /**
     * Adds an address. Staff must send {@code patientId}. A patient may leave it out.
     *
     * @param request the address data
     * @return the saved address
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    @PostMapping
    public AddressResponse addAddress(@RequestBody AddressRequest request) {
        JwtUser user = CurrentUser.require();
        if (user.role() == Role.PATIENT) {
            int ownId = patientService.getOwnPatientId(user.userId());
            if (request.patientId() != null && request.patientId() != ownId) {
                throw new ForbiddenException(ONLY_OWN);
            }
            return addressService.addAddress(ownId, request);
        }
        if (request.patientId() == null || request.patientId() <= 0) {
            throw new BadRequestException("Valid patient ID is required");
        }
        return addressService.addAddress(request.patientId(), request);
    }

    /**
     * Adds an address to the logged-in patient.
     *
     * @param request the address data
     * @return the saved address
     */
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/me")
    public AddressResponse addMyAddress(@RequestBody AddressRequest request) {
        int ownId = patientService.getOwnPatientId(CurrentUser.require().userId());
        return addressService.addAddress(ownId, request);
    }

    /**
     * Changes an address.
     *
     * @param addressId id of the address
     * @param request   the new data
     * @return the saved address
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    @PutMapping("/{addressId}")
    public AddressResponse updateAddress(@PathVariable int addressId,
                                         @RequestBody AddressRequest request) {
        return addressService.updateAddress(addressId, request, onlyOwnPatientId());
    }

    /**
     * Deletes an address.
     *
     * @param addressId id of the address
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    @DeleteMapping("/{addressId}")
    public void removeAddress(@PathVariable int addressId) {
        addressService.removeAddress(addressId, onlyOwnPatientId());
    }

    /**
     * Returns the addresses of the logged-in patient.
     *
     * @return the addresses
     */
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me")
    public List<AddressResponse> getMyAddresses() {
        int ownId = patientService.getOwnPatientId(CurrentUser.require().userId());
        return addressService.getAddressesByPatient(ownId);
    }

    /**
     * Returns the default address of the logged-in patient.
     *
     * @return the default address, or an empty answer if there is none
     */
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me/default")
    public AddressResponse getMyDefaultAddress() {
        int ownId = patientService.getOwnPatientId(CurrentUser.require().userId());
        return addressService.getDefaultAddress(ownId);
    }

    /**
     * Returns one address.
     *
     * @param addressId id of the address
     * @return the address
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    @GetMapping("/{addressId}")
    public AddressResponse getAddressById(@PathVariable int addressId) {
        return addressService.getAddressById(addressId, onlyOwnPatientId());
    }

    /**
     * Returns the addresses of a patient.
     *
     * @param patientId id of the patient
     * @return the addresses
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    @GetMapping("/patient/{patientId}")
    public List<AddressResponse> getAddressesByPatient(@PathVariable int patientId) {
        checkOwnPatient(patientId);
        return addressService.getAddressesByPatient(patientId);
    }

    /**
     * Returns the default address of a patient.
     *
     * @param patientId id of the patient
     * @return the default address, or an empty answer if there is none
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    @GetMapping("/patient/{patientId}/default")
    public AddressResponse getDefaultAddress(@PathVariable int patientId) {
        checkOwnPatient(patientId);
        return addressService.getDefaultAddress(patientId);
    }

    /**
     * Returns all addresses.
     *
     * @return all addresses
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @GetMapping
    public List<AddressResponse> getAllAddresses() {
        return addressService.getAllAddresses();
    }

    /**
     * Returns the logged-in patient's ID, or {@code null} for staff.
     *
     * @return the patient's ID, or {@code null} for staff
     */
    private Integer onlyOwnPatientId() {
        JwtUser user = CurrentUser.require();
        if (user.role() == Role.PATIENT) {
            return patientService.getOwnPatientId(user.userId());
        }
        return null;
    }

    /**
     * Checks that a patient uses their own ID. Staff may use any patient ID.
     *
     * @param patientId the ID to check
     */
    private void checkOwnPatient(int patientId) {
        Integer ownId = onlyOwnPatientId();
        if (ownId != null && ownId != patientId) {
            throw new ForbiddenException(ONLY_OWN);
        }
    }
}
