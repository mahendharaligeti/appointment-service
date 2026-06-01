package com.hospital.appointmentservice.unit;

import com.hospital.appointmentservice.config.ObservedOperation;
import com.hospital.appointmentservice.dto.AppointmentResponse;
import com.hospital.appointmentservice.dto.BookAppointmentRequest;
import com.hospital.appointmentservice.entity.Appointment;
import com.hospital.appointmentservice.entity.AppointmentStatus;
import com.hospital.appointmentservice.entity.Doctor;
import com.hospital.appointmentservice.exception.AppointmentConflictException;
import com.hospital.appointmentservice.exception.AppointmentNotFoundException;
import com.hospital.appointmentservice.exception.DoctorNotAvailableException;
import com.hospital.appointmentservice.exception.InvalidAppointmentStateException;
import com.hospital.appointmentservice.repository.AppointmentRepository;
import com.hospital.appointmentservice.repository.DoctorRepository;
import com.hospital.appointmentservice.service.AppointmentServiceImpl;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    private AppointmentServiceImpl appointmentService;
    private Doctor testDoctor;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentServiceImpl(
                appointmentRepository,
                doctorRepository,
                new ObservedOperation(ObservationRegistry.create()));
        
        testDoctor = new Doctor("Dr. Sharma", "Cardiology", "sharma@test.com", "123",
            LocalTime.of(9, 0), LocalTime.of(17, 0));
        testDoctor.setId(1L);
        testDoctor.setIsActive(true);
    }

    @Test
    void testBookAppointment_Success() {
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setPatientId(101L);
        request.setPatientName("John Doe");
        request.setDoctorId(1L);
        request.setAppointmentDate(LocalDate.now().plusDays(5));
        request.setAppointmentTime("10:30 AM");

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                any(), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.save(any())).thenAnswer(invocation -> {
            Appointment apt = invocation.getArgument(0);
            apt.setId(1L);
            return apt;
        });

        AppointmentResponse result = appointmentService.bookAppointment(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(result.getPatientName()).isEqualTo("John Doe");
    }

    @Test
    void testBookAppointment_DoubleBooking_ThrowsConflictException() {
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setPatientId(101L);
        request.setPatientName("John Doe");
        request.setDoctorId(1L);
        request.setAppointmentDate(LocalDate.now().plusDays(5));
        request.setAppointmentTime("10:30 AM");

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.bookAppointment(request))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    void testBookAppointment_TimeOutsideDoctorWindow_ThrowsException() {
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setPatientId(101L);
        request.setPatientName("John Doe");
        request.setDoctorId(1L);
        request.setAppointmentDate(LocalDate.now().plusDays(5));
        request.setAppointmentTime("08:00 AM");

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));

        assertThatThrownBy(() -> appointmentService.bookAppointment(request))
                .isInstanceOf(DoctorNotAvailableException.class)
                .hasMessageContaining("outside");
    }

    @Test
    void testCancelAppointment_Success() {
        Appointment appointment = new Appointment(101L, "John Doe", 1L, "Cardiology",
            LocalDate.now().plusDays(5), LocalTime.of(10, 30));
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.PENDING);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.save(any())).thenReturn(appointment);

        AppointmentResponse result = appointmentService.cancelAppointment(1L, 101L);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void testCancelAppointment_WrongPatient_ThrowsAccessDeniedException() {
        Appointment appointment = new Appointment(101L, "John Doe", 1L, "Cardiology",
            LocalDate.now().plusDays(5), LocalTime.of(10, 30));
        appointment.setId(1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, 999L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("cancel their own");
    }

    @Test
    void testCancelAppointment_AlreadyCancelled_ThrowsInvalidStateException() {
        Appointment appointment = new Appointment(101L, "John Doe", 1L, "Cardiology",
            LocalDate.now().plusDays(5), LocalTime.of(10, 30));
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.CANCELLED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, 101L))
                .isInstanceOf(InvalidAppointmentStateException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    void testCancelAppointment_AppointmentNotFound_ThrowsNotFoundException() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.cancelAppointment(999L, 101L))
                .isInstanceOf(AppointmentNotFoundException.class);
    }

    @Test
    void testGetAppointmentById_Found() {
        Appointment appointment = new Appointment(101L, "John Doe", 1L, "Cardiology",
            LocalDate.now().plusDays(5), LocalTime.of(10, 30));
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.PENDING);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));

        AppointmentResponse result = appointmentService.getAppointmentById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }
}
