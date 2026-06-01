package com.hospital.appointmentservice.config;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ObservedOperation {

    private final ObservationRegistry observationRegistry;

    public ObservedOperation(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    public <T> T observe(String name, Supplier<T> supplier, KeyValue... keyValues) {
        Observation observation = Observation.createNotStarted(name, observationRegistry);
        for (KeyValue keyValue : keyValues) {
            observation.lowCardinalityKeyValue(keyValue);
        }
        return observation.observe(supplier);
    }
}
