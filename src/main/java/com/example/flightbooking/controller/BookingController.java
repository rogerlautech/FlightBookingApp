package com.example.flightbooking.controller;

import com.example.flightbooking.dto.BookingRequest;
import com.example.flightbooking.dto.BookingResponse;
import com.example.flightbooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for flight ticket booking operations.
 *
 * <p>The controller intentionally stays thin and delegates booking rules and
 * concurrency handling to {@link BookingService}.</p>
 */
@RestController
@RequestMapping("/api/bookings")
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    /**
     * Creates a controller with service dependency injected by Spring.
     *
     * @param bookingService application service that executes booking logic
     */
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Creates a booking for a known flight.
     *
     * @param request validated booking payload from request body
     * @return booking confirmation payload with HTTP status {@code 201 Created}
     * @throws com.example.flightbooking.exception.FlightNotFoundException when flight number does not exist
     * @throws com.example.flightbooking.exception.NoSeatsAvailableException when the flight has no remaining seats
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        log.info("Booking request received: flight={}, passenger={}", request.flightNumber(), request.passengerName());
        try {
            BookingResponse response = bookingService.bookTicket(request);
            log.info("Booking response ready: bookingId={}", response.bookingId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException ex) {
            log.error(
                    "Booking request failed: flight={}, passenger={}, reason={}",
                    request.flightNumber(),
                    request.passengerName(),
                    ex.getMessage()
            );
            throw ex;
        }
    }
}
