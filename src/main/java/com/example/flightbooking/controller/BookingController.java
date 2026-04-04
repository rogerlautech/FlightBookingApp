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
 */
@RestController
@RequestMapping("/api/bookings")
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Creates a booking for a known flight.
     *
     * @param request booking payload
     * @return booking confirmation with HTTP 201
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
