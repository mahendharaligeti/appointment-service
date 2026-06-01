package com.hospital.appointmentservice.unit;

import com.hospital.appointmentservice.dto.DoctorResponse;
import com.hospital.appointmentservice.entity.Doctor;
import com.hospital.appointmentservice.exception.DoctorNotFoundException;
import com.hospital.appointmentservice.repository.AppointmentRepository;
import com.hospital.appointmentservice.repository.DoctorRepository;
import com.hospital.appointmentservice.service.DoctorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    private DoctorServiceImpl doctorService;

    @BeforeEach
    void setUp() {
        doctorService = new DoctorServiceImpl(doctorRepository, appointmentRepository);
    }

    @Test
    void testGetAllActiveDoctors_ReturnsList() {
        Doctor doc1 = new Doctor("Dr. Sharma", "Cardiology", "sharma@test.com", "123", 
            LocalTime.of(9, 0), LocalTime.of(17, 0));
        Doctor doc2 = new Doctor("Dr. Patel", "Dermatology", "patel@test.com", "456",
            LocalTime.of(10, 0), LocalTime.of(18, 0));

        when(doctorRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(doc1, doc2));

        List<DoctorResponse> result = doctorService.getAllActiveDoctors();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(DoctorResponse::getIsActive);
    }

    @Test
    void testGetDoctorById_Found() {
        Doctor doctor = new Doctor("Dr. Sharma", "Cardiology", "sharma@test.com", "123",
            LocalTime.of(9, 0), LocalTime.of(17, 0));
        doctor.setId(1L);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        DoctorResponse result = doctorService.getDoctorById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Dr. Sharma");
    }

    @Test
    void testGetDoctorById_NotFound_ThrowsException() {
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.getDoctorById(999L))
                .isInstanceOf(DoctorNotFoundException.class)
                .hasMessageContaining("Doctor not found");
    }

    @Test
    void testGetDoctorsBySpecialization_ReturnsList() {
        Doctor doc1 = new Doctor("Dr. Sharma", "Cardiology", "sharma@test.com", "123",
            LocalTime.of(9, 0), LocalTime.of(17, 0));
        doc1.setIsActive(true);

        when(doctorRepository.findBySpecializationIgnoreCase("Cardiology"))
                .thenReturn(Arrays.asList(doc1));

        List<DoctorResponse> result = doctorService.getDoctorsBySpecialization("Cardiology");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpecialization()).isEqualTo("Cardiology");
    }

    @Test
    void testGetAvailableSlots_ReturnsFilteredSlots() {
        Doctor doctor = new Doctor("Dr. Sharma", "Cardiology", "sharma@test.com", "123",
            LocalTime.of(9, 0), LocalTime.of(17, 0));
        doctor.setId(1L);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctorIdAndAppointmentDate(any(), any()))
                .thenReturn(new ArrayList<>());

        List<String> result = doctorService.getAvailableSlots(1L, 
            java.time.LocalDate.now().plusDays(1));

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(slot -> slot.contains(":") && slot.contains("M"));
    }
}
