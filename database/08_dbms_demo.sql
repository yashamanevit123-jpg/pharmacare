-- ============================================================================
-- PharmaCare Pharmacy Management System
-- DBMS Concepts Live Demonstration Script (08_dbms_demo.sql)
-- Designed for College Practical Lab & Faculty Viva Demonstration
-- ============================================================================

USE pharmacare_db;

SELECT '====================================================================' AS '';
SELECT 'PHARMACARE DBMS CONCEPTS DEMONSTRATION' AS 'DEMO SUITE';
SELECT '====================================================================' AS '';

-- ============================================================================
-- SECTION 1: RELATIONAL QUERIES (SELECT, PROJECTION, RESTRICTION)
-- ============================================================================

-- 1.1 Simple Restriction & Projection (WHERE, ORDER BY)
SELECT '1.1 Active Medicines in R1 shelves sorted by name' AS 'Demo Step';
SELECT medicine_id, name, dosage_form, unit_pack, rack_location 
FROM medicine
WHERE rack_location LIKE 'R1%' AND is_active = TRUE
ORDER BY name ASC;

-- 1.2 Multi-Table Inner Join (Medicine, Category, Manufacturer)
SELECT '1.2 Inner Join: Medicine Catalog with Category and Manufacturer' AS 'Demo Step';
SELECT 
    m.medicine_id,
    m.name AS medicine_name,
    c.name AS category,
    man.name AS manufacturer,
    m.reorder_level,
    m.rack_location
FROM medicine m
INNER JOIN category c ON m.category_id = c.category_id
INNER JOIN manufacturer man ON m.manufacturer_id = man.manufacturer_id
LIMIT 8;

-- 1.3 Left Outer Join (Medicines and their Batches, including lot quantities)
SELECT '1.3 Left Join: Medicines with Batch Lot Quantities' AS 'Demo Step';
SELECT 
    m.name AS medicine_name,
    COALESCE(b.batch_number, 'NO_BATCH') AS batch_number,
    COALESCE(b.quantity, 0) AS units_in_stock,
    b.expiry_date
FROM medicine m
LEFT JOIN batch b ON m.medicine_id = b.medicine_id
LIMIT 10;

-- ============================================================================
-- SECTION 2: AGGREGATIONS & GROUP BY WITH HAVING
-- ============================================================================

-- 2.1 Group By with Aggregations (COUNT, SUM, AVG, MIN, MAX)
SELECT '2.1 Category Aggregations: Stock count, average and max price' AS 'Demo Step';
SELECT 
    c.name AS category_name,
    COUNT(DISTINCT m.medicine_id) AS total_medicines,
    COALESCE(SUM(b.quantity), 0) AS total_units_in_stock,
    ROUND(COALESCE(AVG(b.selling_price), 0), 2) AS avg_selling_price,
    COALESCE(MAX(b.selling_price), 0.00) AS highest_price
FROM category c
LEFT JOIN medicine m ON c.category_id = m.category_id
LEFT JOIN batch b ON m.medicine_id = b.medicine_id
GROUP BY c.category_id, c.name
ORDER BY total_units_in_stock DESC;

-- 2.2 GROUP BY with HAVING Clause
SELECT '2.2 Suppliers with procurement volume greater than 1 order' AS 'Demo Step';
SELECT 
    s.name AS supplier_name,
    COUNT(p.purchase_id) AS order_count,
    SUM(p.total_amount) AS total_procurement_cost
FROM supplier s
JOIN purchase p ON s.supplier_id = p.supplier_id
GROUP BY s.supplier_id, s.name
HAVING COUNT(p.purchase_id) >= 1
ORDER BY total_procurement_cost DESC;

-- ============================================================================
-- SECTION 3: SUBQUERIES (SCALAR, IN, CORRELATED)
-- ============================================================================

