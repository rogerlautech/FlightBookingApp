package com.example.flightbooking.exception;

/**
 * Thrown when a requested flight number does not exist in the in-memory flight catalog.
 */
public class FlightNotFoundException extends RuntimeException {

    public FlightNotFoundException(String message) {
        super(message);
    }
}
