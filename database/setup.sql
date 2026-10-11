-- ============================================================================
-- PharmaCare Pharmacy Management System
-- Master Setup & Provisioning Script (setup.sql)
-- Executes complete database initialization in sequence:
-- 1. Database Creation & User Provisioning
-- 2. Schema (01_schema.sql)
-- 3. Seed Data (02_seed.sql)
-- 4. Views (03_views.sql)
-- 5. Stored Functions (04_functions.sql)
-- 6. Triggers (05_triggers.sql)
-- 7. Stored Procedures (06_procedures.sql)
-- 8. Indexes (07_indexes.sql)
-- ============================================================================

-- Create database
CREATE DATABASE IF NOT EXISTS pharmacare_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pharmacare_db;

-- Provision dedicated application user with least-privilege security
CREATE USER IF NOT EXISTS 'pharmacare_user'@'localhost' IDENTIFIED BY 'PharmaCare@2026!';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON pharmacare_db.* TO 'pharmacare_user'@'localhost';
FLUSH PRIVILEGES;

-- Source all modular scripts in dependency order
-- (Ensure current working directory is database/ or run via mysql runner)
SOURCE 01_schema.sql;
SOURCE 02_seed.sql;
SOURCE 03_views.sql;
SOURCE 04_functions.sql;
SOURCE 05_triggers.sql;
SOURCE 06_procedures.sql;
SOURCE 07_indexes.sql;

SELECT 'PharmaCare database setup successfully completed!' AS Status;
