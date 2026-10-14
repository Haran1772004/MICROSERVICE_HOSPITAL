package com.hospital.appointment.dto;

/**
 * Error body returned by a remote service.
 *
 * @param message the error message
 */
public record RemoteError(String message) {
}
