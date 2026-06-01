CREATE TABLE doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    specialization VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    available_from TIME,
    available_to TIME,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_email (email),
    INDEX idx_specialization (specialization),
    INDEX idx_is_active (is_active)
);

CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    patient_name VARCHAR(255) NOT NULL,
    doctor_id BIGINT NOT NULL,
    specialization VARCHAR(255) NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status VARCHAR(50) NOT NULL,
    reason VARCHAR(500),
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    INDEX idx_patient_id (patient_id),
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_appointment_date (appointment_date),
    INDEX idx_status (status),
    INDEX idx_patient_status (patient_id, status)
);
