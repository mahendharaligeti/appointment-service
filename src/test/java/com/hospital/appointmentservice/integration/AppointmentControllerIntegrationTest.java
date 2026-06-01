package com.hospital.appointmentservice.integration;

import com.hospital.appointmentservice.dto.BookAppointmentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testBookAppointment_Unauthenticated_Returns401() throws Exception {
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setPatientId(101L);
        request.setPatientName("John Doe");
        request.setDoctorId(1L);
        request.setAppointmentDate(LocalDate.now().plusDays(5));
        request.setAppointmentTime("10:30 AM");

        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAppointmentsByPatientId_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/appointments/patient/101")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetUpcomingAppointments_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/appointments/patient/101/upcoming")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCancelAppointment_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(delete("/api/appointments/1")
                .param("patientId", "101")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAppointmentById_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/appointments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testBookAppointment_MissingFields_Returns400() throws Exception {
        BookAppointmentRequest request = new BookAppointmentRequest();
        
        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isBadRequest());
    }
}
