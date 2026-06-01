package com.hospital.appointmentservice.controller;

import com.hospital.appointmentservice.dto.ApiResponse;
import com.hospital.appointmentservice.dto.AppointmentResponse;
import com.hospital.appointmentservice.dto.BookAppointmentRequest;
import com.hospital.appointmentservice.dto.UpdateAppointmentRequest;
import com.hospital.appointmentservice.entity.AppointmentStatus;
import com.hospital.appointmentservice.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> bookAppointment(
            @Valid @RequestBody BookAppointmentRequest request) {
        AppointmentResponse appointment = appointmentService.bookAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("success", "Appointment booked successfully", appointment));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @PathVariable Long id,
            @RequestParam Long patientId) {
        AppointmentResponse appointment = appointmentService.cancelAppointment(id, patientId);
        return ResponseEntity.ok(
            new ApiResponse<>("success", "Appointment cancelled successfully", appointment));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointmentsByPatientId(
            @PathVariable Long patientId) {
        List<AppointmentResponse> appointments = 
            appointmentService.getAppointmentsByPatientId(patientId);
        return ResponseEntity.ok(
            new ApiResponse<>("success", "Appointments retrieved successfully", appointments));
    }

    @GetMapping("/patient/{patientId}/upcoming")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getUpcomingAppointments(
            @PathVariable Long patientId) {
        List<AppointmentResponse> appointments = 
            appointmentService.getUpcomingAppointments(patientId);
        return ResponseEntity.ok(
            new ApiResponse<>("success", "Upcoming appointments retrieved successfully", appointments));
    }

    @GetMapping("/patient/{patientId}/status/{status}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointmentsByPatientIdAndStatus(
            @PathVariable Long patientId,
            @PathVariable AppointmentStatus status) {
        List<AppointmentResponse> appointments = 
            appointmentService.getAppointmentsByPatientIdAndStatus(patientId, status);
        return ResponseEntity.ok(
            new ApiResponse<>("success", "Appointments retrieved successfully", appointments));
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointmentsByDoctorId(
            @PathVariable Long doctorId) {
        List<AppointmentResponse> appointments = 
            appointmentService.getAppointmentsByDoctorId(doctorId);
        return ResponseEntity.ok(
            new ApiResponse<>("success", "Appointments retrieved successfully", appointments));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(
            @PathVariable Long id) {
        AppointmentResponse appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(
            new ApiResponse<>("success", "Appointment retrieved successfully", appointment));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointment(
            @PathVariable Long id,
            @RequestBody UpdateAppointmentRequest request) {
        AppointmentResponse appointment = appointmentService.updateAppointment(id, request);
        return ResponseEntity.ok(
            new ApiResponse<>("success", "Appointment updated successfully", appointment));
    }
}
