package com.example.flightbooking.exception;

/**
 * Thrown when a booking request cannot be fulfilled because the flight is sold out.
 */
public class NoSeatsAvailableException extends RuntimeException {

    /**
     * Creates the exception with a client-safe error message.
     *
     * @param message descriptive reason including the exhausted flight number
     */
    public NoSeatsAvailableException(String message) {
        super(message);
    }
}
