package com.hospital.appointment.security;

import com.hospital.common.exception.UnauthorizedException;
import com.hospital.common.security.JwtUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Gives access to the logged-in user of the current request.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    /**
     * Returns the logged-in user of the current request.
     *
     * @return the JWT user, or empty if there is no user
     */
    public static Optional<JwtUser> get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /**
     * Returns the logged-in user, or throws.
     *
     * @return the JWT user
     * @throws UnauthorizedException if there is no authenticated user
     */
    public static JwtUser require() {
        return get().orElseThrow(() -> new UnauthorizedException("Authentication required"));
    }
}
