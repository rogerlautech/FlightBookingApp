package com.example.flightbooking.dto;

/**
 * Response payload returned after a booking attempt.
 *
 * @param bookingId unique identifier generated for the booking
 * @param flightNumber flight number associated with the booking
 * @param passengerName passenger name stored for the booking
 * @param message human-readable status message (for example, booking confirmed)
 */
public record BookingResponse(
        String bookingId,
        String flightNumber,
        String passengerName,
        String message
) {
}
