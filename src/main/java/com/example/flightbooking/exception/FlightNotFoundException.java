package com.example.flightbooking.exception;

/**
 * Thrown when a requested flight number does not exist in the in-memory flight catalog.
 */
public class FlightNotFoundException extends RuntimeException {

    /**
     * Creates the exception with a client-safe error message.
     *
     * @param message descriptive reason including the missing flight number
     */
    public FlightNotFoundException(String message) {
        super(message);
    }
}
