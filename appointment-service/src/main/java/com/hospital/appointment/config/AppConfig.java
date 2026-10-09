package com.hospital.appointment.config;

import com.hospital.common.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * General beans of the appointment-service.
 */
@Configuration
public class AppConfig {

    /**
     * Creates the helper that signs and reads JWT tokens.
     *
     * @param secret       value of {@code JWT_SECRET}
     * @param expirationMs token life time in milliseconds
     * @return the JWT helper
     */
    @Bean
    public JwtUtil jwtUtil(@Value("${jwt.secret}") String secret,
                           @Value("${jwt.expiration-ms}") long expirationMs) {
        return new JwtUtil(secret, expirationMs);
    }
}
