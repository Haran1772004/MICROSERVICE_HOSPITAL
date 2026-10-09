package com.hospital.hospitalservice.config;

import com.hospital.common.security.JwtUtil;
import com.hospital.hospitalservice.security.InternalSecretFilter;
import com.hospital.hospitalservice.security.JwtAuthenticationFilter;
import com.hospital.hospitalservice.security.SecurityErrorWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security rules of the hospital-service: stateless JWT, role checks with
 * {@code @PreAuthorize}, and the internal secret for {@code /internal/**}.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final String internalSecret;
    private final String allowedOriginPatterns;

    /**
     * Creates the configuration.
     *
     * @param jwtUtil               helper to read JWT tokens
     * @param internalSecret        value of {@code INTERNAL_API_SECRET}
     * @param allowedOriginPatterns comma separated CORS origin patterns
     */
    public SecurityConfig(JwtUtil jwtUtil,
                          @Value("${internal.api-secret}") String internalSecret,
                          @Value("${cors.allowed-origin-patterns}") String allowedOriginPatterns) {
        this.jwtUtil = jwtUtil;
        this.internalSecret = internalSecret;
        this.allowedOriginPatterns = allowedOriginPatterns;
    }

    /**
     * CORS rules for all URLs.
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> patterns = Arrays.stream(allowedOriginPatterns.split(","))
                .map(String::trim)
                .filter(pattern -> !pattern.isEmpty())
                .toList();
        configuration.setAllowedOriginPatterns(patterns);
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * The security filter chain.
     *
     * @param http the HTTP security builder
     * @return the filter chain
     * @throws Exception if the chain cannot be built
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // The two filters are created here (not as @Component) so they run only once.
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil);
        InternalSecretFilter internalFilter = new InternalSecretFilter(internalSecret);

        http
            // CSRF is not needed: no cookies or sessions, the JWT is sent in a header.
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint((request, response, exception) ->
                            SecurityErrorWriter.write(response,
                                    HttpServletResponse.SC_UNAUTHORIZED,
                                    "Authentication required"))
                    .accessDeniedHandler((request, response, exception) ->
                            SecurityErrorWriter.write(response,
                                    HttpServletResponse.SC_FORBIDDEN, "Access denied")))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/error").permitAll()
                    .requestMatchers("/internal/**")
                            .hasRole(InternalSecretFilter.INTERNAL_ROLE)
                    .anyRequest().authenticated())
            .addFilterBefore(internalFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
