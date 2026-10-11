-- ============================================================================
-- PharmaCare Pharmacy Management System
-- Views Definition Script (03_views.sql)
-- Target RDBMS: MySQL 8.0+
-- Demonstrates: SQL Views, Multi-Table Joins, Aggregations, Date Arithmetic
-- ============================================================================

USE pharmacare_db;

-- ----------------------------------------------------------------------------
-- Drop views if already existing
-- ----------------------------------------------------------------------------
DROP VIEW IF EXISTS vw_top_selling_medicines;
DROP VIEW IF EXISTS vw_medicine_inventory;
DROP VIEW IF EXISTS vw_supplier_purchase_summary;
DROP VIEW IF EXISTS vw_monthly_sales;
DROP VIEW IF EXISTS vw_expiring_batches;
DROP VIEW IF EXISTS vw_low_stock;

-- ============================================================================
-- 1. VIEW: vw_low_stock
-- Identifies medicines and active batches whose quantity is at or below
-- the designated reorder threshold.
-- ============================================================================
CREATE VIEW vw_low_stock AS
SELECT 
    m.medicine_id,
    m.name AS medicine_name,
    c.name AS category_name,
    man.name AS manufacturer_name,
    b.batch_id,
    b.batch_number,
    b.quantity AS batch_quantity,
    m.reorder_level,
    m.rack_location,
    b.expiry_date,
    CASE 
        WHEN b.quantity = 0 THEN 'OUT_OF_STOCK'
        WHEN b.quantity <= m.reorder_level THEN 'CRITICAL_LOW'
        ELSE 'LOW_STOCK'
    END AS alert_severity
FROM batch b
JOIN medicine m ON b.medicine_id = m.medicine_id
JOIN category c ON m.category_id = c.category_id
JOIN manufacturer man ON m.manufacturer_id = man.manufacturer_id
WHERE b.quantity <= m.reorder_level
  AND b.status = 'Available'
  AND b.expiry_date >= CURRENT_DATE;

-- ============================================================================
-- 2. VIEW: vw_expiring_batches
-- Tracks batches that are already expired or are expiring within 90 days.
-- Uses DATEDIFF and CURRENT_DATE.
-- ============================================================================
CREATE VIEW vw_expiring_batches AS
SELECT 
    b.batch_id,
    m.medicine_id,
    m.name AS medicine_name,
    c.name AS category_name,
    b.batch_number,
    b.quantity,
    b.selling_price,
    b.expiry_date,
    DATEDIFF(b.expiry_date, CURRENT_DATE) AS days_remaining,
    m.rack_location,
    CASE 
        WHEN b.expiry_date < CURRENT_DATE THEN 'EXPIRED'
        WHEN DATEDIFF(b.expiry_date, CURRENT_DATE) <= 30 THEN 'CRITICAL_EXPIRING_30D'
        WHEN DATEDIFF(b.expiry_date, CURRENT_DATE) <= 90 THEN 'EXPIRING_SOON_90D'
        ELSE 'OK'
    END AS expiry_status
FROM batch b
JOIN medicine m ON b.medicine_id = m.medicine_id
JOIN category c ON m.category_id = c.category_id
WHERE b.expiry_date <= DATE_ADD(CURRENT_DATE, INTERVAL 90 DAY);

-- ============================================================================
-- 3. VIEW: vw_monthly_sales
-- Aggregates sales volume, discounts, taxes, and net revenue by month.
-- ============================================================================
CREATE VIEW vw_monthly_sales AS
SELECT 
    YEAR(s.sale_date) AS sales_year,
    MONTH(s.sale_date) AS sales_month,
    DATE_FORMAT(s.sale_date, '%M %Y') AS month_label,
    COUNT(s.sale_id) AS total_invoices,
    COALESCE(SUM(s.subtotal), 0.00) AS gross_subtotal,
    COALESCE(SUM(s.discount_amount), 0.00) AS total_discounts,
    COALESCE(SUM(s.tax_amount), 0.00) AS total_tax,
    COALESCE(SUM(s.total_amount), 0.00) AS net_revenue,
    COALESCE(AVG(s.total_amount), 0.00) AS average_invoice_value
