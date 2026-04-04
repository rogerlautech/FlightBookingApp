package com.example.flightbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the Flight Booking REST API.
 *
 * <p>This application is intentionally designed for an interview assignment:
 * single instance, in-memory state, and booking-only endpoints.</p>
 */
@SpringBootApplication
public class FlightBookingApplication {

    /**
     * Starts the Spring Boot application context and embedded web server.
     *
     * @param args runtime arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(FlightBookingApplication.class, args);
    }
}
