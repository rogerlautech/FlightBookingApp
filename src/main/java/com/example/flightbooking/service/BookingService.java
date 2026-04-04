package com.example.flightbooking.service;

import com.example.flightbooking.dto.BookingRequest;
import com.example.flightbooking.dto.BookingResponse;
import com.example.flightbooking.exception.FlightNotFoundException;
import com.example.flightbooking.exception.NoSeatsAvailableException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core booking logic with in-memory, thread-safe seat management.
 *
 * <p>This service owns all mutable booking state for the assignment and is
 * responsible for preventing overbooking under concurrent requests.</p>
 */
@Service
@Slf4j
public class BookingService {

    /**
     * Key: flight number, Value: remaining seats for that flight.
     *
     * ConcurrentHashMap provides safe concurrent access to flight entries while
     * AtomicInteger provides lock-free atomic seat updates per flight.
     */
    private final Map<String, AtomicInteger> remainingSeatsByFlight = new ConcurrentHashMap<>();

    /**
     * Monotonic counter used to generate unique booking IDs.
     */
    private final AtomicInteger bookingSequence = new AtomicInteger(0);

    /**
     * Loads a small static flight catalog for the interview assignment.
     *
     * <p>Runs once after bean construction and before serving requests.</p>
     */
    @PostConstruct
    void initializeFlights() {
        remainingSeatsByFlight.put("FL-1001", new AtomicInteger(2));
        remainingSeatsByFlight.put("FL-1002", new AtomicInteger(5));
        remainingSeatsByFlight.put("FL-1003", new AtomicInteger(1));
        remainingSeatsByFlight.put("FL-1004", new AtomicInteger(3));
        remainingSeatsByFlight.put("FL-1005", new AtomicInteger(10));
    }

    /**
     * Books one seat for a known flight.
     *
     * Concurrency approach:
     * - Each flight's remaining seats are tracked with a dedicated AtomicInteger.
     * - We reserve seats with a compare-and-set (CAS) loop:
     *   read current seats -> fail fast if 0 -> atomically update current to current-1.
     * - If another thread changed the value between read and write, CAS fails and we retry.
     * - This guarantees no overbooking, including concurrent requests racing for the last seat.
     *
     * @param request booking input containing known flight number and passenger name
     * @return booking confirmation payload
     * @throws FlightNotFoundException when the flight number does not exist
     * @throws NoSeatsAvailableException when no seats remain for the flight
     */
    public BookingResponse bookTicket(BookingRequest request) {
        String flightNumber = request.flightNumber();
        String passengerName = request.passengerName();
        AtomicInteger remainingSeats = remainingSeatsByFlight.get(flightNumber);

        if (remainingSeats == null) {
            log.error("Booking failed: flight {} not found for passenger {}", flightNumber, passengerName);
            throw new FlightNotFoundException("Flight not found: " + flightNumber);
        }

        // Lock-free reservation loop for this flight counter.
        while (true) {
            int current = remainingSeats.get();
            if (current <= 0) {
                log.warn("Booking rejected: no seats available on flight {} for passenger {}", flightNumber, passengerName);
                throw new NoSeatsAvailableException("No seats available for flight: " + flightNumber);
            }
            if (remainingSeats.compareAndSet(current, current - 1)) {
                break;
            }
        }

        int remainingAfterBooking = remainingSeats.get();
        String bookingId = generateBookingId();
        BookingResponse response = new BookingResponse(
                bookingId,
                flightNumber,
                passengerName,
                "Booking confirmed"
        );
        log.info(
                "Booking succeeded: bookingId={}, flight={}, passenger={}, remainingSeats={}",
                bookingId,
                flightNumber,
                passengerName,
                remainingAfterBooking
        );
        return response;
    }

    /**
     * Produces a unique booking identifier using a thread-safe monotonically
     * increasing sequence and uppercase hexadecimal formatting.
     *
     * @return booking identifier in the form {@code BK-XXXXXXXX}
     */
    private String generateBookingId() {
        return "BK-%08X".formatted(bookingSequence.incrementAndGet());
    }
}
