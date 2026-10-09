package com.hospital.common.security;

import com.hospital.common.enums.Role;

/**
 * Data read from a valid JWT token.
 *
 * @param username username of the logged-in user
 * @param userId   id of the user in auth-service
 * @param role     role of the user
 */
public record JwtUser(String username, int userId, Role role) {
}
