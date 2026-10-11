-- ============================================================================
-- PharmaCare Pharmacy Management System
-- Database Schema Script (01_schema.sql)
-- Target RDBMS: MySQL 8.0+
-- Demonstrates: DDL, Table Constraints, Foreign Keys, 3NF Normalization
-- ============================================================================

CREATE DATABASE IF NOT EXISTS pharmacare_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pharmacare_db;

-- ----------------------------------------------------------------------------
-- Drop existing tables in reverse dependency order for clean recreation
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS inventory_audit;
DROP TABLE IF EXISTS return_item;
DROP TABLE IF EXISTS sales_return;
DROP TABLE IF EXISTS prescription_item;
DROP TABLE IF EXISTS prescription;
DROP TABLE IF EXISTS sale_item;
DROP TABLE IF EXISTS sale;
DROP TABLE IF EXISTS purchase_item;
DROP TABLE IF EXISTS purchase;
DROP TABLE IF EXISTS batch;
DROP TABLE IF EXISTS medicine;
DROP TABLE IF EXISTS doctor;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS supplier;
DROP TABLE IF EXISTS manufacturer;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS role;

-- ============================================================================
-- 1. ROLE TABLE
-- Stores security roles for Role-Based Access Control (RBAC)
-- ============================================================================
CREATE TABLE role (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================================
-- 2. CATEGORY TABLE
-- Therapeutic and formulation drug categories (Tablets, Syrups, etc.)
-- ============================================================================
CREATE TABLE category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================================
-- 3. MANUFACTURER TABLE
-- Pharmaceutical manufacturing companies (Cipla, Sun Pharma, etc.)
-- Normalized in 3NF to avoid update anomalies for pharmaceutical brands
-- ============================================================================
CREATE TABLE manufacturer (
    manufacturer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    contact_email VARCHAR(100) NULL,
    phone VARCHAR(25) NULL,
    country VARCHAR(50) NOT NULL DEFAULT 'India',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================================
-- 4. SUPPLIER TABLE
-- Wholesale medicine distributors and logistics vendors
-- ============================================================================
CREATE TABLE supplier (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100) NULL,
    phone VARCHAR(25) NOT NULL,
    email VARCHAR(100) NULL,
    address TEXT NULL,
    gst_number VARCHAR(30) NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================================
-- 5. CUSTOMER TABLE
-- Pharmacy retail customers / patients
-- ============================================================================
CREATE TABLE customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(25) NOT NULL UNIQUE,
    email VARCHAR(100) NULL,
    address TEXT NULL,
    age INT NULL,
    gender ENUM('Male', 'Female', 'Other') NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_customer_age CHECK (age IS NULL OR (age >= 0 AND age <= 130))
) ENGINE=InnoDB;

-- ============================================================================
-- 6. EMPLOYEE TABLE
-- Pharmacy staff members (Pharmacists, Cashiers, Admins)
-- ============================================================================
CREATE TABLE employee (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    role_id INT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(25) NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_employee_role FOREIGN KEY (role_id)
        REFERENCES role (role_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- 7. DOCTOR TABLE
-- Medical practitioners issuing prescriptions
-- ============================================================================
CREATE TABLE doctor (
    doctor_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    hospital_clinic VARCHAR(150) NOT NULL,
    phone VARCHAR(25) NULL,
    email VARCHAR(100) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================================
-- 8. MEDICINE TABLE (Master Catalog)
-- Normalized catalog master (distinct from variable batches)
-- ============================================================================
CREATE TABLE medicine (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    manufacturer_id INT NOT NULL,
    name VARCHAR(150) NOT NULL,
    generic_name VARCHAR(150) NULL,
    dosage_form VARCHAR(50) NOT NULL,
    unit_pack VARCHAR(50) NOT NULL DEFAULT '1 Strip',
    reorder_level INT NOT NULL DEFAULT 10,
    rack_location VARCHAR(30) NOT NULL DEFAULT 'R1-A1',
    is_prescription_required BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medicine_category FOREIGN KEY (category_id)
        REFERENCES category (category_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_medicine_manufacturer FOREIGN KEY (manufacturer_id)
        REFERENCES manufacturer (manufacturer_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_medicine_reorder CHECK (reorder_level >= 0)
) ENGINE=InnoDB;

-- ============================================================================
-- 9. BATCH TABLE (Inventory Lots)
-- Represents physical inventory lots with distinct expiry dates & pricing
-- ============================================================================
CREATE TABLE batch (
    batch_id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id INT NOT NULL,
    supplier_id INT NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    manufacturing_date DATE NULL,
    expiry_date DATE NOT NULL,
    purchase_price DECIMAL(10,2) NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    status ENUM('Available', 'Expired', 'Recalled', 'Quarantine') NOT NULL DEFAULT 'Available',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_batch_medicine FOREIGN KEY (medicine_id)
        REFERENCES medicine (medicine_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_batch_supplier FOREIGN KEY (supplier_id)
        REFERENCES supplier (supplier_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT uq_medicine_batch UNIQUE (medicine_id, batch_number),
    CONSTRAINT chk_batch_qty CHECK (quantity >= 0),
    CONSTRAINT chk_batch_purchase_price CHECK (purchase_price >= 0),
    CONSTRAINT chk_batch_selling_price CHECK (selling_price >= 0)
) ENGINE=InnoDB;

-- ============================================================================
-- 10. PURCHASE TABLE (Header)
-- Purchase orders from wholesale drug distributors
-- ============================================================================
CREATE TABLE purchase (
    purchase_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id INT NOT NULL,
    employee_id INT NOT NULL,
    invoice_number VARCHAR(60) NOT NULL UNIQUE,
    purchase_date DATE NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    payment_status ENUM('Paid', 'Pending', 'Partial') NOT NULL DEFAULT 'Paid',
    notes TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_purchase_supplier FOREIGN KEY (supplier_id)
        REFERENCES supplier (supplier_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_purchase_employee FOREIGN KEY (employee_id)
        REFERENCES employee (employee_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_purchase_total CHECK (total_amount >= 0)
) ENGINE=InnoDB;

-- ============================================================================
-- 11. PURCHASE_ITEM TABLE (Line Items)
-- Individual items received in a supplier purchase
-- ============================================================================
CREATE TABLE purchase_item (
    purchase_item_id INT AUTO_INCREMENT PRIMARY KEY,
    purchase_id INT NOT NULL,
    medicine_id INT NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    expiry_date DATE NOT NULL,
    quantity INT NOT NULL,
    unit_cost DECIMAL(10,2) NOT NULL,
    total_cost DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_pitem_purchase FOREIGN KEY (purchase_id)
        REFERENCES purchase (purchase_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_pitem_medicine FOREIGN KEY (medicine_id)
        REFERENCES medicine (medicine_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_pitem_qty CHECK (quantity > 0),
    CONSTRAINT chk_pitem_cost CHECK (unit_cost >= 0 AND total_cost >= 0)
) ENGINE=InnoDB;

-- ============================================================================
-- 12. SALE TABLE (Header)
-- Retail sales invoices generated for customers
-- ============================================================================
CREATE TABLE sale (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(60) NOT NULL UNIQUE,
    customer_id INT NOT NULL,
    employee_id INT NOT NULL,
    sale_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_method ENUM('Cash', 'UPI', 'Card', 'Credit') NOT NULL DEFAULT 'Cash',
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    status ENUM('Completed', 'Refunded', 'Partially_Refunded', 'Cancelled') NOT NULL DEFAULT 'Completed',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sale_customer FOREIGN KEY (customer_id)
        REFERENCES customer (customer_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_sale_employee FOREIGN KEY (employee_id)
        REFERENCES employee (employee_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_sale_total CHECK (total_amount >= 0)
) ENGINE=InnoDB;

-- ============================================================================
-- 13. SALE_ITEM TABLE (Line Items)
-- Specific batch units sold in a sales invoice
-- ============================================================================
CREATE TABLE sale_item (
    sale_item_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    batch_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_sitem_sale FOREIGN KEY (sale_id)
        REFERENCES sale (sale_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_sitem_batch FOREIGN KEY (batch_id)
        REFERENCES batch (batch_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_sitem_medicine FOREIGN KEY (medicine_id)
        REFERENCES medicine (medicine_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT uq_sale_batch UNIQUE (sale_id, batch_id),
    CONSTRAINT chk_sitem_qty CHECK (quantity > 0),
    CONSTRAINT chk_sitem_price CHECK (unit_price >= 0 AND subtotal >= 0)
) ENGINE=InnoDB;

-- ============================================================================
-- 14. SALES_RETURN TABLE (Header)
-- Customer product return authorizations
-- ============================================================================
CREATE TABLE sales_return (
    return_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    customer_id INT NOT NULL,
    employee_id INT NOT NULL,
    return_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    refund_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    reason VARCHAR(255) NOT NULL,
    status ENUM('Approved', 'Rejected', 'Pending') NOT NULL DEFAULT 'Approved',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_return_sale FOREIGN KEY (sale_id)
        REFERENCES sale (sale_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_return_customer FOREIGN KEY (customer_id)
        REFERENCES customer (customer_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_return_employee FOREIGN KEY (employee_id)
        REFERENCES employee (employee_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_return_amount CHECK (refund_amount >= 0)
) ENGINE=InnoDB;

-- ============================================================================
-- 15. RETURN_ITEM TABLE (Line Items)
-- Items returned and returned to stock if eligible
-- ============================================================================
CREATE TABLE return_item (
    return_item_id INT AUTO_INCREMENT PRIMARY KEY,
    return_id INT NOT NULL,
    sale_item_id INT NOT NULL,
    batch_id INT NOT NULL,
    quantity INT NOT NULL,
    refund_unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    restocked BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_ritem_return FOREIGN KEY (return_id)
        REFERENCES sales_return (return_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_ritem_sitem FOREIGN KEY (sale_item_id)
        REFERENCES sale_item (sale_item_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_ritem_batch FOREIGN KEY (batch_id)
        REFERENCES batch (batch_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_ritem_qty CHECK (quantity > 0)
) ENGINE=InnoDB;

-- ============================================================================
-- 16. PRESCRIPTION TABLE (Header)
-- Patient medical prescriptions
-- ============================================================================
CREATE TABLE prescription (
    prescription_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    doctor_id INT NOT NULL,
    prescription_date DATE NOT NULL,
    diagnosis_notes TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rx_customer FOREIGN KEY (customer_id)
        REFERENCES customer (customer_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_rx_doctor FOREIGN KEY (doctor_id)
        REFERENCES doctor (doctor_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- 17. PRESCRIPTION_ITEM TABLE (Line Items)
-- Prescribed medications, dosages, and regimens
-- ============================================================================
CREATE TABLE prescription_item (
    prescription_item_id INT AUTO_INCREMENT PRIMARY KEY,
    prescription_id INT NOT NULL,
    medicine_id INT NOT NULL,
    dosage_instruction VARCHAR(100) NOT NULL,
    duration VARCHAR(50) NOT NULL,
    quantity_prescribed INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_rxitem_rx FOREIGN KEY (prescription_id)
        REFERENCES prescription (prescription_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_rxitem_med FOREIGN KEY (medicine_id)
        REFERENCES medicine (medicine_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_rxitem_qty CHECK (quantity_prescribed > 0)
) ENGINE=InnoDB;

-- ============================================================================
-- 18. INVENTORY_AUDIT TABLE
-- Immutable transaction audit log tracking all batch stock level changes
-- Populated automatically via MySQL triggers
-- ============================================================================
CREATE TABLE inventory_audit (
    audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id INT NOT NULL,
    medicine_id INT NOT NULL,
    change_type ENUM('SALE', 'PURCHASE', 'RETURN', 'MANUAL_ADJUSTMENT', 'INITIAL_STOCK') NOT NULL,
    old_quantity INT NOT NULL,
    new_quantity INT NOT NULL,
    quantity_change INT NOT NULL,
    changed_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255) NULL,
    INDEX idx_audit_batch (batch_id),
    INDEX idx_audit_date (changed_at)
) ENGINE=InnoDB;
