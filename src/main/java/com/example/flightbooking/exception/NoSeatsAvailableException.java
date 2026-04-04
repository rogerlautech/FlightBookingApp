package com.example.flightbooking.exception;

/**
 * Thrown when a booking request cannot be fulfilled because the flight is sold out.
 */
public class NoSeatsAvailableException extends RuntimeException {

    public NoSeatsAvailableException(String message) {
        super(message);
    }
}
