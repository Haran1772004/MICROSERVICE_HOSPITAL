package com.hospital.hospitalservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hospital.common.security.JwtUtil;

/**
 * General beans of the hospital-service.
 */
@Configuration
public class AppConfig {

    /**
     * Creates the helper that signs and reads JWT tokens.
     * All services use the same secret, so they can check each other's tokens.
     *
     * @param secret       value of {@code JWT_SECRET} (at least 32 characters)
     * @param expirationMs token life time in milliseconds
     * @return the JWT helper
     */
    @Bean
    public JwtUtil jwtUtil(@Value("${jwt.secret}") String secret,
                           @Value("${jwt.expiration-ms}") long expirationMs) {
        return new JwtUtil(secret, expirationMs);
    }
}
