package com.hospital.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hospital.common.security.JwtUtil;

/** General beans of the auth-service. */
@Configuration
public class AppConfig {

    /**
      * Creates the helper that signs and reads JWT tokens. All services use the same secret, so
      * they can check tokens from other services.
      *
      * @param secret value of {@code JWT_SECRET} (at least 32 characters)
      * @param expirationMs token life time in milliseconds
      * @return the JWT helper
      */
    @Bean
    public JwtUtil jwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        return new JwtUtil(secret, expirationMs);
    }
}
