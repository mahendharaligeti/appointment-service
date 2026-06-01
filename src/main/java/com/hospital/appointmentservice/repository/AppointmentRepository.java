package com.hospital.appointmentservice.repository;

import com.hospital.appointmentservice.entity.Appointment;
import com.hospital.appointmentservice.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);
    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
        Long doctorId, LocalDate date, LocalTime time, AppointmentStatus status);
    List<Appointment> findByAppointmentDateBetween(LocalDate start, LocalDate end);
    long countByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
}
