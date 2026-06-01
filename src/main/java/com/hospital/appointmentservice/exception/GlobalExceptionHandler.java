package com.hospital.appointmentservice.exception;

import com.hospital.appointmentservice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppointmentNotFound(
            AppointmentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>("error", ex.getMessage()));
    }

    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleDoctorNotFound(
            DoctorNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>("error", ex.getMessage()));
    }

    @ExceptionHandler(AppointmentConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppointmentConflict(
            AppointmentConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidAppointmentStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidAppointmentState(
            InvalidAppointmentStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("error", ex.getMessage()));
    }

    @ExceptionHandler(DoctorNotAvailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleDoctorNotAvailable(
            DoctorNotAvailableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("error", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>("error", "Access denied: " + ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(
            MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("error", "Validation failed: " + errors, errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("error", 
                    "An unexpected error occurred: " + ex.getMessage()));
    }
}
