# Appointment Service Microservice

A production-ready Spring Boot 3.x microservice for managing doctor appointments and scheduling. Integrates with the Hospital Management System's User Service for JWT-based authentication.

## Project Overview

- **Java Version**: 17
- **Spring Boot Version**: 3.2.5
- **Build Tool**: Maven
- **Database**: MySQL (Dev/Prod), H2 (Test)
- **Authentication**: JWT (shared with user-service)
- **Port**: 8082

## Key Features

- JWT-based authentication and authorization
- Doctor and appointment management
- Double-booking prevention
- Time slot availability checking
- Role-based access control (PATIENT, DOCTOR, ADMIN)
- RESTful API with comprehensive error handling
- Comprehensive unit and integration tests
- H2 in-memory database for testing

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8+ (for development)
- Environment variables: `JWT_SECRET` (must match user-service)

## Project Structure

```
src/main/java/com/hospital/appointmentservice/
├── config/              # Security, JWT, and Spring configurations
├── controller/          # REST endpoints
├── dto/                 # Data Transfer Objects
├── entity/              # JPA entities
├── exception/           # Custom exceptions and global handler
├── repository/          # JPA repositories
├── service/             # Business logic
└── util/                # Utility classes (JWT, TimeSlot)

src/test/
├── unit/                # Unit tests (Mockito)
├── integration/         # Integration tests (Spring Boot Test)
└── smoke/               # Smoke tests
```

## Build and Run

### Build the project
```bash
mvn clean package
```

### Run in development mode
```bash
export JWT_SECRET="your-256-bit-secret-key-matching-user-service"
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Run in production mode
```bash
export JWT_SECRET="your-secret"
export DB_URL="jdbc:mysql://host:3306/appointment_db"
export DB_USERNAME="username"
export DB_PASSWORD="password"
export USER_SERVICE_URL="http://user-service:8081"
java -jar target/appointment-service-1.0.0.jar --spring.profiles.active=prod
```

### Run tests
```bash
mvn test
```

## API Endpoints

### Doctor Management
- `GET /api/doctors` - Get all active doctors (Public)
- `GET /api/doctors/{id}` - Get doctor by ID (Authenticated)
- `GET /api/doctors/specialization/{specialization}` - Get doctors by specialization (Authenticated)
- `GET /api/doctors/{id}/available-slots?date=YYYY-MM-DD` - Get available slots for a doctor (Authenticated)
- `POST /api/doctors` - Add new doctor (Admin Only)
- `PATCH /api/doctors/{id}/deactivate` - Deactivate doctor (Admin Only)

### Appointment Management
- `POST /api/appointments` - Book appointment (Patient/Doctor)
- `DELETE /api/appointments/{id}?patientId={patientId}` - Cancel appointment (Patient)
- `GET /api/appointments/patient/{patientId}` - Get patient's appointments (Patient/Admin)
- `GET /api/appointments/patient/{patientId}/upcoming` - Get upcoming appointments (Patient/Admin)
- `GET /api/appointments/patient/{patientId}/status/{status}` - Get appointments by status (Patient/Admin)
- `GET /api/appointments/doctor/{doctorId}` - Get doctor's appointments (Doctor/Admin)
- `GET /api/appointments/{id}` - Get appointment details (Patient/Doctor/Admin)
- `PATCH /api/appointments/{id}` - Update appointment (Doctor/Admin)

## Configuration Files

### application.yml
Base configuration with JWT secret and user-service URL.

### application-dev.yml
Development profile with MySQL local database and SQL logging enabled.

### application-prod.yml
Production profile with environment-based configuration and validation-only DDL.

### application-test.yml
Test profile with H2 in-memory database for integration tests.

## Database Schema

### doctors table
- id (PK, auto-increment)
- name (VARCHAR 255)
- specialization (VARCHAR 255)
- email (VARCHAR 255, UNIQUE)
- phone (VARCHAR 20)
- available_from (TIME)
- available_to (TIME)
- is_active (BOOLEAN, default TRUE)
- created_at (TIMESTAMP)

### appointments table
- id (PK, auto-increment)
- patient_id (BIGINT)
- patient_name (VARCHAR 255)
- doctor_id (BIGINT, FK)
- specialization (VARCHAR 255)
- appointment_date (DATE)
- appointment_time (TIME)
- status (VARCHAR 50 - ENUM)
- reason (VARCHAR 500)
- notes (VARCHAR 1000)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)

## Security

- JWT token validation on all endpoints except public doctor list and health check
- Stateless session management
- CSRF disabled (stateless API)
- Role-based access control via @PreAuthorize annotations
- Password encryption using BCrypt (when user auth is needed)

## Error Handling

All errors are returned in standard format:
```json
{
  "status": "error",
  "message": "Error description",
  "data": null
}
```

Common HTTP Status Codes:
- 200: Success
- 201: Created
- 400: Bad Request (validation errors, invalid state)
- 401: Unauthorized
- 403: Forbidden (role-based access denied)
- 404: Not Found
- 409: Conflict (double booking)
- 500: Internal Server Error

## Testing

The project includes:
- **Unit Tests** (Mockito): Service layer logic testing
- **Integration Tests** (Spring Boot Test + H2): Repository and controller testing
- **Smoke Tests**: Context loading and bean validation

Run all tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=SmokeTest
```

## Available Slot Generation

Doctor availability is divided into 30-minute slots. For example:
- Doctor available: 9:00 AM - 5:00 PM
- Generated slots: 09:00 AM, 09:30 AM, 10:00 AM, 10:30 AM, ..., 04:30 PM

Booked slots (excluding cancelled appointments) are filtered out from available slots.

## Time Format Support

Appointment times can be specified in multiple formats:
- 24-hour format: "14:30", "09:00"
- 12-hour format: "2:30 PM", "9:00 AM"
- JSON responses use 12-hour format with AM/PM

## Environment Variables

| Variable | Required | Example |
|----------|----------|---------|
| JWT_SECRET | Yes | your-256-bit-base64-secret-key |
| DB_URL | Prod | jdbc:mysql://localhost:3306/appointment_db |
| DB_USERNAME | Prod | root |
| DB_PASSWORD | Prod | password |
| USER_SERVICE_URL | Prod | http://user-service:8081 |

## Development Notes

- All service methods are transactional
- Read operations use `@Transactional(readOnly = true)`
- Appointment booking is atomic (no partial saves)
- Double booking is prevented at the database query level
- Doctor availability time window is validated before booking
- Cancelled appointments are excluded from double-booking checks

## Future Enhancements

- Appointment reminders via email/SMS
- Doctor ratings and reviews
- Bulk appointment cancellation
- Appointment history and analytics
- Patient availability preferences
- Calendar integration

## Support

For issues or questions, refer to the main Hospital Management System documentation.
