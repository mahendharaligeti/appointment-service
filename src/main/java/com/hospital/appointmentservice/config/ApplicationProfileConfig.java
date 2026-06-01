package com.hospital.appointmentservice.config;

import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;

@Configuration
public class ApplicationProfileConfig {

    public static void applyProfiles(SpringApplication application, Class<?> sourceClass) {
        EnableApplicationProfiles annotation =
                AnnotationUtils.findAnnotation(sourceClass, EnableApplicationProfiles.class);

        if (annotation != null && annotation.value().length > 0) {
            application.setDefaultProperties(Map.of(
                    "spring.profiles.default",
                    String.join(",", annotation.value())
            ));
        }
    }
}
