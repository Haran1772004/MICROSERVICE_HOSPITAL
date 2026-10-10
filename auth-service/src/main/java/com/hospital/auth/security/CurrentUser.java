package com.hospital.auth.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.hospital.common.security.JwtUser;

/** Gives access to the user of the current request. */
public final class CurrentUser {

    private CurrentUser() { }

    /**
      * Returns the logged-in user of the current request.
      *
      * @return the user from the JWT, or empty if the request is not a user request
      */
    public static Optional<JwtUser> get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }
}
