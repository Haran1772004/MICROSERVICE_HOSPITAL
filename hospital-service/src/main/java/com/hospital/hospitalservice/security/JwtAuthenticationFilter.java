package com.hospital.hospitalservice.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hospital.common.security.InternalApi;
import com.hospital.common.security.JwtUser;
import com.hospital.common.security.JwtUtil;

/**
 * Reads the {@code Authorization: Bearer <token>} header, checks the token with
 * {@link JwtUtil} and puts the logged-in user into the security context.
 * A request without a token passes through (Spring Security then answers 401 where login
 * is needed). A request with a wrong or expired token gets 401 here.
 * It ignores {@code /internal/**}: a user token must never open those endpoints.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    /**
     * Creates the filter.
     *
     * @param jwtUtil helper used to read tokens
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals(InternalApi.PATH_PREFIX)
                || path.startsWith(InternalApi.PATH_PREFIX + "/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<JwtUser> user = jwtUtil.parse(header.substring(BEARER_PREFIX.length()));
        if (user.isEmpty()) {
            SecurityErrorWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired JWT");
            return;
        }

        JwtUser jwtUser = user.get();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(jwtUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + jwtUser.role().name())));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
