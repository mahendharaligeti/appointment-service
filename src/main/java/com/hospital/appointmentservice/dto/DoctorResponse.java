package com.hospital.appointmentservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public class DoctorResponse {

    private Long id;
    private String name;
    private String specialization;
    private String email;
    private String phone;

    @JsonFormat(pattern = "hh:mm a")
    private LocalTime availableFrom;

    @JsonFormat(pattern = "hh:mm a")
    private LocalTime availableTo;

    private Boolean isActive;

    public DoctorResponse() {
    }

    public DoctorResponse(Long id, String name, String specialization, String email,
                         String phone, LocalTime availableFrom, LocalTime availableTo,
                         Boolean isActive) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.email = email;
        this.phone = phone;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalTime getAvailableFrom() {
        return availableFrom;
    }

    public void setAvailableFrom(LocalTime availableFrom) {
        this.availableFrom = availableFrom;
    }

    public LocalTime getAvailableTo() {
        return availableTo;
    }

    public void setAvailableTo(LocalTime availableTo) {
        this.availableTo = availableTo;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
