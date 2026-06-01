package com.hospital.appointmentservice.service;

import com.hospital.appointmentservice.dto.AppointmentResponse;
import com.hospital.appointmentservice.dto.BookAppointmentRequest;
import com.hospital.appointmentservice.dto.UpdateAppointmentRequest;
import com.hospital.appointmentservice.entity.AppointmentStatus;
import java.util.List;

public interface AppointmentService {
    AppointmentResponse bookAppointment(BookAppointmentRequest request);
    AppointmentResponse cancelAppointment(Long appointmentId, Long requestingPatientId);
    List<AppointmentResponse> getAppointmentsByPatientId(Long patientId);
    List<AppointmentResponse> getAppointmentsByPatientIdAndStatus(Long patientId, 
                                                                   AppointmentStatus status);
    List<AppointmentResponse> getAppointmentsByDoctorId(Long doctorId);
    AppointmentResponse getAppointmentById(Long id);
    AppointmentResponse updateAppointment(Long id, UpdateAppointmentRequest request);
    List<AppointmentResponse> getUpcomingAppointments(Long patientId);
}