FROM sale s
WHERE s.status IN ('Completed', 'Partially_Refunded')
GROUP BY YEAR(s.sale_date), MONTH(s.sale_date), DATE_FORMAT(s.sale_date, '%M %Y');

-- ============================================================================
-- 4. VIEW: vw_supplier_purchase_summary
-- Summarizes procurement spending, order frequency, and vendor contacts.
-- ============================================================================
CREATE VIEW vw_supplier_purchase_summary AS
SELECT 
    sup.supplier_id,
    sup.name AS supplier_name,
    sup.contact_person,
    sup.phone,
    sup.gst_number,
    COUNT(p.purchase_id) AS total_orders,
    COALESCE(SUM(p.total_amount), 0.00) AS total_amount_spent,
    MAX(p.purchase_date) AS last_purchase_date
FROM supplier sup
LEFT JOIN purchase p ON sup.supplier_id = p.supplier_id
GROUP BY sup.supplier_id, sup.name, sup.contact_person, sup.phone, sup.gst_number;

-- ============================================================================
-- 5. VIEW: vw_medicine_inventory
-- Comprehensive view providing total stock per medicine across all batches.
-- ============================================================================
CREATE VIEW vw_medicine_inventory AS
SELECT 
    m.medicine_id,
    m.name AS medicine_name,
    m.generic_name,
    c.name AS category_name,
    man.name AS manufacturer_name,
    m.dosage_form,
    m.unit_pack,
    m.reorder_level,
    m.rack_location,
    COALESCE(SUM(CASE WHEN b.expiry_date >= CURRENT_DATE AND b.status = 'Available' THEN b.quantity ELSE 0 END), 0) AS total_available_stock,
    COALESCE(SUM(CASE WHEN b.expiry_date < CURRENT_DATE THEN b.quantity ELSE 0 END), 0) AS expired_stock,
    COUNT(b.batch_id) AS total_batch_count,
    CASE 
        WHEN COALESCE(SUM(CASE WHEN b.expiry_date >= CURRENT_DATE AND b.status = 'Available' THEN b.quantity ELSE 0 END), 0) = 0 THEN 'OUT_OF_STOCK'
        WHEN COALESCE(SUM(CASE WHEN b.expiry_date >= CURRENT_DATE AND b.status = 'Available' THEN b.quantity ELSE 0 END), 0) <= m.reorder_level THEN 'LOW_STOCK'
        ELSE 'ADEQUATE'
    END AS inventory_health
FROM medicine m
JOIN category c ON m.category_id = c.category_id
JOIN manufacturer man ON m.manufacturer_id = man.manufacturer_id
LEFT JOIN batch b ON m.medicine_id = b.medicine_id
WHERE m.is_active = TRUE
GROUP BY m.medicine_id, m.name, m.generic_name, c.name, man.name, m.dosage_form, m.unit_pack, m.reorder_level, m.rack_location;

-- ============================================================================
-- 6. VIEW: vw_top_selling_medicines
-- Evaluates sales velocity: top-performing medicines by quantity and revenue.
-- ============================================================================
CREATE VIEW vw_top_selling_medicines AS
SELECT 
    m.medicine_id,
    m.name AS medicine_name,
    c.name AS category_name,
    man.name AS manufacturer_name,
    COALESCE(SUM(si.quantity), 0) AS total_quantity_sold,
    COALESCE(SUM(si.subtotal), 0.00) AS total_revenue_generated,
    COUNT(DISTINCT si.sale_id) AS invoice_appearances
FROM medicine m
JOIN category c ON m.category_id = c.category_id
JOIN manufacturer man ON m.manufacturer_id = man.manufacturer_id
JOIN sale_item si ON m.medicine_id = si.medicine_id
JOIN sale s ON si.sale_id = s.sale_id
WHERE s.status IN ('Completed', 'Partially_Refunded')
GROUP BY m.medicine_id, m.name, c.name, man.name;
