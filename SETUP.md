# Appointment Service - Setup Guide

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+ (for development)
- Git

## Quick Start - Local Development

### 1. Create MySQL Database

```sql
CREATE DATABASE appointment_service_db;
USE appointment_service_db;
```

### 2. Set Environment Variables

```bash
# Set JWT secret (must match user-service)
export JWT_SECRET="your-256-bit-base64-encoded-secret-key-here"

# Optional: Override user-service URL (default: http://localhost:8081)
export USER_SERVICE_BASE_URL="http://localhost:8081"
```

### 3. Build the Project

```bash
cd appointment-service
mvn clean package -DskipTests
```

### 4. Run Application

```bash
# Development mode (default)
java -jar target/appointment-service-1.0.0.jar \
  --spring.profiles.active=dev \
  --app.jwt.secret=$JWT_SECRET

# Or use Maven
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

The service will start on `http://localhost:8082`

### 5. Verify Health

```bash
curl http://localhost:8082/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

## Running Tests

### Unit Tests
```bash
mvn test -Dtest=DoctorServiceImplTest
mvn test -Dtest=AppointmentServiceImplTest
```

### Integration Tests
```bash
mvn test -Dtest=AppointmentRepositoryTest
mvn test -Dtest=DoctorRepositoryTest
mvn test -Dtest=DoctorControllerIntegrationTest
mvn test -Dtest=AppointmentControllerIntegrationTest
```

### All Tests
```bash
mvn test
```

## Seeding Test Data

When running in development mode, you can manually insert test data:

```sql
INSERT INTO doctors (name, specialization, email, phone, available_from, available_to, is_active, created_at) VALUES
('Dr. Sharma', 'Cardiology', 'sharma@hospital.com', '9876543210', '09:00:00', '17:00:00', true, NOW()),
('Dr. Patel', 'Dermatology', 'patel@hospital.com', '9876543211', '10:00:00', '18:00:00', true, NOW()),
('Dr. Kumar', 'Neurology', 'kumar@hospital.com', '9876543212', '08:00:00', '16:00:00', true, NOW());
```

## Docker Setup (Optional)

### Build Docker Image
```bash
docker build -t appointment-service:1.0.0 .
```

### Run with Docker Compose

Add to your docker-compose.yml:

```yaml
appointment-service:
  image: appointment-service:1.0.0
  container_name: appointment-service
  ports:
    - "8082:8082"
  environment:
    SPRING_PROFILES_ACTIVE: prod
    JWT_SECRET: ${JWT_SECRET}
    DB_URL: jdbc:mysql://mysql:3306/appointment_service_db
    DB_USERNAME: root
    DB_PASSWORD: ${DB_PASSWORD}
    USER_SERVICE_URL: http://user-service:8081
  depends_on:
    - mysql
  networks:
    - hospital-network
```

## Obtaining JWT Token

Since this service validates tokens from user-service, you'll need to:

1. Get a token from user-service:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password"}'
```

2. Use the token in appointment-service requests:
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8082/api/appointments/patient/1
```

## Example API Requests

### Get All Doctors (Public)
```bash
curl http://localhost:8082/api/doctors
```

### Book Appointment (Authenticated)
```bash
curl -X POST http://localhost:8082/api/appointments \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 101,
    "patientName": "John Doe",
    "doctorId": 1,
    "appointmentDate": "2026-06-15",
    "appointmentTime": "02:30 PM",
    "reason": "Regular checkup"
  }'
```

### Get Available Slots
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  "http://localhost:8082/api/doctors/1/available-slots?date=2026-06-15"
```

### Cancel Appointment
```bash
curl -X DELETE "http://localhost:8082/api/appointments/1?patientId=101" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Troubleshooting

### Issue: Connection to user-service fails
**Solution**: Verify user-service is running on port 8081 or update USER_SERVICE_URL

### Issue: JWT validation fails
**Solution**: Ensure JWT_SECRET environment variable matches the one used in user-service

### Issue: Database connection error
**Solution**: 
- Verify MySQL is running
- Check database credentials in application-dev.yml
- Ensure appointment_service_db exists

### Issue: Tests fail with context errors
**Solution**: 
- Set JWT_SECRET environment variable before running tests
- Ensure H2 driver is in test dependencies (already included)

## Production Deployment

### 1. Create Production Database

```sql
CREATE DATABASE appointment_service_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'app_user'@'%' IDENTIFIED BY 'strong_password';
GRANT ALL PRIVILEGES ON appointment_service_db.* TO 'app_user'@'%';
FLUSH PRIVILEGES;
```

### 2. Set Environment Variables

```bash
export JWT_SECRET="production-secret-key"
export DB_URL="jdbc:mysql://db-host:3306/appointment_service_db"
export DB_USERNAME="app_user"
export DB_PASSWORD="strong_password"
export USER_SERVICE_URL="http://user-service:8081"
export SPRING_PROFILES_ACTIVE="prod"
```

### 3. Run Application

```bash
java -jar appointment-service-1.0.0.jar
```

### 4. Monitor Application

```bash
curl http://localhost:8082/actuator/health/live
curl http://localhost:8082/actuator/health/ready
```

## IDE Setup

### IntelliJ IDEA
1. Open project: `File > Open > select appointment-service directory`
2. Configure SDK: `File > Project Structure > Project > SDK` (select Java 17)
3. Enable annotation processing: `Settings > Build, Execution, Deployment > Compiler > Annotation Processors > Enable annotation processing`

### VS Code
1. Install extensions:
   - Extension Pack for Java
   - Spring Boot Extension Pack
2. Open folder and let VS Code configure the workspace
3. Update settings.json:
   ```json
   {
     "java.configuration.runtimes": [
       {
         "name": "JavaSE-17",
         "path": "/path/to/java17",
         "default": true
       }
     ]
   }
   ```

## Common Development Tasks

### Create a New Controller Endpoint
1. Add method to existing controller or create new controller in `controller/` package
2. Implement corresponding service method
3. Add @PreAuthorize annotation for role-based access
4. Write unit tests for service logic
5. Write integration tests for controller

### Add New Entity
1. Create entity class in `entity/` package with JPA annotations
2. Create repository interface extending JpaRepository in `repository/` package
3. Create custom query methods in repository if needed
4. Run `mvn clean compile` to ensure no compilation errors

### Add New Exception
1. Create exception class extending RuntimeException in `exception/` package
2. Add handler method in GlobalExceptionHandler
3. Throw exception in service/controller as needed

### Database Schema Changes
1. Update entity in `entity/` package
2. For development: Set `spring.jpa.hibernate.ddl-auto=update` in application-dev.yml
3. For production: Create migration scripts and set `spring.jpa.hibernate.ddl-auto=validate`

## Additional Resources

- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Spring Security: https://spring.io/projects/spring-security
- JWT Documentation: https://jwt.io/
- MySQL Documentation: https://dev.mysql.com/doc/
- JPA Documentation: https://www.oracle.com/java/technologies/persistence-jsp.html
