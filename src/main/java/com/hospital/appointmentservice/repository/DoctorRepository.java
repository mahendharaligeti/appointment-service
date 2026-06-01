package com.hospital.appointmentservice.repository;

import com.hospital.appointmentservice.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecializationIgnoreCase(String specialization);
    List<Doctor> findByIsActiveTrue();
    Optional<Doctor> findByEmail(String email);
    boolean existsByEmail(String email);
}
