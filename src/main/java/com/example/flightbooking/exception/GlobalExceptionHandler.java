package com.example.flightbooking.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralized exception-to-HTTP mapping for booking APIs.
 * Keeps error responses consistent across all controllers.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maps missing-flight domain errors to HTTP 404.
     *
     * @param ex thrown domain exception
     * @param request current HTTP request
     * @return standardized error response with {@code 404 Not Found}
     */
    @ExceptionHandler(FlightNotFoundException.class)
    public ResponseEntity<ApiError> handleFlightNotFound(
            FlightNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
    }

    /**
     * Maps sold-out-flight domain errors to HTTP 409.
     *
     * @param ex thrown domain exception
     * @param request current HTTP request
     * @return standardized error response with {@code 409 Conflict}
     */
    @ExceptionHandler(NoSeatsAvailableException.class)
    public ResponseEntity<ApiError> handleNoSeatsAvailable(
            NoSeatsAvailableException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), null);
    }

    /**
     * Maps request payload validation failures to HTTP 400 and includes
     * field-level error details.
     *
     * @param ex thrown validation exception
     * @param request current HTTP request
     * @return standardized error response with {@code 400 Bad Request}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationError(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        return buildError(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                request.getRequestURI(),
                fieldErrors
        );
    }

    /**
     * Provides a safe fallback for uncaught exceptions while logging details
     * for server-side diagnostics.
     *
     * @param ex unexpected exception
     * @param request current HTTP request
     * @return standardized error response with {@code 500 Internal Server Error}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedError(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception for request path {}", request.getRequestURI(), ex);
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request.getRequestURI(),
                null
        );
    }

    /**
     * Builds the common JSON error payload used across all handlers.
     *
     * @param status HTTP status to return
     * @param message client-facing error message
     * @param path request path that triggered the error
     * @param details optional structured details (for example validation field errors)
     * @return response entity with uniform error structure
     */
    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> details
    ) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                details
        );
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Uniform JSON error contract for all handled exceptions.
     *
     * @param timestamp ISO-8601 UTC time when the error response is generated
     * @param status numeric HTTP status code
     * @param error HTTP reason phrase
     * @param message client-facing description of the problem
     * @param path request URI path
     * @param details optional key/value details such as validation errors
     */
    public record ApiError(
            String timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, String> details
    ) {
    }
}
