package com.hospital.common.exception;

/**
 * Thrown when the user is not allowed to do this (HTTP 403).
 */
public class ForbiddenException extends ApiException {

    private static final int HTTP_STATUS_FORBIDDEN = 403;
    private static final long serialVersionUID = 1L;

    /**
     * Creates the error.
     *
     * @param message message shown to the caller
     */
    public ForbiddenException(String message) {
        super(HTTP_STATUS_FORBIDDEN, message);
    }
}
