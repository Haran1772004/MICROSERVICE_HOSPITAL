package com.hospital.auth.controller;

import com.hospital.auth.dto.DoctorRegistrationRequest;
import com.hospital.auth.dto.LoginRequest;
import com.hospital.auth.dto.LoginResponse;
import com.hospital.auth.dto.MessageResponse;
import com.hospital.auth.dto.OrderedChecks;
import com.hospital.auth.dto.PatientRegistrationRequest;
import com.hospital.auth.service.AuthService;
import com.hospital.auth.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoints: login and self-registration.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final RegistrationService registrationService;

    /**
     * Creates the controller.
     *
     * @param authService         login logic
     * @param registrationService registration logic
     */
    public AuthController(AuthService authService, RegistrationService registrationService) {
        this.authService = authService;
        this.registrationService = registrationService;
    }

    /**
     * Logs a user in.
     *
     * @param request username and password
     * @return the JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.username(), request.password());
        return ResponseEntity.ok(new LoginResponse(token, "Login successful"));
    }

    /**
     * Registers a patient (ACTIVE at once).
     *
     * @param request the registration data
     * @return 201 with a message
     */
    @PostMapping("/register/patient")
    public ResponseEntity<MessageResponse> registerPatient(
            @Validated(OrderedChecks.class) @RequestBody PatientRegistrationRequest request) {
        registrationService.registerPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse(
                "Patient registered successfully. You can now log in."));
    }

    /**
     * Registers a doctor (PENDING until an admin approves).
     *
     * @param request the registration data
     * @return 201 with a message
     */
    @PostMapping("/register/doctor")
    public ResponseEntity<MessageResponse> registerDoctor(
            @Validated(OrderedChecks.class) @RequestBody DoctorRegistrationRequest request) {
        registrationService.registerDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse(
                "Doctor registration submitted successfully. "
                        + "Your account is pending admin approval."));
    }
}
