package com.hospital.common.dto;

import java.time.Instant;

/**
 * Standard error body returned by every service.
 *
 * @param status    HTTP status code
 * @param message   short message for the caller
 * @param timestamp time the error happened
 */
public record ErrorResponse(int status, String message, Instant timestamp) {

    /**
     * Creates an error body with the current time.
     *
     * @param status  HTTP status code
     * @param message short message for the caller
     * @return the error body
     */
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, Instant.now());
    }
}
