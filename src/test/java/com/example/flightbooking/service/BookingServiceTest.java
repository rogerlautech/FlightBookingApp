package com.example.flightbooking.service;

import com.example.flightbooking.dto.BookingRequest;
import com.example.flightbooking.dto.BookingResponse;
import com.example.flightbooking.exception.FlightNotFoundException;
import com.example.flightbooking.exception.NoSeatsAvailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingServiceTest {

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService();
        bookingService.initializeFlights();
    }

    @Test
    void bookTicket_successfulBooking_decrementsSeatCount() {
        BookingResponse first = bookingService.bookTicket(new BookingRequest("FL-1003", "Alice Walker"));

        assertNotNull(first.bookingId());
        assertTrue(first.bookingId().matches("BK-[0-9A-F]{8}"));
        assertEquals("FL-1003", first.flightNumber());
        assertEquals("Alice Walker", first.passengerName());
        assertEquals("Booking confirmed", first.message());

        assertThrows(
                NoSeatsAvailableException.class,
                () -> bookingService.bookTicket(new BookingRequest("FL-1003", "Bob Martin"))
        );
    }

    @Test
    void bookTicket_flightNotFound_throwsFlightNotFoundException() {
        assertThrows(
                FlightNotFoundException.class,
                () -> bookingService.bookTicket(new BookingRequest("FL-9999", "Alice Walker"))
        );
    }

    @Test
    void bookTicket_noSeatsAvailable_throwsNoSeatsAvailableException() {
        bookingService.bookTicket(new BookingRequest("FL-1001", "Passenger One"));
        bookingService.bookTicket(new BookingRequest("FL-1001", "Passenger Two"));

        assertThrows(
                NoSeatsAvailableException.class,
                () -> bookingService.bookTicket(new BookingRequest("FL-1001", "Passenger Three"))
        );
    }

    @Test
    void bookTicket_concurrentRequestsOnLastSeat_onlyOneSucceeds() throws Exception {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        ConcurrentLinkedQueue<BookingResponse> successes = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        List<Future<Void>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threadCount; i++) {
                int passengerIndex = i;
                Callable<Void> task = () -> {
                    startGate.await();
                    try {
                        BookingResponse response = bookingService.bookTicket(
                                new BookingRequest("FL-1003", "Passenger-" + passengerIndex)
                        );
                        successes.add(response);
                    } catch (Throwable t) {
                        failures.add(t);
                    }
                    return null;
                };
                futures.add(executor.submit(task));
            }

            startGate.countDown();

            for (Future<Void> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        long noSeatFailures = failures.stream().filter(NoSeatsAvailableException.class::isInstance).count();

        assertEquals(1, successes.size(), "Exactly one booking should succeed for the last seat");
        assertEquals(threadCount - 1L, noSeatFailures, "All other concurrent attempts should fail");
    }
}
