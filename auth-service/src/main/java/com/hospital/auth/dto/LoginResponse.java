package com.hospital.auth.dto;
/**
 * Answer of a successful login.
 *
 * @param token the JWT to send in the {@code Authorization: Bearer} header
 * @param message a short text
 */
public record LoginResponse(String token, String message) { }
