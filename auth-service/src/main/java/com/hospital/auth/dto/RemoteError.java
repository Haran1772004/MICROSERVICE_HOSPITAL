package com.hospital.auth.dto;
/**
 * Part of the error body ({@code ErrorResponse}) that another service sent back. Only the message
 * is needed.
 *
 * @param message the error message of the other service
 */
public record RemoteError(String message) { }
