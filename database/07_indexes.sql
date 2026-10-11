-- ============================================================================
-- PharmaCare Pharmacy Management System
-- Indexes Definition Script (07_indexes.sql)
-- Target RDBMS: MySQL 8.0+
-- Demonstrates: B-Tree Indexes, Composite Indexes, Query Optimization, EXPLAIN
-- ============================================================================

USE pharmacare_db;

-- ----------------------------------------------------------------------------
-- 1. INDEXES ON MEDICINE CATALOG
-- Optimizes autocomplete, text filtering, and relational joins
-- ----------------------------------------------------------------------------
CREATE INDEX idx_medicine_name ON medicine (name);
CREATE INDEX idx_medicine_generic ON medicine (generic_name);
CREATE INDEX idx_medicine_category_fk ON medicine (category_id);
CREATE INDEX idx_medicine_manufacturer_fk ON medicine (manufacturer_id);

-- ----------------------------------------------------------------------------
-- 2. INDEXES ON BATCH INVENTORY
-- Crucial for FEFO (First-Expired, First-Out) queries, shelf alerts, and joins
-- ----------------------------------------------------------------------------
CREATE INDEX idx_batch_expiry ON batch (expiry_date);
CREATE INDEX idx_batch_fefo ON batch (medicine_id, expiry_date, quantity);
CREATE INDEX idx_batch_status ON batch (status);
CREATE INDEX idx_batch_supplier_fk ON batch (supplier_id);

-- ----------------------------------------------------------------------------
-- 3. INDEXES ON SALES & BILLING
-- Optimizes date-range aggregations and customer purchase lookup
-- ----------------------------------------------------------------------------
CREATE INDEX idx_sale_date ON sale (sale_date);
CREATE INDEX idx_sale_customer_fk ON sale (customer_id);
CREATE INDEX idx_sale_employee_fk ON sale (employee_id);
CREATE INDEX idx_sale_status ON sale (status);

-- ----------------------------------------------------------------------------
-- 4. INDEXES ON SALE ITEMS & LINE DETAILS
-- ----------------------------------------------------------------------------
CREATE INDEX idx_saleitem_batch_fk ON sale_item (batch_id);
CREATE INDEX idx_saleitem_med_fk ON sale_item (medicine_id);

-- ----------------------------------------------------------------------------
-- 5. INDEXES ON PURCHASES & SUPPLIER TRANSACTIONS
-- ----------------------------------------------------------------------------
CREATE INDEX idx_purchase_date ON purchase (purchase_date);
CREATE INDEX idx_purchase_supplier_fk ON purchase (supplier_id);
CREATE INDEX idx_purchase_employee_fk ON purchase (employee_id);

-- ----------------------------------------------------------------------------
-- 6. INDEXES ON CUSTOMERS, DOCTORS & PRESCRIPTIONS
-- ----------------------------------------------------------------------------
CREATE INDEX idx_customer_name ON customer (name);
CREATE INDEX idx_prescription_date ON prescription (prescription_date);
CREATE INDEX idx_prescription_customer_fk ON prescription (customer_id);
CREATE INDEX idx_prescription_doctor_fk ON prescription (doctor_id);

-- ============================================================================
-- VERIFICATION: EXPLAIN QUERY PERFORMANCE EXAMPLES
-- Demonstrates optimizer index usage to faculty and reviewers
-- ============================================================================

-- Query 1: FEFO Batch Allocation (Uses composite index idx_batch_fefo)
EXPLAIN SELECT batch_id, batch_number, quantity, selling_price, expiry_date
FROM batch
WHERE medicine_id = 1
  AND expiry_date >= CURRENT_DATE
  AND quantity > 0
ORDER BY expiry_date ASC;

-- Query 2: Sales date-range report (Uses index idx_sale_date)
EXPLAIN SELECT sale_id, invoice_number, total_amount, sale_date
FROM sale
WHERE sale_date BETWEEN '2026-09-01' AND '2026-10-31';

-- Query 3: Medicine search filter (Uses index idx_medicine_name)
EXPLAIN SELECT medicine_id, name, dosage_form, rack_location
FROM medicine
WHERE name LIKE 'Dolo%';
