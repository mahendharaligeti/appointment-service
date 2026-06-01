package com.hospital.appointmentservice.exception;

public class DoctorNotAvailableException extends RuntimeException {
    public DoctorNotAvailableException(String message) {
        super(message);
    }

    public DoctorNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
