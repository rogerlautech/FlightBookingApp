package com.example.flightbooking.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating a booking for a known flight.
 *
 * @param flightNumber unique flight number provided by the client
 * @param passengerName full name of the passenger to book
 */
public record BookingRequest(
        @NotBlank(message = "flightNumber is required") String flightNumber,
        @NotBlank(message = "passengerName is required") String passengerName
) {
}
