# Flight Booking API

## Project Description
This project is a Spring Boot 3 + Java 17 REST API for flight ticket booking, built for an interview assignment.

Scope is intentionally minimal and focused:
- Single application instance
- In-memory storage only (no database)
- No authentication or authorization
- No flight search or creation APIs (client already knows `flightNumber`)
- Booking-only API (`POST /api/bookings`)
- Concurrency-safe seat allocation to prevent overbooking

## Tech Stack
- Java 17
- Spring Boot 3 (Web, Validation)
- Gradle Kotlin DSL (`build.gradle.kts`)
- JUnit 5 for unit testing

## Build and Run

### Prerequisites
- Java 17 installed
- Gradle installed, or use Gradle Wrapper (`./gradlew`)

### Build
```bash
./gradlew clean build
```
If wrapper is not available:
```bash
gradle clean build
```

### Run
```bash
./gradlew bootRun
```
Or:
```bash
gradle bootRun
```

Application starts on:
- `http://localhost:8080`

## API Endpoint

### Create Booking
- Method: `POST`
- Path: `/api/bookings`
- Content-Type: `application/json`

Request body:
```json
{
  "flightNumber": "FL-1003",
  "passengerName": "Alice Walker"
}
```

## Sample cURL Commands

### 1. Successful booking
```bash
curl -i -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"flightNumber":"FL-1003","passengerName":"Alice Walker"}'
```

Expected:
- `201 Created`
- JSON response with `bookingId`, `flightNumber`, `passengerName`, `message`

Example response:
```json
{
  "bookingId": "BK-00000001",
  "flightNumber": "FL-1003",
  "passengerName": "Alice Walker",
  "message": "Booking confirmed"
}
```

### 2. Booking when no seats are left
`FL-1003` is initialized with 1 seat. After one successful booking, a second booking should fail:

```bash
curl -i -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"flightNumber":"FL-1003","passengerName":"Bob Martin"}'
```

Expected:
- `409 Conflict`
- Error JSON response

Example:
```json
{
  "timestamp": "2026-04-04T14:20:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "No seats available for flight: FL-1003",
  "path": "/api/bookings",
  "details": null
}
```

### 3. Booking non-existent flight
```bash
curl -i -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"flightNumber":"FL-9999","passengerName":"Charlie Reed"}'
```

Expected:
- `404 Not Found`
- Error JSON response

Example:
```json
{
  "timestamp": "2026-04-04T14:21:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Flight not found: FL-9999",
  "path": "/api/bookings",
  "details": null
}
```

## Key Design Decisions

- In-memory storage only:
Seat inventory is stored in-memory to match assignment constraints and keep setup simple.

- Layered architecture:
`Controller -> Service` separation keeps controllers thin and business logic centralized.

- Consistent error handling:
`@ControllerAdvice` maps domain and validation exceptions to stable JSON responses:
`404` for unknown flight, `409` for sold-out flight, and `400` for request validation errors.

- Booking-only API surface:
No GET/retrieval endpoints and no flight management endpoints, per requirements.

## Concurrency Edge Case (Last Seat)

The system is designed so that if two users attempt to book the last seat at the same time, exactly one succeeds.

- Seat inventory uses `ConcurrentHashMap<String, AtomicInteger>`.
- Booking uses an atomic compare-and-set (CAS) loop on the per-flight `AtomicInteger`.
- Only one thread can transition seat count from `1 -> 0`.
- Any concurrent loser sees `0` and gets `NoSeatsAvailableException` (`409 Conflict`).

This prevents overbooking without explicit locks and is validated by the concurrent unit test in `BookingServiceTest`.
