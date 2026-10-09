package com.hospital.hospitalservice.service;

import com.hospital.common.enums.AccountStatus;
import com.hospital.common.exception.BadRequestException;
import com.hospital.common.exception.ConflictException;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.hospitalservice.dto.DoctorRequest;
import com.hospital.hospitalservice.dto.DoctorResponse;
import com.hospital.hospitalservice.entity.Department;
import com.hospital.hospitalservice.entity.Doctor;
import com.hospital.hospitalservice.repository.DepartmentRepository;
import com.hospital.hospitalservice.repository.DoctorRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for doctors. Reads are cached in the cache {@code doctors}. Every change
 * clears this cache.
 */
@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Creates the service.
     *
     * @param doctorRepository     database access for doctors
     * @param departmentRepository database access for departments
     */
    public DoctorService(DoctorRepository doctorRepository,
                         DepartmentRepository departmentRepository) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Creates an ACTIVE doctor without a login account (added by an admin).
     *
     * @param request the doctor data
     * @return the saved doctor
     * @throws BadRequestException       if the data is wrong or the department is not active
     * @throws ResourceNotFoundException if the department does not exist
     * @throws ConflictException         if the phone or email is already used
     */
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public DoctorResponse addDoctor(DoctorRequest request) {
        String name = Validation.clean(request.name());
        String specialization = Validation.clean(request.specialization());
        String phone = Validation.clean(request.phone());
        String email = Validation.clean(request.email());
        Validation.doctor(name, specialization, phone, email);
        Department department = activeDepartment(request.resolveDepartmentId());
        if (doctorRepository.existsByEmail(email)) {
            throw new ConflictException("Doctor with email already exists: " + email);
        }
        if (doctorRepository.existsByPhone(phone)) {
            throw new ConflictException("Doctor with phone already exists: " + phone);
        }
        Doctor doctor = new Doctor(null, name, specialization, phone, email, department,
                AccountStatus.ACTIVE);
        return DoctorResponse.from(doctorRepository.save(doctor));
    }

    /**
     * Changes the data of a doctor. The status is not changed here.
     *
     * @param doctorId id of the doctor
     * @param request  the new data
     * @return the saved doctor
     * @throws ResourceNotFoundException if the doctor or the department does not exist
     * @throws BadRequestException       if the data is wrong or the department is not active
     * @throws ConflictException         if the phone or email is used by another doctor
     */
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public DoctorResponse updateDoctor(int doctorId, DoctorRequest request) {
        Doctor doctor = find(doctorId);
        String name = Validation.clean(request.name());
        String specialization = Validation.clean(request.specialization());
        String phone = Validation.clean(request.phone());
        String email = Validation.clean(request.email());
        Validation.doctor(name, specialization, phone, email);
        Department department = activeDepartment(request.resolveDepartmentId());
        if (doctorRepository.existsByEmailAndDoctorIdNot(email, doctorId)) {
            throw new ConflictException("Doctor with email already exists: " + email);
        }
        if (doctorRepository.existsByPhoneAndDoctorIdNot(phone, doctorId)) {
            throw new ConflictException("Doctor with phone already exists: " + phone);
        }
        doctor.setName(name);
        doctor.setSpecialization(specialization);
        doctor.setPhone(phone);
        doctor.setEmail(email);
        doctor.setDepartment(department);
        return DoctorResponse.from(doctorRepository.save(doctor));
    }

    /**
     * Sets a doctor to INACTIVE.
     *
     * @param doctorId id of the doctor
     * @throws ResourceNotFoundException if the doctor does not exist
     */
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public void deactivateDoctor(int doctorId) {
        Doctor doctor = find(doctorId);
        doctor.setStatus(AccountStatus.INACTIVE);
        doctorRepository.save(doctor);
    }

    /**
     * Sets a doctor to ACTIVE.
     *
     * @param doctorId id of the doctor
     * @throws ResourceNotFoundException if the doctor does not exist
     */
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public void activateDoctor(int doctorId) {
        Doctor doctor = find(doctorId);
        doctor.setStatus(AccountStatus.ACTIVE);
        doctorRepository.save(doctor);
    }

    /**
     * Returns one doctor. The answer is cached.
     *
     * @param doctorId id of the doctor
     * @return the doctor
     * @throws ResourceNotFoundException if the doctor does not exist
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "doctors", key = "#p0")
    public DoctorResponse getDoctorById(int doctorId) {
        return DoctorResponse.from(find(doctorId));
    }

    /**
     * Returns all doctors. The answer is cached.
     *
     * @return the doctors
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "doctors", key = "'all'")
    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(DoctorResponse::from)
                .toList();
    }

    private Doctor find(int doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with ID: " + doctorId));
    }

    private Department activeDepartment(int departmentId) {
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
        return department;
    }
}
