# Appointment Service - Files Manifest

## Project Structure Overview

```
appointment-service/
├── pom.xml                          # Maven configuration with all dependencies
├── .gitignore                       # Git ignore patterns
├── Dockerfile                       # Container image definition
├── README.md                        # Project overview and documentation
├── SETUP.md                         # Setup and deployment guide
├── .env.example                     # Environment variables template
├── FILES_MANIFEST.md                # This file - complete file listing
│
├── src/main/
│   ├── java/com/hospital/appointmentservice/
│   │   ├── AppointmentServiceApplication.java  # Spring Boot entry point
│   │   │
│   │   ├── config/                  # Security and framework configurations
│   │   │   ├── SecurityConfig.java              # Spring Security configuration
│   │   │   ├── JwtAuthFilter.java              # JWT authentication filter
│   │   │   └── CustomUserDetailsService.java   # Custom user details service
│   │   │
│   │   ├── controller/              # REST API endpoints
│   │   │   ├── DoctorController.java            # Doctor management endpoints
│   │   │   └── AppointmentController.java       # Appointment management endpoints
│   │   │
│   │   ├── dto/                     # Data Transfer Objects
│   │   │   ├── ApiResponse.java                 # Generic API response wrapper
│   │   │   ├── BookAppointmentRequest.java      # Appointment booking DTO
│   │   │   ├── UpdateAppointmentRequest.java    # Appointment update DTO
│   │   │   ├── AppointmentResponse.java         # Appointment response DTO
│   │   │   ├── DoctorRequest.java               # Doctor creation DTO
│   │   │   └── DoctorResponse.java              # Doctor response DTO
│   │   │
│   │   ├── entity/                  # JPA entities
│   │   │   ├── Doctor.java                      # Doctor entity
│   │   │   ├── Appointment.java                 # Appointment entity
│   │   │   └── AppointmentStatus.java           # Appointment status enum
│   │   │
│   │   ├── exception/               # Exception handling
│   │   │   ├── GlobalExceptionHandler.java      # Global exception handler
│   │   │   ├── AppointmentNotFoundException.java
│   │   │   ├── AppointmentConflictException.java
│   │   │   ├── InvalidAppointmentStateException.java
│   │   │   ├── DoctorNotFoundException.java
│   │   │   └── DoctorNotAvailableException.java
│   │   │
│   │   ├── repository/              # JPA repositories
│   │   │   ├── AppointmentRepository.java       # Appointment data access
│   │   │   └── DoctorRepository.java            # Doctor data access
│   │   │
│   │   ├── service/                 # Business logic
│   │   │   ├── AppointmentService.java          # Appointment service interface
│   │   │   ├── AppointmentServiceImpl.java       # Appointment service implementation
│   │   │   ├── DoctorService.java               # Doctor service interface
│   │   │   └── DoctorServiceImpl.java            # Doctor service implementation
│   │   │
│   │   └── util/                    # Utility classes
│   │       ├── JwtUtil.java                     # JWT token utilities
│   │       └── TimeSlotUtil.java                # Time slot generation utilities
│   │
│   └── resources/
│       ├── application.yml              # Base Spring configuration
│       ├── application-dev.yml          # Development profile
│       ├── application-prod.yml         # Production profile
│       └── application-test.yml         # Test profile
│
└── src/test/
    ├── java/com/hospital/appointmentservice/
    │   ├── smoke/
    │   │   └── SmokeTest.java              # Context loading and bean verification tests
    │   │
    │   ├── unit/
    │   │   ├── DoctorServiceImplTest.java      # Unit tests for DoctorService
    │   │   └── AppointmentServiceImplTest.java # Unit tests for AppointmentService
    │   │
    │   └── integration/
    │       ├── DoctorRepositoryTest.java       # Integration tests for DoctorRepository
    │       ├── AppointmentRepositoryTest.java  # Integration tests for AppointmentRepository
    │       ├── DoctorControllerIntegrationTest.java     # Integration tests for DoctorController
    │       └── AppointmentControllerIntegrationTest.java # Integration tests for AppointmentController
    │
    └── resources/
        ├── schema.sql          # H2 database schema for tests
        └── data.sql            # Sample test data for H2 database
```

## File Descriptions

### Core Application Files

#### AppointmentServiceApplication.java
- Main Spring Boot application class
- Bootstraps the entire microservice
- Enables component scanning and auto-configuration

### Configuration Files (config/)

#### SecurityConfig.java
- Configures Spring Security for the application
- Enables JWT authentication
- Defines public vs protected endpoints
- Sets up stateless session management

#### JwtAuthFilter.java
- Extends OncePerRequestFilter for JWT processing
- Extracts Bearer token from Authorization header
- Validates token and sets SecurityContext

