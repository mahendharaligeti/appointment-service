package com.hospital.appointmentservice;

import com.hospital.appointmentservice.config.ApplicationProfileConfig;
import com.hospital.appointmentservice.config.EnableApplicationProfiles;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableApplicationProfiles("dev")
public class AppointmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(AppointmentServiceApplication.class);
        ApplicationProfileConfig.applyProfiles(application, AppointmentServiceApplication.class);
        application.run(args);
    }
}
