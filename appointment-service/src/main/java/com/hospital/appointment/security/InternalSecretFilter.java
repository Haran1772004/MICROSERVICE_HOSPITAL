package com.hospital.appointment.security;

import com.hospital.common.security.InternalApi;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Protects internal endpoints by requiring the shared secret header.
 */
public class InternalSecretFilter extends OncePerRequestFilter {

    /** Role given to a caller that sent the correct internal secret. */
    public static final String INTERNAL_ROLE = "INTERNAL";

    private final byte[] expectedSecret;

    /**
     * Creates the filter.
     *
     * @param secret expected value of {@code INTERNAL_API_SECRET}
     */
    public InternalSecretFilter(String secret) {
        this.expectedSecret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.equals(InternalApi.PATH_PREFIX)
                || path.startsWith(InternalApi.PATH_PREFIX + "/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String sent = request.getHeader(InternalApi.SECRET_HEADER);
        boolean valid = sent != null
                && expectedSecret.length > 0
                && MessageDigest.isEqual(expectedSecret, sent.getBytes(StandardCharsets.UTF_8));

        if (!valid) {
            SecurityErrorWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Missing or invalid internal secret");
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("internal-service", null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + INTERNAL_ROLE))));

        filterChain.doFilter(request, response);
    }
}
