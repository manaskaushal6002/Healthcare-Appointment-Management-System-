-- =====================================================================
-- Healthcare Appointment Management System
-- Database Setup and Seed Data Script
-- Compatible with: MySQL 8.0+ / MariaDB 10.4+
-- =====================================================================

-- Drop and recreate database to ensure a clean state
DROP DATABASE IF EXISTS healthcare_db;
CREATE DATABASE healthcare_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE healthcare_db;

-- ---------------------------------------------------------------------
-- Table 1: Admins
-- Manages administrative staff credentials and metadata
-- ---------------------------------------------------------------------
CREATE TABLE admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table 2: Patients
-- Stores patient profiles, authentication hashes, and medical baseline
-- ---------------------------------------------------------------------
CREATE TABLE patients (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    blood_group VARCHAR(5) NULL,
    address TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Table 3: Doctors
-- Medical practitioners available for appointment booking
-- ---------------------------------------------------------------------
CREATE TABLE doctors (
    doctor_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    qualification VARCHAR(100) NOT NULL,
    experience_years INT NOT NULL DEFAULT 0,
    consultation_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    available_days VARCHAR(100) NOT NULL DEFAULT 'Monday,Tuesday,Wednesday,Thursday,Friday',
    shift_start_time TIME NOT NULL DEFAULT '09:00:00',
    shift_end_time TIME NOT NULL DEFAULT '17:00:00',
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_doctor_exp CHECK (experience_years >= 0),
    CONSTRAINT chk_doctor_fee CHECK (consultation_fee >= 0.00)
) ENGINE=InnoDB;

-- Indexes on doctors for high-frequency search and filtering
CREATE INDEX idx_doctor_specialization ON doctors(specialization);
CREATE INDEX idx_doctor_active ON doctors(is_active);

-- ---------------------------------------------------------------------
-- Table 4: Appointments
-- Appointment schedule links patient and doctor with time and lifecycle status
-- ---------------------------------------------------------------------
CREATE TABLE appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'CONFIRMED',
    reason VARCHAR(255) NOT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointment_patient 
        FOREIGN KEY (patient_id) REFERENCES patients(patient_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_appointment_doctor 
        FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Composite index to accelerate collision detection (doctor slot verification)
CREATE INDEX idx_appointment_doc_slot ON appointments(doctor_id, appointment_date, appointment_time, status);
CREATE INDEX idx_appointment_patient ON appointments(patient_id);
CREATE INDEX idx_appointment_status ON appointments(status);

-- =====================================================================
-- SEED DATA (Realistic Sample Data for Testing and Demo)
-- =====================================================================

-- Default Administrator
-- Username: admin
-- Password: Admin@123
-- Salt: a1b2c3d4e5f67890123456789abcdef0
-- Hash (SHA-256 with Salt): 94f930dd1e9cd9fdb21e172c6d6440e3b0d0a0bc7bf89b38d721807f77f19b47
INSERT INTO admins (username, password_hash, salt, full_name, email) VALUES
('admin', '94f930dd1e9cd9fdb21e172c6d6440e3b0d0a0bc7bf89b38d721807f77f19b47', 'a1b2c3d4e5f67890123456789abcdef0', 'System Administrator', 'admin@healthcare.com');

-- Sample Doctors Across Specialties
INSERT INTO doctors (full_name, email, phone, specialization, qualification, experience_years, consultation_fee, available_days, shift_start_time, shift_end_time, is_active) VALUES
('Dr. Robert Vance', 'robert.vance@healthcare.com', '9876543210', 'Cardiology', 'MD, FACC (Harvard Medical)', 15, 120.00, 'Monday,Wednesday,Friday', '09:00:00', '16:00:00', 1),
('Dr. Elena Rostova', 'elena.rostova@healthcare.com', '9876543211', 'Dermatology', 'MD, DNB (Johns Hopkins)', 9, 90.00, 'Monday,Tuesday,Thursday', '10:00:00', '17:00:00', 1),
('Dr. Marcus Brody', 'marcus.brody@healthcare.com', '9876543212', 'Neurology', 'MD, PhD, DM (Oxford)', 18, 150.00, 'Tuesday,Thursday,Saturday', '09:00:00', '15:00:00', 1),
('Dr. Aisha Patel', 'aisha.patel@healthcare.com', '9876543213', 'Pediatrics', 'MBBS, DCH, MD (Stanford)', 11, 85.00, 'Monday,Tuesday,Wednesday,Friday', '09:00:00', '16:00:00', 1),
('Dr. David Kim', 'david.kim@healthcare.com', '9876543214', 'Orthopedics', 'MS (Ortho), MCh (Mayo Clinic)', 14, 110.00, 'Monday,Wednesday,Thursday', '11:00:00', '18:00:00', 1),
('Dr. Sarah Jenkins', 'sarah.jenkins@healthcare.com', '9876543215', 'General Medicine', 'MBBS, MD (Internal Medicine)', 8, 70.00, 'Monday,Tuesday,Wednesday,Thursday,Friday', '08:30:00', '16:30:00', 1);

-- Sample Patients
-- Patient 1: john.doe@example.com / Patient@123
-- Salt: f0e1d2c3b4a598765432109876543210
-- Hash: 09e453f7838405b8f8517ac1db64091d3e29ea8f63159c443581e64f4317b5c3
-- Patient 2: alice.smith@example.com / Patient@123
-- Salt: 11223344556677889900aabbccddeeff
-- Hash: 3991d0442c0128a37f4da7282b3cde6909b5149dad9c022f27dcb057c4a68678
INSERT INTO patients (full_name, email, password_hash, salt, phone, date_of_birth, gender, blood_group, address) VALUES
('John Doe', 'john.doe@example.com', '09e453f7838405b8f8517ac1db64091d3e29ea8f63159c443581e64f4317b5c3', 'f0e1d2c3b4a598765432109876543210', '555-010-2345', '1988-04-15', 'MALE', 'O+', '742 Evergreen Terrace, Springfield'),
('Alice Smith', 'alice.smith@example.com', '3991d0442c0128a37f4da7282b3cde6909b5149dad9c022f27dcb057c4a68678', '11223344556677889900aabbccddeeff', '555-010-6789', '1993-09-22', 'FEMALE', 'A+', '221B Baker Street, London');

-- Sample Appointments Across Diverse Lifecycle States
INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status, reason, notes) VALUES
(1, 1, '2026-11-25', '10:00:00', 'CONFIRMED', 'Routine cardiac stress test and ECG consultation', 'Patient reports mild shortness of breath during workouts.'),
(1, 6, '2026-11-10', '09:30:00', 'COMPLETED', 'Seasonal flu symptoms and persistent cough', 'Prescribed antihistamines and rest; advised 5-day follow up if fever persists.'),
(2, 2, '2026-11-26', '11:00:00', 'CONFIRMED', 'Annual skin screening and rash consultation', 'Rash noted on forearm for 2 weeks.'),
(2, 4, '2026-11-12', '14:00:00', 'CANCELLED', 'Follow-up for seasonal allergies', 'Cancelled by patient due to schedule conflict.');
