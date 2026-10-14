package com.hospital.hospitalservice.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.common.enums.AccountStatus;
import com.hospital.common.exception.BadRequestException;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.hospitalservice.client.AuthServiceClient;
import com.hospital.hospitalservice.dto.DoctorResponse;
import com.hospital.hospitalservice.dto.MessageResponse;
import com.hospital.hospitalservice.entity.Doctor;
import com.hospital.hospitalservice.repository.DoctorRepository;

/**
 * Admin work on doctors: list the doctors that wait for approval, approve or reject them.
 *
 * <p>Approve and reject first change the doctor here, then the user in auth-service. If the
 * call to auth-service fails, the change here is rolled back, so both services stay equal.
 */
@Service
public class AdminService {

    private final DoctorRepository doctorRepository;
    private final AuthServiceClient authServiceClient;

    /**
     * Creates the service.
     *
     * @param doctorRepository  database access for doctors
     * @param authServiceClient client for auth-service
     */
    public AdminService(DoctorRepository doctorRepository,
                        AuthServiceClient authServiceClient) {
        this.doctorRepository = doctorRepository;
        this.authServiceClient = authServiceClient;
    }

    /**
     * Approves a doctor: the doctor and the login account become ACTIVE.
     *
     * @param doctorId id of the doctor
     * @return a message for the admin
     * @throws ResourceNotFoundException   if the doctor does not exist
     * @throws BadRequestException         if the doctor is not PENDING or has no login account
     * @throws ApiException                if auth-service rejected the status change
     * @throws ServiceUnavailableException if auth-service cannot be used
     */
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public MessageResponse approveDoctor(int doctorId) {
        Doctor doctor = changeStatus(doctorId, AccountStatus.ACTIVE);
        return new MessageResponse("Doctor '" + doctor.getName()
                + "' has been approved. They can now log in.");
    }

    /**
     * Rejects a doctor: the doctor and the login account become REJECTED.
     *
     * @param doctorId id of the doctor
     * @return a message for the admin
     * @throws ResourceNotFoundException   if the doctor does not exist
     * @throws BadRequestException         if the doctor is not PENDING or has no login account
     * @throws ApiException                if auth-service rejected the status change
     * @throws ServiceUnavailableException if auth-service cannot be used
     */
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public MessageResponse rejectDoctor(int doctorId) {
        Doctor doctor = changeStatus(doctorId, AccountStatus.REJECTED);
        return new MessageResponse("Doctor '" + doctor.getName()
                + "' registration has been rejected.");
    }

    /**
     * Returns the doctors that wait for approval.
     *
     * @return the doctors with status PENDING
     */
    @Transactional(readOnly = true)
    public List<DoctorResponse> getPendingDoctors() {
        return doctorRepository.findByStatus(AccountStatus.PENDING).stream()
                .map(DoctorResponse::from)
                .toList();
    }

    private Doctor changeStatus(int doctorId, AccountStatus newStatus) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with ID: " + doctorId));
        if (doctor.getStatus() != AccountStatus.PENDING) {
            throw new BadRequestException("Doctor '" + doctor.getName()
                    + "' is not in PENDING status. Current status: " + doctor.getStatus());
        }
        if (doctor.getUserId() == null) {
            throw new BadRequestException("Doctor '" + doctor.getName()
                    + "' has no login account.");
        }
        doctor.setStatus(newStatus);
        doctorRepository.saveAndFlush(doctor);
        // An exception here rolls back the change above.
        authServiceClient.updateUserStatus(doctor.getUserId(), newStatus);
        return doctor;
    }
}
