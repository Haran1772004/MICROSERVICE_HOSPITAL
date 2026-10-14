package com.hospital.common.exception;

/**
 * Thrown when another service cannot be reached (HTTP 503).
 */
public class ServiceUnavailableException extends ApiException {

    private static final int HTTP_STATUS_SERVICE_UNAVAILABLE = 503;
    private static final long serialVersionUID = 1L;

    /**
     * Creates the error.
     *
     * @param message message shown to the caller
     */
    public ServiceUnavailableException(String message) {
        super(HTTP_STATUS_SERVICE_UNAVAILABLE, message);
    }
}
