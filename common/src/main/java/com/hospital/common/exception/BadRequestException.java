package com.hospital.common.exception;

/**
 * Thrown when the request data is wrong (HTTP 400).
 */
public class BadRequestException extends ApiException {

    private static final int HTTP_STATUS_BAD_REQUEST = 400;
    private static final long serialVersionUID = 1L;

    /**
     * Creates the error.
     *
     * @param message message shown to the caller
     */
    public BadRequestException(String message) {
        super(HTTP_STATUS_BAD_REQUEST, message);
    }
}
