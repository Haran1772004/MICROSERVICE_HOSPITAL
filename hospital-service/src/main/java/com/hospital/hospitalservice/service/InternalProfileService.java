package com.hospital.hospitalservice.service;

import com.hospital.common.enums.AccountStatus;
import com.hospital.common.enums.AddressType;
import com.hospital.common.exception.BadRequestException;
import com.hospital.common.exception.ConflictException;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.hospitalservice.dto.CreateDoctorProfileRequest;
import com.hospital.hospitalservice.dto.CreatePatientProfileRequest;
import com.hospital.hospitalservice.dto.DoctorInfo;
import com.hospital.hospitalservice.dto.PatientInfo;
import com.hospital.hospitalservice.entity.Department;
import com.hospital.hospitalservice.entity.Doctor;
import com.hospital.hospitalservice.entity.Patient;
import com.hospital.hospitalservice.entity.PatientAddress;
import com.hospital.hospitalservice.repository.DepartmentRepository;
import com.hospital.hospitalservice.repository.DoctorRepository;
import com.hospital.hospitalservice.repository.PatientAddressRepository;
import com.hospital.hospitalservice.repository.PatientRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Work for the {@code /internal} endpoints, which other services call: create a profile after
 * a registration, undo it, and look up a patient or doctor.
 */
@Service
public class InternalProfileService {

    private final PatientRepository patientRepository;
    private final PatientAddressRepository addressRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Creates the service.
     *
     * @param patientRepository    database access for patients
     * @param addressRepository    database access for addresses
     * @param doctorRepository     database access for doctors
     * @param departmentRepository database access for departments
     */
    public InternalProfileService(PatientRepository patientRepository,
                                  PatientAddressRepository addressRepository,
                                  DoctorRepository doctorRepository,
                                  DepartmentRepository departmentRepository) {
        this.patientRepository = patientRepository;
        this.addressRepository = addressRepository;
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Creates an ACTIVE patient and its first address in one transaction.
     *
     * @param request the data sent by auth-service
     * @return the short patient data
     * @throws BadRequestException if the data is wrong
     * @throws ConflictException   if the phone, email or user already has a profile
     */
    @Transactional
    public PatientInfo createPatient(CreatePatientProfileRequest request) {
        requireUserId(request.userId());
        String name = Validation.clean(request.name());
        String phone = Validation.clean(request.phone());
        String email = Validation.clean(request.email());
        String state = Validation.clean(request.state());
        String district = Validation.clean(request.district());
        String pincode = Validation.clean(request.pincode());
        Validation.patient(name, request.dob(), request.gender(), phone, email);
        Validation.address(state, district, pincode);

        if (patientRepository.findByUserId(request.userId()).isPresent()) {
            throw new ConflictException("A profile already exists for this user.");
        }
        if (patientRepository.existsByEmail(email)) {
            throw new ConflictException("Patient with email already exists: " + email);
        }
        if (patientRepository.existsByPhone(phone)) {
            throw new ConflictException("Patient with phone already exists: " + phone);
        }

        Patient patient = patientRepository.save(new Patient(request.userId(), name,
                request.dob(), request.gender(), phone, email, AccountStatus.ACTIVE));
        AddressType type = request.addressType() == null ? AddressType.HOME
                : request.addressType();
        addressRepository.save(new PatientAddress(patient.getPatientId(), state, district,
                pincode, type));
        return PatientInfo.from(patient);
    }

    /**
     * Creates a PENDING doctor. An admin must approve it later.
     *
     * @param request the data sent by auth-service
     * @return the short doctor data
     * @throws BadRequestException       if the data is wrong or the department is not active
     * @throws ResourceNotFoundException if the department does not exist
     * @throws ConflictException         if the phone, email or user already has a profile
     */
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public DoctorInfo createDoctor(CreateDoctorProfileRequest request) {
        requireUserId(request.userId());
        String name = Validation.clean(request.name());
        String specialization = Validation.clean(request.specialization());
        String phone = Validation.clean(request.phone());
        String email = Validation.clean(request.email());
        Validation.doctor(name, specialization, phone, email);

        int departmentId = request.departmentId() == null ? 0 : request.departmentId();
        if (departmentId <= 0) {
            throw new BadRequestException("Doctor must belong to a valid department");
        }
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + departmentId));
        if (department.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException(
                    "The selected department is not active. Please choose an active department.");
        }

        if (doctorRepository.findByUserId(request.userId()).isPresent()) {
            throw new ConflictException("A profile already exists for this user.");
        }
        if (doctorRepository.existsByEmail(email)) {
            throw new ConflictException("Doctor with email already exists: " + email);
        }
        if (doctorRepository.existsByPhone(phone)) {
            throw new ConflictException("Doctor with phone already exists: " + phone);
        }

        Doctor doctor = doctorRepository.save(new Doctor(request.userId(), name,
                specialization, phone, email, department, AccountStatus.PENDING));
        return DoctorInfo.from(doctor);
    }

    /**
     * Deletes the patient or doctor profile of a user. It does nothing if there is no
     * profile, so it is safe to call it to undo a failed registration.
     *
     * @param userId id of the user in auth-service
     */
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public void deleteProfileByUser(int userId) {
        patientRepository.findByUserId(userId).ifPresent(patient -> {
            addressRepository.deleteByPatientId(patient.getPatientId());
            patientRepository.delete(patient);
        });
        doctorRepository.findByUserId(userId).ifPresent(doctorRepository::delete);
    }

    /**
     * Returns short data of a patient.
     *
     * @param patientId id of the patient
     * @return the short data
     * @throws ResourceNotFoundException if the patient does not exist
     */
    @Transactional(readOnly = true)
    public PatientInfo getPatient(int patientId) {
        return patientRepository.findById(patientId)
                .map(PatientInfo::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with ID: " + patientId));
    }

    /**
     * Returns short data of the patient of a login account.
     *
     * @param userId id of the user in auth-service
     * @return the short data
     * @throws ResourceNotFoundException if the user has no patient profile
     */
    @Transactional(readOnly = true)
    public PatientInfo getPatientByUser(int userId) {
        return patientRepository.findByUserId(userId)
                .map(PatientInfo::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No patient profile linked to user ID: " + userId));
    }

    /**
     * Returns short data of a doctor.
     *
     * @param doctorId id of the doctor
     * @return the short data
     * @throws ResourceNotFoundException if the doctor does not exist
     */
    @Transactional(readOnly = true)
    public DoctorInfo getDoctor(int doctorId) {
        return doctorRepository.findById(doctorId)
                .map(DoctorInfo::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with ID: " + doctorId));
    }

    private void requireUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new BadRequestException("userId is required.");
        }
    }
}
