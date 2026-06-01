package com.hospital.appointmentservice.exception;

public class InvalidAppointmentStateException extends RuntimeException {
    public InvalidAppointmentStateException(String message) {
        super(message);
    }

    public InvalidAppointmentStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
