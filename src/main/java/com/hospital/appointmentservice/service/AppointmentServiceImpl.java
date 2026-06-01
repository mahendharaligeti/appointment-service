package com.hospital.appointmentservice.service;

import com.hospital.appointmentservice.dto.AppointmentResponse;
import com.hospital.appointmentservice.dto.BookAppointmentRequest;
import com.hospital.appointmentservice.dto.UpdateAppointmentRequest;
import com.hospital.appointmentservice.entity.Appointment;
import com.hospital.appointmentservice.entity.AppointmentStatus;
import com.hospital.appointmentservice.entity.Doctor;
import com.hospital.appointmentservice.exception.AppointmentConflictException;
import com.hospital.appointmentservice.exception.AppointmentNotFoundException;
import com.hospital.appointmentservice.exception.DoctorNotFoundException;
import com.hospital.appointmentservice.exception.DoctorNotAvailableException;
import com.hospital.appointmentservice.exception.InvalidAppointmentStateException;
import com.hospital.appointmentservice.repository.AppointmentRepository;
import com.hospital.appointmentservice.repository.DoctorRepository;
import com.hospital.appointmentservice.util.TimeSlotUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    public AppointmentResponse bookAppointment(BookAppointmentRequest request) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException(
                    "Doctor not found with id: " + request.getDoctorId()));

        if (!doctor.getIsActive()) {
            throw new DoctorNotAvailableException("Doctor is not available");
        }

        LocalTime appointmentTime = TimeSlotUtil.parseTimeString(request.getAppointmentTime());

        if (appointmentTime.isBefore(doctor.getAvailableFrom()) || 
            appointmentTime.isAfter(doctor.getAvailableTo())) {
            throw new DoctorNotAvailableException(
                "Appointment time is outside doctor's availability window. " +
                "Available: " + doctor.getAvailableFrom() + " to " + doctor.getAvailableTo());
        }

        boolean isBooked = appointmentRepository
                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                    request.getDoctorId(),
                    request.getAppointmentDate(),
                    appointmentTime,
                    AppointmentStatus.CANCELLED);

        if (isBooked) {
            throw new AppointmentConflictException(
                "Doctor is already booked for this date and time");
        }

        Appointment appointment = new Appointment(
                request.getPatientId(),
                request.getPatientName(),
                request.getDoctorId(),
                doctor.getSpecialization(),
                request.getAppointmentDate(),
                appointmentTime);

        appointment.setReason(request.getReason());

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToResponse(savedAppointment, doctor);
    }

    @Override
    public AppointmentResponse cancelAppointment(Long appointmentId, Long requestingPatientId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                    "Appointment not found with id: " + appointmentId));

        if (!appointment.getPatientId().equals(requestingPatientId)) {
            throw new AccessDeniedException("Patients can only cancel their own appointments");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentStateException("Appointment is already cancelled");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentStateException("Cannot cancel a completed appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment updatedAppointment = appointmentRepository.save(appointment);

        Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));

        return convertToResponse(updatedAppointment, doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(apt -> {
                    Doctor doctor = doctorRepository.findById(apt.getDoctorId())
                            .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
                    return convertToResponse(apt, doctor);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByPatientIdAndStatus(Long patientId,
                                                                          AppointmentStatus status) {
        return appointmentRepository.findByPatientIdAndStatus(patientId, status)
                .stream()
                .map(apt -> {
                    Doctor doctor = doctorRepository.findById(apt.getDoctorId())
                            .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
                    return convertToResponse(apt, doctor);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(apt -> {
                    Doctor doctor = doctorRepository.findById(apt.getDoctorId())
                            .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
                    return convertToResponse(apt, doctor);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(
                    "Appointment not found with id: " + id));

        Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));

        return convertToResponse(appointment, doctor);
    }

    @Override
    public AppointmentResponse updateAppointment(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(
                    "Appointment not found with id: " + id));

        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }

        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        if (request.getAppointmentDate() != null) {
            appointment.setAppointmentDate(request.getAppointmentDate());
        }

        if (request.getAppointmentTime() != null) {
            LocalTime newTime = TimeSlotUtil.parseTimeString(request.getAppointmentTime());
            Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                    .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));

            if (newTime.isBefore(doctor.getAvailableFrom()) || 
                newTime.isAfter(doctor.getAvailableTo())) {
                throw new DoctorNotAvailableException(
                    "New appointment time is outside doctor's availability window");
            }

            boolean isBooked = appointmentRepository
                    .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                        appointment.getDoctorId(),
                        appointment.getAppointmentDate(),
                        newTime,
                        AppointmentStatus.CANCELLED);

            if (isBooked) {
                throw new AppointmentConflictException(
                    "Doctor is already booked for this date and time");
            }

            appointment.setAppointmentTime(newTime);
        }

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));

        return convertToResponse(updatedAppointment, doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUpcomingAppointments(Long patientId) {
        LocalDate today = LocalDate.now();
        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .filter(apt -> !apt.getAppointmentDate().isBefore(today) && 
                       apt.getStatus() != AppointmentStatus.CANCELLED)
                .map(apt -> {
                    Doctor doctor = doctorRepository.findById(apt.getDoctorId())
                            .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
                    return convertToResponse(apt, doctor);
                })
                .collect(Collectors.toList());
    }

    private AppointmentResponse convertToResponse(Appointment appointment, Doctor doctor) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getPatientName(),
                appointment.getDoctorId(),
                doctor.getName(),
                appointment.getSpecialization(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getStatus(),
                appointment.getReason(),
                appointment.getNotes(),
                appointment.getCreatedAt());
    }
}
