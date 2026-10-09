package com.hospital.appointment.exception;

import com.hospital.common.dto.ErrorResponse;
import com.hospital.common.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.util.stream.Collectors;

/**
 * Turns exceptions into the shared {@link ErrorResponse} format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles business errors.
     *
     * @param exception the business error
     * @return the error response
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
        return build(exception.getStatus(), exception.getMessage());
    }

    /**
     * Handles validation errors.
     *
     * @param exception the validation error
     * @return the error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(" "));
        return build(HttpStatus.BAD_REQUEST.value(),
                message.isBlank() ? "Validation failed." : message);
    }

    /**
     * Handles malformed JSON bodies.
     *
     * @param exception the read error
     * @return the error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException exception) {
        return build(HttpStatus.BAD_REQUEST.value(), "Malformed or unreadable request body.");
    }

    /**
     * Handles invalid parameter types.
     *
     * @param exception the conversion error
     * @return the error response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception) {
        return build(HttpStatus.BAD_REQUEST.value(),
                "Invalid value for parameter '" + exception.getName() + "'.");
    }

    /**
     * Handles missing parameters.
     *
     * @param exception the missing parameter error
     * @return the error response
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException exception) {
        return build(HttpStatus.BAD_REQUEST.value(),
                "Required parameter '" + exception.getParameterName() + "' is missing.");
    }

    /**
     * Handles wrong login data.
     *
     * @param exception the error
     * @return the error response
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException exception) {
        return build(HttpStatus.UNAUTHORIZED.value(), "Invalid username or password.");
    }

    /**
     * Handles access denial.
     *
     * @param exception the access denial error
     * @return the error response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        return build(HttpStatus.FORBIDDEN.value(), "Access denied");
    }

    /**
     * Handles unsupported HTTP methods.
     *
     * @param exception the method error
     * @return the error response
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception) {
        return build(HttpStatus.METHOD_NOT_ALLOWED.value(),
                "HTTP method not supported: " + exception.getMethod());
    }

    /**
     * Handles unsupported media types.
     *
     * @param exception the media type error
     * @return the error response
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(
            HttpMediaTypeNotSupportedException exception) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "Content type not supported. Use application/json.");
    }

    /**
     * Handles missing endpoints.
     *
     * @param exception the missing route
     * @return the error response
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException exception) {
        return build(HttpStatus.NOT_FOUND.value(), "Resource not found");
    }

    /**
     * Handles unexpected internal errors.
     *
     * @param exception the unexpected error
     * @return the error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        log.error("Unexpected error", exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred.");
    }

    private ResponseEntity<ErrorResponse> build(int status, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, message));
    }
}
