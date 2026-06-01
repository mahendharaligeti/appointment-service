package com.hospital.appointmentservice.integration;

import com.hospital.appointmentservice.entity.Appointment;
import com.hospital.appointmentservice.entity.AppointmentStatus;
import com.hospital.appointmentservice.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    void testFindByPatientId() {
        List<Appointment> appointments = appointmentRepository.findByPatientId(101L);
        assertThat(appointments).isNotEmpty();
        assertThat(appointments).allMatch(apt -> apt.getPatientId().equals(101L));
    }

    @Test
    void testFindByDoctorIdAndAppointmentDate() {
        LocalDate futureDate = LocalDate.now().plusDays(5);
        List<Appointment> appointments = 
            appointmentRepository.findByDoctorIdAndAppointmentDate(1L, futureDate);
        assertThat(appointments).isNotEmpty();
    }

    @Test
    void testDoubleBookingCheck_SameSlot_ReturnsTrue() {
        LocalDate futureDate = LocalDate.now().plusDays(5);
        LocalTime time = LocalTime.of(10, 0);
        
        boolean exists = appointmentRepository
                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                    1L, futureDate, time, AppointmentStatus.CANCELLED);
        assertThat(exists).isTrue();
    }

    @Test
    void testDoubleBookingCheck_CancelledSlot_ReturnsFalse() {
        LocalDate pastDate = LocalDate.now().minusDays(2);
        LocalTime time = LocalTime.of(14, 0);
        
        boolean exists = appointmentRepository
                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                    1L, pastDate, time, AppointmentStatus.CANCELLED);
        assertThat(exists).isFalse();
    }

    @Test
    void testFindByPatientIdAndStatus() {
        List<Appointment> appointments = 
            appointmentRepository.findByPatientIdAndStatus(101L, AppointmentStatus.PENDING);
        assertThat(appointments).isNotEmpty();
        assertThat(appointments).allMatch(apt -> apt.getStatus() == AppointmentStatus.PENDING);
    }
}
