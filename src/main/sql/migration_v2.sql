-- V2 Migration: Add matching_result table for Audit Trail (Task 4)
-- Run this script manually in your MySQL client after upgrading the application.

CREATE TABLE IF NOT EXISTS matching_result
(
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    sample_id            INT          NOT NULL,
    drug_label_id        VARCHAR(100) NOT NULL,
    score                INT          DEFAULT 0,
    recommendation_level VARCHAR(20),
    matched_genes        TEXT,
    created_at           DATETIME     NOT NULL,
    INDEX idx_matching_result_sample (sample_id)
);
