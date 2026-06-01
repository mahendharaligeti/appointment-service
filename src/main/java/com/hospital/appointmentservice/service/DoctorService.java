package com.hospital.appointmentservice.service;

import com.hospital.appointmentservice.dto.DoctorRequest;
import com.hospital.appointmentservice.dto.DoctorResponse;
import java.time.LocalDate;
import java.util.List;

public interface DoctorService {
    List<DoctorResponse> getAllActiveDoctors();
    DoctorResponse getDoctorById(Long id);
    List<DoctorResponse> getDoctorsBySpecialization(String specialization);
    List<String> getAvailableSlots(Long doctorId, LocalDate date);
    DoctorResponse addDoctor(DoctorRequest request);
    DoctorResponse deactivateDoctor(Long id);
}
