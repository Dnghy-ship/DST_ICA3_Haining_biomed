-- V4 Migration: Add patient_profile table for personalized dosing PoC
-- Run this script manually in MySQL after upgrading the application.

CREATE TABLE IF NOT EXISTS patient_profile
(
    id        INT AUTO_INCREMENT PRIMARY KEY,
    sample_id INT            NOT NULL,
    age       INT,
    height    DECIMAL(10, 2),
    weight    DECIMAL(10, 2),
    gender    VARCHAR(50),
    CONSTRAINT fk_patient_profile_sample
        FOREIGN KEY (sample_id) REFERENCES sample (id)
            ON DELETE CASCADE,
    CONSTRAINT uk_patient_profile_sample
        UNIQUE (sample_id)
);

CREATE INDEX idx_patient_profile_sample_id ON patient_profile (sample_id);
