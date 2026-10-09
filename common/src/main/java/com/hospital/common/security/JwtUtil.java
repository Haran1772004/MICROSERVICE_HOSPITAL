package com.hospital.common.security;

import com.hospital.common.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * Creates and reads JWT tokens. Every service uses the same secret, so a token
 * made by auth-service can be checked by the other services.
 * This is a plain class. Each service creates one in its own configuration class.
 */
public class JwtUtil {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey key;
    private final long expirationMs;

    /**
     * Creates the helper.
     *
     * @param secret       secret text (at least 32 characters)
     * @param expirationMs token life time in milliseconds
     */
    public JwtUtil(String secret, long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Creates a signed token.
     *
     * @param username username of the user
     * @param userId   id of the user
     * @param role     role of the user
     * @return the token text
     */
    public String generateToken(String username, int userId, Role role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_ROLE, role.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Reads a token. Returns empty if the token is wrong or expired.
     *
     * @param token the token text
     * @return the user data, or empty if the token is not valid
     */
    public Optional<JwtUser> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            int userId = claims.get(CLAIM_USER_ID, Number.class).intValue();
            Role role = Role.valueOf(claims.get(CLAIM_ROLE, String.class));
            return Optional.of(new JwtUser(claims.getSubject(), userId, role));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }
}
