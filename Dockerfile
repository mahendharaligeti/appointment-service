# Multi-stage build
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Final stage
FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

COPY --from=builder --chown=app:app /build/target/appointment-service-1.0.0.jar appointment-service.jar

USER app

EXPOSE 8082

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "appointment-service.jar"]
