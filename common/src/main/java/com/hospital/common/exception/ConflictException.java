package com.hospital.common.exception;

/**
 * Thrown when the request clashes with existing data (HTTP 409).
 */
public class ConflictException extends ApiException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the error.
     *
     * @param message message shown to the caller
     */
    public ConflictException(String message) {
        super(409, message);
    }
}
