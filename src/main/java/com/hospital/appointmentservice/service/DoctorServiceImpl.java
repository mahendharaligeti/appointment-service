package com.hospital.appointmentservice.service;

import com.hospital.appointmentservice.config.ObservedOperation;
import com.hospital.appointmentservice.dto.DoctorRequest;
import com.hospital.appointmentservice.dto.DoctorResponse;
import com.hospital.appointmentservice.entity.Appointment;
import com.hospital.appointmentservice.entity.AppointmentStatus;
import com.hospital.appointmentservice.entity.Doctor;
import com.hospital.appointmentservice.exception.DoctorNotFoundException;
import com.hospital.appointmentservice.repository.AppointmentRepository;
import com.hospital.appointmentservice.repository.DoctorRepository;
import com.hospital.appointmentservice.util.TimeSlotUtil;
import io.micrometer.common.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private static final Logger log = LoggerFactory.getLogger(DoctorServiceImpl.class);

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final ObservedOperation observedOperation;

    public DoctorServiceImpl(DoctorRepository doctorRepository,
                             AppointmentRepository appointmentRepository,
                             ObservedOperation observedOperation) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.observedOperation = observedOperation;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllActiveDoctors() {
        return observedOperation.observe("doctor.list-active", () ->
            doctorRepository.findByIsActiveTrue()
                    .stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long id) {
        return observedOperation.observe("doctor.get", () -> {
            Doctor doctor = doctorRepository.findById(id)
                    .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + id));
            return convertToResponse(doctor);
        }, KeyValue.of("doctor.id", String.valueOf(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getDoctorsBySpecialization(String specialization) {
        return observedOperation.observe("doctor.list-by-specialization", () ->
            doctorRepository.findBySpecializationIgnoreCase(specialization)
                    .stream()
                    .filter(Doctor::getIsActive)
                    .map(this::convertToResponse)
                    .collect(Collectors.toList()),
                KeyValue.of("doctor.specialization", specialization));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableSlots(Long doctorId, LocalDate date) {
        return observedOperation.observe("doctor.available-slots", () -> {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + doctorId));

            List<String> allSlots = TimeSlotUtil.generateAvailableSlots(
                    doctor.getAvailableFrom(),
                    doctor.getAvailableTo());

            List<Appointment> bookedAppointments = appointmentRepository
                    .findByDoctorIdAndAppointmentDate(doctorId, date);

            return allSlots.stream()
                    .filter(slot -> {
                        LocalTime slotTime = TimeSlotUtil.parseTimeString(slot);
                        return bookedAppointments.stream()
                                .filter(apt -> apt.getStatus() != AppointmentStatus.CANCELLED)
                                .noneMatch(apt -> apt.getAppointmentTime().equals(slotTime));
                    })
                    .collect(Collectors.toList());
        }, KeyValue.of("doctor.id", String.valueOf(doctorId)));
    }

    @Override
    public DoctorResponse addDoctor(DoctorRequest request) {
        return observedOperation.observe("doctor.add", () -> {
            log.info("Adding doctor specialization={}", request.getSpecialization());
            if (doctorRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Doctor with email already exists: " + request.getEmail());
            }

            Doctor doctor = new Doctor(
                    request.getName(),
                    request.getSpecialization(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getAvailableFrom(),
                    request.getAvailableTo());

            Doctor savedDoctor = doctorRepository.save(doctor);
            log.info("Doctor added doctorId={} specialization={}",
                    savedDoctor.getId(), savedDoctor.getSpecialization());
            return convertToResponse(savedDoctor);
        }, KeyValue.of("doctor.specialization", request.getSpecialization()));
    }

    @Override
    public DoctorResponse deactivateDoctor(Long id) {
        return observedOperation.observe("doctor.deactivate", () -> {
            log.info("Deactivating doctor doctorId={}", id);
            Doctor doctor = doctorRepository.findById(id)
                    .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + id));

            doctor.setIsActive(false);
            Doctor updatedDoctor = doctorRepository.save(doctor);
            log.info("Doctor deactivated doctorId={}", id);
            return convertToResponse(updatedDoctor);
        }, KeyValue.of("doctor.id", String.valueOf(id)));
    }

    private DoctorResponse convertToResponse(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getName(),
                doctor.getSpecialization(),
                doctor.getEmail(),
                doctor.getPhone(),
                doctor.getAvailableFrom(),
                doctor.getAvailableTo(),
                doctor.getIsActive());
    }
}
