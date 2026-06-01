INSERT INTO doctors (id, name, specialization, email, phone, available_from, available_to, is_active, created_at)
VALUES 
(1, 'Dr. Sharma', 'Cardiology', 'sharma@hospital.com', '9876543210', '09:00:00', '17:00:00', true, CURRENT_TIMESTAMP),
(2, 'Dr. Patel', 'Dermatology', 'patel@hospital.com', '9876543211', '10:00:00', '18:00:00', true, CURRENT_TIMESTAMP),
(3, 'Dr. Kumar', 'Neurology', 'kumar@hospital.com', '9876543212', '08:00:00', '16:00:00', true, CURRENT_TIMESTAMP);

INSERT INTO appointments (id, patient_id, patient_name, doctor_id, specialization, appointment_date, appointment_time, status, created_at, updated_at)
VALUES
(1, 101, 'Patient One', 1, 'Cardiology', DATE_ADD(CURDATE(), INTERVAL 5 DAY), '10:00:00', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 101, 'Patient One', 2, 'Dermatology', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '11:00:00', 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 102, 'Patient Two', 1, 'Cardiology', DATE_ADD(CURDATE(), INTERVAL -2 DAY), '14:00:00', 'CANCELLED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
