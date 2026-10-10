package com.hospital.hospitalservice.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.common.enums.AccountStatus;
import com.hospital.common.exception.ConflictException;
import com.hospital.common.exception.ForbiddenException;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.hospitalservice.dto.PatientRequest;
import com.hospital.hospitalservice.dto.PatientResponse;
import com.hospital.hospitalservice.entity.Patient;
import com.hospital.hospitalservice.repository.PatientRepository;

/**
 * Business logic for patients.
 */
@Service
public class PatientService {

    private final PatientRepository patientRepository;

    /**
     * Creates the service.
     *
     * @param patientRepository database access for patients
     */
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    /**
     * Creates a patient without a login account (added by staff). The status is PENDING,
     * like in the old project, until the patient is activated.
     *
     * @param request the patient data
     * @return the saved patient
     * @throws BadRequestException if the data is wrong
     * @throws ConflictException   if the phone or email is already used
     */
    @Transactional
    public PatientResponse addPatient(PatientRequest request) {
        String name = Validation.clean(request.name());
        String phone = Validation.clean(request.phone());
        String email = Validation.clean(request.email());
        Validation.patient(name, request.dob(), request.gender(), phone, email);
        if (patientRepository.existsByEmail(email)) {
            throw new ConflictException("Patient with email already exists: " + email);
        }
        if (patientRepository.existsByPhone(phone)) {
            throw new ConflictException("Patient with phone already exists: " + phone);
        }
        Patient patient = new Patient(null, name, request.dob(), request.gender(), phone,
                email, AccountStatus.PENDING);
        return PatientResponse.from(patientRepository.save(patient));
    }

    /**
     * Changes the personal data of a patient. The status is not changed here.
     *
     * @param patientId id of the patient
     * @param request   the new data
     * @return the saved patient
     * @throws ResourceNotFoundException if the patient does not exist
     * @throws BadRequestException       if the data is wrong
     * @throws ConflictException         if the phone or email is used by another patient
     */
    @Transactional
    public PatientResponse updatePatient(int patientId, PatientRequest request) {
        return update(find(patientId), request);
    }

    /**
     * Changes the personal data of the patient who owns the given login account.
     *
     * @param userId  id of the logged-in user
     * @param request the new data
     * @return the saved patient
     * @throws ForbiddenException  if the user has no patient profile
     * @throws BadRequestException if the data is wrong
     * @throws ConflictException   if the phone or email is used by another patient
     */
    @Transactional
    public PatientResponse updateOwnPatient(int userId, PatientRequest request) {
        return update(findOwn(userId), request);
    }

    /**
     * Sets a patient to INACTIVE.
     *
     * @param patientId id of the patient
     * @throws ResourceNotFoundException if the patient does not exist
     */
    @Transactional
    public void deactivatePatient(int patientId) {
        Patient patient = find(patientId);
        patient.setStatus(AccountStatus.INACTIVE);
        patientRepository.save(patient);
    }

    /**
     * Sets a patient to ACTIVE.
     *
     * @param patientId id of the patient
     * @throws ResourceNotFoundException if the patient does not exist
     */
    @Transactional
    public void activatePatient(int patientId) {
        Patient patient = find(patientId);
        patient.setStatus(AccountStatus.ACTIVE);
        patientRepository.save(patient);
    }

    /**
     * Returns one patient.
     *
     * @param patientId id of the patient
     * @return the patient
     * @throws ResourceNotFoundException if the patient does not exist
     */
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(int patientId) {
        return PatientResponse.from(find(patientId));
    }

    /**
     * Returns the patient who owns the given login account.
     *
     * @param userId id of the logged-in user
     * @return the patient
     * @throws ForbiddenException if the user has no patient profile
     */
    @Transactional(readOnly = true)
    public PatientResponse getOwnPatient(int userId) {
        return PatientResponse.from(findOwn(userId));
    }

    /**
     * Returns the id of the patient who owns the given login account.
     *
     * @param userId id of the logged-in user
     * @return the patient id
     * @throws ForbiddenException if the user has no patient profile
     */
    @Transactional(readOnly = true)
    public int getOwnPatientId(int userId) {
        return findOwn(userId).getPatientId();
    }

    /**
     * Returns all patients.
     *
     * @return the patients
     */
    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll().stream().map(PatientResponse::from).toList();
    }

    /**
     * Returns the patients with a status.
     *
     * @param status the status
     * @return the patients
     */
    @Transactional(readOnly = true)
    public List<PatientResponse> getPatientsByStatus(AccountStatus status) {
        return patientRepository.findByStatus(status).stream()
                .map(PatientResponse::from)
                .toList();
    }

    private PatientResponse update(Patient patient, PatientRequest request) {
        String name = Validation.clean(request.name());
        String phone = Validation.clean(request.phone());
        String email = Validation.clean(request.email());
        Validation.patient(name, request.dob(), request.gender(), phone, email);
        int patientId = patient.getPatientId();
        if (patientRepository.existsByEmailAndPatientIdNot(email, patientId)) {
            throw new ConflictException("Patient with email already exists: " + email);
        }
        if (patientRepository.existsByPhoneAndPatientIdNot(phone, patientId)) {
            throw new ConflictException("Patient with phone already exists: " + phone);
        }
        patient.setName(name);
        patient.setDob(request.dob());
        patient.setGender(request.gender());
        patient.setPhone(phone);
        patient.setEmail(email);
        return PatientResponse.from(patientRepository.save(patient));
    }

    private Patient find(int patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with ID: " + patientId));
    }

    private Patient findOwn(int userId) {
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException(
                        "No patient profile linked to your account."));
    }
}
