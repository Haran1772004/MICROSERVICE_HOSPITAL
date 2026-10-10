package com.hospital.hospitalservice.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.hospital.common.dto.ErrorResponse;
import com.hospital.common.exception.ApiException;

/**
 * Turns every exception into an {@link ErrorResponse} with the right HTTP status.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles business errors; the status comes from the exception.
     *
     * @param exception the business error
     * @return the error body with the status of the exception
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
        return build(exception.getStatus(), exception.getMessage());
    }

    /**
     * Handles failed validation of a request body (400).
     *
     * @param exception the validation error
     * @return the error body with all field messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(" "));
        return build(HttpStatus.BAD_REQUEST.value(),
                message.isBlank() ? "Validation failed." : message);
    }

    /**
     * Handles an unreadable body, for example broken JSON or an unknown enum value (400).
     *
     * @param exception the read error
     * @return the error body
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(
            HttpMessageNotReadableException exception) {
        return build(HttpStatus.BAD_REQUEST.value(),
                "Malformed or unreadable request body.");
    }

    /**
     * Handles a parameter with a wrong type or value, for example an unknown status (400).
     *
     * @param exception the conversion error
     * @return the error body
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception) {
        return build(HttpStatus.BAD_REQUEST.value(),
                "Invalid value for parameter '" + exception.getName() + "'.");
    }

    /**
     * Handles a missing request parameter (400).
     *
     * @param exception the error
     * @return the error body
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException exception) {
        return build(HttpStatus.BAD_REQUEST.value(),
                "Required parameter '" + exception.getParameterName() + "' is missing.");
    }

    /**
     * Handles a database rule that was broken, for example a duplicate value that two
     * requests tried to save at the same time (409).
     *
     * @param exception the database error
     * @return the error body
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException exception) {
        LOG.warn("Data integrity violation: {}", exception.getClass().getSimpleName());
        return build(HttpStatus.CONFLICT.value(),
                "The data conflicts with existing data.");
    }

    /**
     * Handles a wrong username or password (401).
     *
     * @param exception the error
     * @return the error body
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException exception) {
        return build(HttpStatus.UNAUTHORIZED.value(), "Invalid username or password.");
    }

    /**
     * Handles a user who has no right for the endpoint (403).
     *
     * @param exception the error
     * @return the error body
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        return build(HttpStatus.FORBIDDEN.value(), "Access denied");
    }

    /**
     * Handles a wrong HTTP method (405).
     *
     * @param exception the error
     * @return the error body
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception) {
        return build(HttpStatus.METHOD_NOT_ALLOWED.value(),
                "HTTP method not supported: " + exception.getMethod());
    }

    /**
     * Handles a wrong content type (415).
     *
     * @param exception the error
     * @return the error body
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(
            HttpMediaTypeNotSupportedException exception) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "Content type not supported. Use application/json.");
    }

    /**
     * Handles a URL that does not exist (404).
     *
     * @param exception the error
     * @return the error body
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException exception) {
        return build(HttpStatus.NOT_FOUND.value(), "Resource not found");
    }

    /**
     * Handles every other error (500). Details are logged, not sent to the caller.
     *
     * @param exception the error
     * @return the error body
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        LOG.error("Unexpected error", exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred.");
    }

    private ResponseEntity<ErrorResponse> build(int status, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, message));
    }
}
