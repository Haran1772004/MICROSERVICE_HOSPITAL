package com.hospital.common.exception;

/**
 * Thrown when something is not found (HTTP 404).
 */
public class ResourceNotFoundException extends ApiException {

    private static final int HTTP_STATUS_NOT_FOUND = 404;
    private static final long serialVersionUID = 1L;

    /**
     * Creates the error.
     *
     * @param message message shown to the caller
     */
    public ResourceNotFoundException(String message) {
        super(HTTP_STATUS_NOT_FOUND, message);
    }
}
