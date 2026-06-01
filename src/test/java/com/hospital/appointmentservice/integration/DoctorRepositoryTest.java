package com.hospital.appointmentservice.integration;

import com.hospital.appointmentservice.entity.Doctor;
import com.hospital.appointmentservice.repository.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DoctorRepositoryTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Test
    void testFindBySpecialization() {
        List<Doctor> doctors = doctorRepository.findBySpecializationIgnoreCase("Cardiology");
        assertThat(doctors).isNotEmpty();
        assertThat(doctors).allMatch(doc -> 
            doc.getSpecialization().equalsIgnoreCase("Cardiology"));
    }

    @Test
    void testFindByIsActiveTrue() {
        List<Doctor> doctors = doctorRepository.findByIsActiveTrue();
        assertThat(doctors).isNotEmpty();
        assertThat(doctors).allMatch(Doctor::getIsActive);
    }

    @Test
    void testExistsByEmail() {
        boolean exists = doctorRepository.existsByEmail("sharma@hospital.com");
        assertThat(exists).isTrue();
    }

    @Test
    void testFindByEmail() {
        Optional<Doctor> doctor = doctorRepository.findByEmail("sharma@hospital.com");
        assertThat(doctor).isPresent();
        assertThat(doctor.get().getName()).isEqualTo("Dr. Sharma");
    }

    @Test
    void testSaveAndRetrieve() {
        Doctor newDoctor = new Doctor(
            "Dr. New",
            "Orthopedics",
            "new@hospital.com",
            "9876543220",
            LocalTime.of(9, 0),
            LocalTime.of(17, 0));

        Doctor saved = doctorRepository.save(newDoctor);
        assertThat(saved.getId()).isNotNull();

        Optional<Doctor> retrieved = doctorRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("Dr. New");
    }
}