#### CustomUserDetailsService.java
- Implements UserDetailsService interface
- Loads user details from JWT token claims
- Creates UserDetails objects for authentication

### Controllers (controller/)

#### DoctorController.java
- `GET /api/doctors` - List all active doctors (public)
- `GET /api/doctors/{id}` - Get doctor details
- `GET /api/doctors/specialization/{spec}` - Filter by specialization
- `GET /api/doctors/{id}/available-slots` - Get available time slots
- `POST /api/doctors` - Add new doctor (admin)
- `PATCH /api/doctors/{id}/deactivate` - Deactivate doctor (admin)

#### AppointmentController.java
- `POST /api/appointments` - Book appointment
- `DELETE /api/appointments/{id}` - Cancel appointment
- `GET /api/appointments/patient/{id}` - List patient appointments
- `GET /api/appointments/patient/{id}/upcoming` - Get upcoming appointments
- `GET /api/appointments/doctor/{id}` - List doctor appointments
- `GET /api/appointments/{id}` - Get appointment details
- `PATCH /api/appointments/{id}` - Update appointment

### Data Transfer Objects (dto/)

#### ApiResponse.java
- Generic wrapper for all API responses
- Contains status, message, and optional data payload
- Ensures consistent response format across all endpoints

#### BookAppointmentRequest.java
- Request DTO for booking appointments
- Includes validation annotations (@NotNull, @Future, etc.)
- Supports both 12-hour and 24-hour time formats

#### UpdateAppointmentRequest.java
- Request DTO for updating appointments
- All fields are optional
- Supports status, notes, and datetime updates

#### AppointmentResponse.java
- Response DTO containing complete appointment information
- Includes doctor details and formatted dates/times
- Uses @JsonFormat for consistent time representation

#### DoctorRequest.java & DoctorResponse.java
- DTOs for doctor management operations
- Response includes availability window and active status
- Request validated for email format and required fields

### Entities (entity/)

#### Doctor.java
- JPA entity mapped to 'doctors' table
- Fields: id, name, specialization, email, phone, availableFrom, availableTo, isActive, createdAt
- Includes database indexes for common queries
- @PrePersist hook auto-sets createdAt timestamp

#### Appointment.java
- JPA entity mapped to 'appointments' table
- Fields: id, patientId, patientName, doctorId, doctor (relationship), specialization, 
  appointmentDate, appointmentTime, status, reason, notes, createdAt, updatedAt
- ManyToOne relationship with Doctor entity
- Includes composite indexes for query optimization
- @PrePersist and @PreUpdate hooks manage timestamps

#### AppointmentStatus.java
- Enum with four values: PENDING, CONFIRMED, CANCELLED, COMPLETED
- Used in database and application logic for type-safe status management

### Exceptions (exception/)

#### GlobalExceptionHandler.java
- Central exception handling using @RestControllerAdvice
- Maps custom exceptions to appropriate HTTP status codes
- Formats all error responses using ApiResponse

#### AppointmentNotFoundException.java
- Thrown when appointment record not found
- Results in HTTP 404 response

#### AppointmentConflictException.java
- Thrown when double-booking detected
- Results in HTTP 409 Conflict response

#### InvalidAppointmentStateException.java
- Thrown when invalid state transition attempted
- Results in HTTP 400 Bad Request response

#### DoctorNotFoundException.java
- Thrown when doctor record not found
- Results in HTTP 404 response

#### DoctorNotAvailableException.java
- Thrown when appointment time outside doctor's availability
- Results in HTTP 400 Bad Request response

### Repositories (repository/)

#### AppointmentRepository.java
- Extends JpaRepository<Appointment, Long>
- Custom query methods for finding appointments by:
  - Patient ID
  - Doctor ID
  - Patient ID and Status
  - Doctor ID and Date
  - Date range
- Double-booking check: existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot

#### DoctorRepository.java
- Extends JpaRepository<Doctor, Long>
- Custom query methods:
  - findBySpecializationIgnoreCase() - case-insensitive specialization search
  - findByIsActiveTrue() - active doctors only
  - findByEmail() - find by email address
  - existsByEmail() - email existence check

### Services (service/)

#### AppointmentService.java (Interface)
- Defines contract for appointment business logic
- Methods: bookAppointment, cancelAppointment, getAppointmentsByPatientId, 
  getAppointmentsByPatientIdAndStatus, getAppointmentsByDoctorId, getAppointmentById, 
  updateAppointment, getUpcomingAppointments

#### AppointmentServiceImpl.java
- Implements AppointmentService interface
- @Transactional on all write methods
- @Transactional(readOnly=true) on all read methods
- Includes double-booking prevention logic
- Validates doctor availability before booking
- Enforces permission checks for cancellation
- Converts entities to DTOs for responses

