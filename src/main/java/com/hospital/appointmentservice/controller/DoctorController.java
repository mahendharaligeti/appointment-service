package com.hospital.appointmentservice.controller;

import com.hospital.appointmentservice.dto.ApiResponse;
import com.hospital.appointmentservice.dto.DoctorRequest;
import com.hospital.appointmentservice.dto.DoctorResponse;
import com.hospital.appointmentservice.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAllActiveDoctors() {
        List<DoctorResponse> doctors = doctorService.getAllActiveDoctors();
        return ResponseEntity.ok(
            new ApiResponse<>("success", "Doctors retrieved successfully", doctors));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DoctorResponse>> getDoctorById(@PathVariable Long id) {
        DoctorResponse doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(
            new ApiResponse<>("success", "Doctor retrieved successfully", doctor));
    }

    @GetMapping("/specialization/{specialization}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getDoctorsBySpecialization(
            @PathVariable String specialization) {
        List<DoctorResponse> doctors = doctorService.getDoctorsBySpecialization(specialization);
        return ResponseEntity.ok(
            new ApiResponse<>("success", "Doctors retrieved successfully", doctors));
    }

    @GetMapping("/{id}/available-slots")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableSlots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<String> slots = doctorService.getAvailableSlots(id, date);
        return ResponseEntity.ok(
            new ApiResponse<>("success", "Available slots retrieved successfully", slots));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> addDoctor(@Valid @RequestBody DoctorRequest request) {
        DoctorResponse doctor = doctorService.addDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("success", "Doctor added successfully", doctor));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponse>> deactivateDoctor(@PathVariable Long id) {
        DoctorResponse doctor = doctorService.deactivateDoctor(id);
        return ResponseEntity.ok(
            new ApiResponse<>("success", "Doctor deactivated successfully", doctor));
    }
}
