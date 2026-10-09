package com.hospital.common.exception;

/**
 * Thrown when another service cannot be reached (HTTP 503).
 */
public class ServiceUnavailableException extends ApiException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the error.
     *
     * @param message message shown to the caller
     */
    public ServiceUnavailableException(String message) {
        super(503, message);
    }
}