-- 3.1 Scalar Subquery: Medicines with selling price above overall catalog average
SELECT '3.1 Scalar Subquery: Batches priced above global average selling price' AS 'Demo Step';
SELECT 
    b.batch_number,
    m.name AS medicine_name,
    b.selling_price,
    ROUND((SELECT AVG(selling_price) FROM batch WHERE status = 'Available'), 2) AS overall_avg_price
FROM batch b
JOIN medicine m ON b.medicine_id = m.medicine_id
WHERE b.selling_price > (SELECT AVG(selling_price) FROM batch WHERE status = 'Available')
  AND b.status = 'Available'
LIMIT 5;

-- 3.2 Subquery with IN: Customers who have made purchases via UPI payment
SELECT '3.2 Subquery with IN: Customers who purchased using UPI' AS 'Demo Step';
SELECT customer_id, name, phone, address
FROM customer
WHERE customer_id IN (
    SELECT DISTINCT customer_id FROM sale WHERE payment_method = 'UPI'
);

-- 3.3 Correlated Subquery: Highest priced batch for each medicine
SELECT '3.3 Correlated Subquery: Batches with max price for that specific medicine' AS 'Demo Step';
SELECT 
    b1.medicine_id,
    m.name AS medicine_name,
    b1.batch_number,
    b1.selling_price
FROM batch b1
JOIN medicine m ON b1.medicine_id = m.medicine_id
WHERE b1.selling_price = (
    SELECT MAX(b2.selling_price) 
    FROM batch b2 
    WHERE b2.medicine_id = b1.medicine_id
)
LIMIT 6;

-- ============================================================================
-- SECTION 4: DATE & STRING MANIPULATION FUNCTIONS
-- ============================================================================
SELECT '4.1 Date and String Functions Demonstration' AS 'Demo Step';
SELECT 
    UPPER(m.name) AS uppercase_name,
    CONCAT(m.name, ' (Pack: ', m.unit_pack, ')') AS packaging_label,
    b.batch_number,
    DATE_FORMAT(b.expiry_date, '%d-%b-%Y') AS formatted_expiry,
    DATEDIFF(b.expiry_date, CURRENT_DATE) AS days_until_expiry
FROM medicine m
JOIN batch b ON m.medicine_id = b.medicine_id
WHERE b.status = 'Available'
LIMIT 5;

-- ============================================================================
-- SECTION 5: SQL VIEWS DEMONSTRATION
-- ============================================================================
SELECT '5.1 View: vw_low_stock' AS 'Demo Step';
SELECT * FROM vw_low_stock LIMIT 5;

SELECT '5.2 View: vw_expiring_batches' AS 'Demo Step';
SELECT * FROM vw_expiring_batches LIMIT 5;

SELECT '5.3 View: vw_monthly_sales' AS 'Demo Step';
SELECT * FROM vw_monthly_sales;

SELECT '5.4 View: vw_supplier_purchase_summary' AS 'Demo Step';
SELECT * FROM vw_supplier_purchase_summary LIMIT 5;

-- ============================================================================
-- SECTION 6: STORED FUNCTIONS DEMONSTRATION
-- ============================================================================
SELECT '6.1 Stored Function: fn_stock_status across different batch types' AS 'Demo Step';
SELECT 
    b.batch_id,
    b.batch_number,
    m.name AS medicine_name,
    b.quantity,
    b.expiry_date,
    fn_stock_status(b.batch_id) AS computed_status
FROM batch b
JOIN medicine m ON b.medicine_id = m.medicine_id
WHERE b.batch_id IN (1, 19, 21, 28, 29)
ORDER BY b.batch_id;

SELECT '6.2 Stored Function: fn_inventory_value (Individual & Total)' AS 'Demo Step';
SELECT 
    fn_inventory_value(1) AS dolo_stock_value_inr,
    fn_inventory_value(2) AS crocin_stock_value_inr,
    fn_inventory_value(NULL) AS total_pharmacy_inventory_value_inr;

