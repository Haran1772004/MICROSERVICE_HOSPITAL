package com.hospital.common.exception;

/**
 * Base class for all business errors. Each error carries the HTTP status
 * that the service should return.
 */
public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;

    /**
     * Creates an error.
     *
     * @param status  HTTP status code to return
     * @param message message shown to the caller
     */
    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Returns the HTTP status code for this error.
     *
     * @return HTTP status code
     */
    public int getStatus() {
        return status;
    }
}
