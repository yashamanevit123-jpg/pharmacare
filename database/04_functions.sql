-- ============================================================================
-- PharmaCare Pharmacy Management System
-- Stored Functions Script (04_functions.sql)
-- Target RDBMS: MySQL 8.0+
-- Demonstrates: Stored Functions, DETERMINISTIC/READS SQL DATA, Control Flow
-- ============================================================================

USE pharmacare_db;

DROP FUNCTION IF EXISTS fn_calculate_discount;
DROP FUNCTION IF EXISTS fn_inventory_value;
DROP FUNCTION IF EXISTS fn_stock_status;

DELIMITER //

-- ============================================================================
-- 1. FUNCTION: fn_stock_status
-- Purpose: Evaluates clinical inventory viability for a specific batch.
-- Parameters:
--   p_batch_id INT - The primary key ID of the batch
-- Returns:
--   VARCHAR(25) - 'EXPIRED', 'OUT_OF_STOCK', 'EXPIRING_SOON', 'LOW_STOCK', 'OPTIMAL', 'NOT_FOUND'
-- ============================================================================
CREATE FUNCTION fn_stock_status(p_batch_id INT)
RETURNS VARCHAR(25)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_qty INT;
    DECLARE v_exp DATE;
    DECLARE v_status VARCHAR(25);

    -- Retrieve batch attributes
    SELECT quantity, expiry_date 
    INTO v_qty, v_exp
    FROM batch
    WHERE batch_id = p_batch_id;

    IF v_qty IS NULL THEN
        SET v_status = 'NOT_FOUND';
    ELSEIF v_exp < CURRENT_DATE THEN
        SET v_status = 'EXPIRED';
    ELSEIF v_qty = 0 THEN
        SET v_status = 'OUT_OF_STOCK';
    ELSEIF DATEDIFF(v_exp, CURRENT_DATE) <= 90 THEN
        SET v_status = 'EXPIRING_SOON';
    ELSEIF v_qty < 10 THEN
        SET v_status = 'LOW_STOCK';
    ELSE
        SET v_status = 'OPTIMAL';
    END IF;

    RETURN v_status;
END //

-- ============================================================================
-- 2. FUNCTION: fn_inventory_value
-- Purpose: Computes monetary valuation of active stock based on purchase cost.
-- Parameters:
--   p_medicine_id INT - If > 0, calculates valuation for that specific medicine.
--                       If NULL or <= 0, calculates grand total pharmacy stock value.
-- Returns:
--   DECIMAL(14,2) - Cumulative purchase valuation in INR.
-- ============================================================================
CREATE FUNCTION fn_inventory_value(p_medicine_id INT)
RETURNS DECIMAL(14,2)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_total_val DECIMAL(14,2);

    IF p_medicine_id IS NOT NULL AND p_medicine_id > 0 THEN
        SELECT COALESCE(SUM(quantity * purchase_price), 0.00)
        INTO v_total_val
        FROM batch
        WHERE medicine_id = p_medicine_id
          AND expiry_date >= CURRENT_DATE
          AND status = 'Available';
    ELSE
        SELECT COALESCE(SUM(quantity * purchase_price), 0.00)
        INTO v_total_val
        FROM batch
        WHERE expiry_date >= CURRENT_DATE
          AND status = 'Available';
    END IF;

    RETURN v_total_val;
END //

-- ============================================================================
-- 3. FUNCTION: fn_calculate_discount
-- Purpose: Evaluates customer loyalty or senior citizen discount entitlement.
-- Parameters:
--   p_total_amount DECIMAL(10,2) - Gross invoice total
--   p_customer_id INT - Customer ID
-- Returns:
--   DECIMAL(10,2) - Eligible discount in INR
-- ============================================================================
CREATE FUNCTION fn_calculate_discount(p_total_amount DECIMAL(10,2), p_customer_id INT)
RETURNS DECIMAL(10,2)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_age INT;
    DECLARE v_discount DECIMAL(10,2) DEFAULT 0.00;

    SELECT age INTO v_age FROM customer WHERE customer_id = p_customer_id;

    -- Senior citizen discount: 5% for customers 60 or older
    IF v_age IS NOT NULL AND v_age >= 60 THEN
        SET v_discount = ROUND(p_total_amount * 0.05, 2);
    -- High value cart discount: 3% for bills >= ₹1000
    ELSEIF p_total_amount >= 1000.00 THEN
        SET v_discount = ROUND(p_total_amount * 0.03, 2);
    END IF;

    RETURN v_discount;
END //

DELIMITER ;