-- ============================================================================
-- SECTION 7: STORED PROCEDURES & ACID TRANSACTIONS
-- ============================================================================

-- 7.1 Successful Multi-Item Sale Execution (sp_process_sale)
SELECT '7.1 Testing sp_process_sale: Successful Multi-Item Invoice' AS 'Demo Step';
SET @cust = 1;
SET @emp = 3;
SET @method = 'UPI';
SET @disc = 5.00;
-- Items: 2 units of Batch 1 (Dolo 650) and 1 unit of Batch 2 (Crocin)
SET @items = '[{"batch_id": 1, "quantity": 2}, {"batch_id": 2, "quantity": 1}]';

-- Check inventory before sale
SELECT batch_id, batch_number, quantity AS qty_before FROM batch WHERE batch_id IN (1, 2);

-- Execute procedure
CALL sp_process_sale(@cust, @emp, @method, @disc, @items, @new_sale_id, @inv_num, @tot_amt);

SELECT @new_sale_id AS generated_sale_id, @inv_num AS invoice_number, @tot_amt AS total_bill_amount;

-- Verify inventory decreased by exact sale quantity
SELECT batch_id, batch_number, quantity AS qty_after FROM batch WHERE batch_id IN (1, 2);

-- 7.2 Low Stock Procedure Execution (sp_get_low_stock)
SELECT '7.2 Testing sp_get_low_stock with threshold 10' AS 'Demo Step';
CALL sp_get_low_stock(10);

-- ============================================================================
-- SECTION 8: TRIGGER VERIFICATION (AUTOMATED AUDITING)
-- ============================================================================
SELECT '8.1 Trigger Verification: Inspect latest records in inventory_audit' AS 'Demo Step';
SELECT audit_id, batch_id, change_type, old_quantity, new_quantity, quantity_change, changed_by, changed_at, notes
FROM inventory_audit
ORDER BY audit_id DESC
LIMIT 6;

-- ============================================================================
-- SECTION 9: TRANSACTION ROLLBACK & CONSTRAINT INTEGRITY DEMO
-- ============================================================================

-- 9.1 Transaction Rollback Test
SELECT '9.1 Manual Transaction ROLLBACK Demonstration' AS 'Demo Step';
START TRANSACTION;
-- Temporarily modify a price
UPDATE batch SET selling_price = 999.00 WHERE batch_id = 1;
SELECT batch_id, selling_price AS price_inside_tx FROM batch WHERE batch_id = 1;
-- Rollback
ROLLBACK;
-- Verify rollback restored original price
SELECT batch_id, selling_price AS price_after_rollback FROM batch WHERE batch_id = 1;

-- 9.2 Constraint Violation Demonstration (Expect Failure if executed directly)
-- Note: The statements below demonstrate engine enforcement.
-- CHECK Constraint violation: quantity < 0
-- UPDATE batch SET quantity = -5 WHERE batch_id = 1; -- Throws Check constraint 'chk_batch_qty' violated
-- UNIQUE Constraint violation: duplicate batch number for same medicine
-- INSERT INTO batch (medicine_id, supplier_id, batch_number, expiry_date, purchase_price, selling_price, quantity)
-- VALUES (1, 1, 'DL650A', '2028-01-01', 1.0, 2.0, 50); -- Throws Duplicate entry '1-DL650A' for key 'uq_medicine_batch'
-- FOREIGN KEY violation: invalid supplier_id
-- INSERT INTO batch (medicine_id, supplier_id, batch_number, expiry_date, purchase_price, selling_price, quantity)
-- VALUES (1, 9999, 'NEWBATCH', '2028-01-01', 1.0, 2.0, 50); -- Throws Foreign key constraint fails

SELECT '====================================================================' AS '';
SELECT 'DEMONSTRATION COMPLETED SUCCESSFULLY!' AS 'STATUS';
SELECT '====================================================================' AS '';
