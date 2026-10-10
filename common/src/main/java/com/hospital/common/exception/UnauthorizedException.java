package com.hospital.common.exception;

/**
 * Thrown when login or token is wrong (HTTP 401).
 */
public class UnauthorizedException extends ApiException {

    private static final int HTTP_STATUS_UNAUTHORIZED = 401;
    private static final long serialVersionUID = 1L;

    /**
     * Creates the error.
     *
     * @param message message shown to the caller
     */
    public UnauthorizedException(String message) {
        super(HTTP_STATUS_UNAUTHORIZED, message);
    }
}
