-- V3 Migration: Vertical partitioning for annovar wide table
-- Run this script manually in MySQL after upgrading the application.

SET @annovar_exists := (
    SELECT COUNT(*) FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'annovar'
);
SET @legacy_exists := (
    SELECT COUNT(*) FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'annovar_legacy'
);
SET @rename_sql := IF(@annovar_exists = 1 AND @legacy_exists = 0,
                      'RENAME TABLE annovar TO annovar_legacy',
                      'SELECT 1');
PREPARE rename_stmt FROM @rename_sql;
EXECUTE rename_stmt;
DEALLOCATE PREPARE rename_stmt;

CREATE TABLE IF NOT EXISTS variant_core
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    sample_id  INT         NOT NULL,
    chr        VARCHAR(64) NOT NULL,
    start_pos  VARCHAR(64) NOT NULL,
    end_pos    VARCHAR(64) NOT NULL,
    ref_allele VARCHAR(255),
    alt_allele VARCHAR(255),
    CONSTRAINT fk_variant_core_sample
        FOREIGN KEY (sample_id) REFERENCES sample (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS variant_annotation
(
    variant_id           INT PRIMARY KEY,
    gene_symbol          VARCHAR(1024),
    acmg_classification  VARCHAR(255),
    CONSTRAINT fk_variant_annotation_variant
        FOREIGN KEY (variant_id) REFERENCES variant_core (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS variant_bio_details
(
    variant_id   INT PRIMARY KEY,
    raw_details  JSON NOT NULL,
    CONSTRAINT fk_variant_bio_details_variant
        FOREIGN KEY (variant_id) REFERENCES variant_core (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_variant_core_sample_id_btree ON variant_core (sample_id);

-- MySQL secondary index on InnoDB is non-clustered.
-- Use prefix index to avoid "Specified key was too long" on utf8mb4 when gene_symbol is VARCHAR(1024).
SET @gene_symbol_idx_exists := (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'variant_annotation'
      AND index_name = 'idx_variant_annotation_gene_symbol'
);
SET @create_gene_symbol_idx_sql := IF(
    @gene_symbol_idx_exists = 0,
    'CREATE INDEX idx_variant_annotation_gene_symbol ON variant_annotation (gene_symbol(191))',
    'SELECT 1'
);
PREPARE create_gene_symbol_idx_stmt FROM @create_gene_symbol_idx_sql;
EXECUTE create_gene_symbol_idx_stmt;
DEALLOCATE PREPARE create_gene_symbol_idx_stmt;
