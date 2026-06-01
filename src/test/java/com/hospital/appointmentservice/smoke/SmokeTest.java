package com.hospital.appointmentservice.smoke;

import com.hospital.appointmentservice.controller.AppointmentController;
import com.hospital.appointmentservice.controller.DoctorController;
import com.hospital.appointmentservice.repository.AppointmentRepository;
import com.hospital.appointmentservice.repository.DoctorRepository;
import com.hospital.appointmentservice.service.AppointmentService;
import com.hospital.appointmentservice.service.DoctorService;
import com.hospital.appointmentservice.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SmokeTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentController appointmentController;

    @Autowired
    private DoctorController doctorController;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void contextLoads() {
    }

    @Test
    void appointmentRepositoryNotNull() {
        assertThat(appointmentRepository).isNotNull();
    }

    @Test
    void doctorRepositoryNotNull() {
        assertThat(doctorRepository).isNotNull();
    }

    @Test
    void appointmentServiceNotNull() {
        assertThat(appointmentService).isNotNull();
    }

    @Test
    void doctorServiceNotNull() {
        assertThat(doctorService).isNotNull();
    }

    @Test
    void appointmentControllerNotNull() {
        assertThat(appointmentController).isNotNull();
    }

    @Test
    void doctorControllerNotNull() {
        assertThat(doctorController).isNotNull();
    }

    @Test
    void jwtUtilNotNull() {
        assertThat(jwtUtil).isNotNull();
    }
}
