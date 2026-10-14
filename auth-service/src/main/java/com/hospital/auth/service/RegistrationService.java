package com.hospital.auth.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hospital.auth.client.HospitalServiceClient;
import com.hospital.auth.dto.CreateDoctorProfileRequest;
import com.hospital.auth.dto.CreatePatientProfileRequest;
import com.hospital.auth.dto.DoctorRegistrationRequest;
import com.hospital.auth.dto.PatientRegistrationRequest;
import com.hospital.auth.entity.User;
import com.hospital.common.enums.AccountStatus;
import com.hospital.common.enums.AddressType;
import com.hospital.common.enums.Gender;
import com.hospital.common.enums.Role;
import com.hospital.common.exception.ApiException;
import com.hospital.common.exception.BadRequestException;
import com.hospital.common.exception.ServiceUnavailableException;

/**
 * Self-registration of patients and doctors.
 *
 * <p>Steps: check the data, create the user here, then ask hospital-service to create the profile.
 * If hospital-service fails, the user is deleted again ("undo"). The user is saved in its own
 * transaction before the HTTP call, so no database connection is held open while waiting for the
 * other service.
 */
@Service
public class RegistrationService {

    private static final Logger LOG = LoggerFactory.getLogger(RegistrationService.class);

    private static final DateTimeFormatter DOB_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final UserService userService;
    private final HospitalServiceClient hospitalClient;

    /**
      * Creates the service.
      *
      * @param userService creates and deletes users
      * @param hospitalClient calls hospital-service
      */
    public RegistrationService(UserService userService, HospitalServiceClient hospitalClient) {
        this.userService = userService;
        this.hospitalClient = hospitalClient;
    }

    /**
      * Registers a patient. The user is ACTIVE at once.
      *
      * @param request the validated registration data
      * @throws BadRequestException if date of birth, gender or address is wrong
      * @throws ApiException if the username is taken (HTTP 409) or hospital-service reports
      *     a business error
      * @throws ServiceUnavailableException 503 if hospital-service cannot be used
      */
    public void registerPatient(PatientRegistrationRequest request) {
        LocalDate dob = parseDob(request.dob());
        Gender gender = parseGender(request.gender());

        boolean hasAddress =
                isNonBlank(request.state())
                        || isNonBlank(request.district())
                        || isNonBlank(request.pincode());
        AddressType addressType = null;
        if (hasAddress) {
            requireAddressFields(request);
            addressType = parseAddressType(request.addressType());
        }

        User user =
                userService.registerAccount(
                        request.username(), request.password(), Role.PATIENT, AccountStatus.ACTIVE);

        CreatePatientProfileRequest profile =
                new CreatePatientProfileRequest(
                        user.getUserId(),
                        request.name(),
                        dob.toString(),
                        gender.name(),
                        request.phone(),
                        request.email(),
                        hasAddress ? request.state() : null,
                        hasAddress ? request.district() : null,
                        hasAddress ? request.pincode() : null,
                        hasAddress ? addressType.name() : null);

        createProfileOrUndo(user, () -> hospitalClient.createPatient(profile));
        LOG.info("Patient registered: user {}", user.getUsername());
    }

    /**
      * Registers a doctor. The user is PENDING until an admin approves the doctor.
      *
      * @param request the validated registration data
      * @throws ApiException if the username is taken (HTTP 409) or hospital-service reports
      *     a business error
      * @throws ServiceUnavailableException 503 if hospital-service cannot be used
      */
    public void registerDoctor(DoctorRegistrationRequest request) {
        User user =
                userService.registerAccount(
                        request.username(), request.password(), Role.DOCTOR, AccountStatus.PENDING);

        CreateDoctorProfileRequest profile =
                new CreateDoctorProfileRequest(
                        user.getUserId(),
                        request.name(),
                        request.specialization(),
                        request.phone(),
                        request.email(),
                        request.departmentId());

        createProfileOrUndo(user, () -> hospitalClient.createDoctor(profile));
        LOG.info("Doctor registered (pending approval): user {}", user.getUsername());
    }

    /**
      * Runs the call to hospital-service and deletes the new user if it fails. For business errors,
      * only the user is deleted. If the service was unavailable, its profile may exist, so that
      * profile is also deleted on a best-effort basis.
      *
      * @param user the newly created user
      * @param call operation that creates the associated profile
      */
    private void createProfileOrUndo(User user, Runnable call) {
        try {
            call.run();
        } catch (ServiceUnavailableException exception) {
            undo(user, true);
            throw exception;
        } catch (ApiException exception) {
            undo(user, false);
            throw exception;
        } catch (RuntimeException exception) {
            undo(user, true);
            LOG.error("Unexpected error while creating the profile", exception);
            throw new ServiceUnavailableException(
                    "Hospital service is unavailable. Please try again later.");
        }
    }

    private void undo(User user, boolean profileMayExist) {
        if (profileMayExist) {
            try {
                hospitalClient.deleteProfileByUser(user.getUserId());
            } catch (RuntimeException exception) {
                LOG.warn(
                        "Could not remove the profile of user id {} in hospital-service",
                        user.getUserId());
            }
        }
        try {
            userService.removeUserById(user.getUserId());
        } catch (RuntimeException exception) {
            LOG.error(
                    "Could not undo the registration: user id {} must be deleted by hand",
                    user.getUserId());
        }
    }

    private LocalDate parseDob(String dob) {
        try {
            return LocalDate.parse(dob, DOB_FORMAT);
        } catch (DateTimeParseException exception) {
            throw new BadRequestException("Invalid date of birth. Expected format: uuuu-MM-dd");
        }
    }

    private Gender parseGender(String gender) {
        try {
            return Gender.valueOf(gender.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Invalid gender. Accepted: MALE, FEMALE, OTHER.");
        }
    }

    private AddressType parseAddressType(String addressType) {
        if (!isNonBlank(addressType)) {
            return AddressType.HOME;
        }
        try {
            return AddressType.valueOf(addressType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(
                    "Invalid address type. Accepted: HOME, WORK, BILLING, EMERGENCY.");
        }
    }

    private void requireAddressFields(PatientRegistrationRequest request) {
        if (!isNonBlank(request.state())) {
            throw new BadRequestException("State is required when providing an address.");
        }
        if (!isNonBlank(request.district())) {
            throw new BadRequestException("District is required when providing an address.");
        }
        if (!isNonBlank(request.pincode())) {
            throw new BadRequestException("Pincode is required when providing an address.");
        }
    }

    private boolean isNonBlank(String value) {
        return value != null && !value.isBlank();
    }
}
