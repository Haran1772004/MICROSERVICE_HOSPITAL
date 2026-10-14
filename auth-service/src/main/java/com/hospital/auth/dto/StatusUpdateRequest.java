package com.hospital.auth.dto;

import jakarta.validation.constraints.NotNull;
import com.hospital.common.enums.AccountStatus;

/**
 * Body of {@code PATCH /internal/users/{userId}/status}.
 *
 * @param status the new account status
 */
public record StatusUpdateRequest(
        @NotNull(message = "Account status is required.") AccountStatus status) { }