#### DoctorService.java (Interface)
- Defines contract for doctor business logic
- Methods: getAllActiveDoctors, getDoctorById, getDoctorsBySpecialization, 
  getAvailableSlots, addDoctor, deactivateDoctor

#### DoctorServiceImpl.java
- Implements DoctorService interface
- Generates 30-minute appointment slots
- Filters already-booked slots
- Validates doctor email uniqueness
- Enforces active status checks

### Utilities (util/)

#### JwtUtil.java
- JWT token generation and validation
- Methods: extractUsername, extractRole, extractClaim, isTokenValid, generateToken
- Uses JJWT library (io.jsonwebtoken)
- 256-bit secret key via environment variable
- Token expiration via app.jwt.expiration property

#### TimeSlotUtil.java
- Generates 30-minute appointment slots
- parseTimeString() - accepts multiple time formats (HH:mm, hh:mm a, etc.)
- formatTime() - formats LocalTime to 12-hour format with AM/PM
- generateAvailableSlots() - creates slot list between two times

### Configuration Files (resources/)

#### application.yml
- Base configuration used by all profiles
- JWT secret and expiration settings
- User service URL
- Actuator endpoints configuration
- JPA open-in-view disabled

#### application-dev.yml
- Development database configuration (MySQL)
- Schema auto-update enabled
- SQL logging enabled
- Hibernate dialect configuration

#### application-prod.yml
- Production database configuration (environment-based)
- Schema validation only (no DDL)
- SQL logging disabled
- Environment variable substitution

#### application-test.yml
- Test profile with H2 in-memory database
- Schema auto-create and drop
- No SQL logging
- H2 console disabled in production

### Test Files

#### SmokeTest.java
- Context loading verification
- Bean autowiring validation
- No complex business logic testing
- Runs quickly to verify basic setup

#### DoctorServiceImplTest.java
- Unit tests using Mockito
- Mocks DoctorRepository and AppointmentRepository
- Tests service layer logic in isolation
- Verifies response DTOs and error handling

#### AppointmentServiceImplTest.java
- Unit tests using Mockito
- Comprehensive booking logic testing
- Double-booking prevention verification
- Permission and state validation tests

#### DoctorRepositoryTest.java
- @DataJpaTest integration tests
- Uses H2 in-memory database
- Tests JPA query methods
- Verifies database constraints

#### AppointmentRepositoryTest.java
- @DataJpaTest integration tests
- Tests custom query methods
- Verifies double-booking detection logic
- Tests status-based filtering

#### DoctorControllerIntegrationTest.java
- @SpringBootTest with MockMvc
- Full application context testing
- Verifies endpoint responses
- Tests authentication requirements

#### AppointmentControllerIntegrationTest.java
- Full integration tests for appointment endpoints
- Tests request validation
- Verifies error responses
- Tests authentication enforcement

### Test Resources

#### schema.sql
- H2 database schema for integration tests
- Matches production entity structure
- Includes all indexes and constraints
- Executed before each test

#### data.sql
- Sample test data for H2 database
- Creates 3 sample doctors with different specializations
- Creates sample appointments with various statuses
- Data reset between test runs

### Root Configuration Files

#### pom.xml
- Maven build configuration
- Spring Boot 3.2.5 parent
- All required dependencies listed
- Compiler set to Java 17

#### README.md
- Project overview and features
- Build and run instructions
- API endpoint documentation
- Troubleshooting guide

#### SETUP.md
- Detailed setup procedures
- Database configuration
- Docker setup instructions
- Production deployment guide
- IDE setup guidelines

#### .gitignore
- Maven target directory
- IDE configuration files (.idea, .vscode)
- Build artifacts
- Environment files

#### Dockerfile
- Multi-stage build using Maven and JRE
- Alpine Linux for minimal image size
- Runs as non-root 'app' user
- Exposes port 8082

#### .env.example
- Template for environment variables
- Database credentials
- JWT configuration
- Service URL settings

## Build Artifacts

After `mvn clean package`, the following are generated:

```
target/
├── appointment-service-1.0.0.jar       # Executable JAR file (59 MB with dependencies)
├── classes/                             # Compiled Java classes
├── test-classes/                        # Compiled test classes
└── ...                                  # Maven build metadata
```

## Dependencies

Major dependencies included:

- Spring Boot 3.2.5 (web, data-jpa, security, actuator, webflux)
- Spring Security 6.2
- JWT (JJWT 0.11.5)
- MySQL Connector 8+
- H2 Database (test only)
- JUnit 5
- Mockito
- Spring Test

## Total File Count

- Java source files: 36 files
- Configuration files: 4 YAML files
- Test resource files: 2 SQL files
- Documentation files: 4 markdown files
- Docker/Build files: 2 files
- Configuration examples: 2 files

Total: ~50 files delivering a complete, production-ready microservice
