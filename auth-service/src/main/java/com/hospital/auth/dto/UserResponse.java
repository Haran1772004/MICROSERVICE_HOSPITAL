package com.hospital.auth.dto;

import com.hospital.auth.entity.User;
import com.hospital.common.enums.AccountStatus;
import com.hospital.common.enums.Role;

/**
 * A user as shown to the caller. The password is never included.
 *
 * @param userId id of the user
 * @param username login name
 * @param role role of the user
 * @param status account status
 */
public record UserResponse(int userId, String username, Role role, AccountStatus status) {

    /**
      * Converts a user entity to a response.
      *
      * @param user the entity
      * @return the response
      */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(), user.getUsername(), user.getRole(), user.getStatus());
    }
}
