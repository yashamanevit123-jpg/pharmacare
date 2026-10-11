-- ============================================================================
-- PharmaCare Automated Test Suite (test_runner.sql)
-- Executes explicit negative tests, edge cases, transactional rollback tests,
-- and constraint failure tests.
-- ============================================================================

USE pharmacare_db;

-- ----------------------------------------------------------------------------
-- TEST 1: INSUFFICIENT STOCK ROLLBACK IN sp_process_sale
-- Attempt to purchase 9999 units of Batch 1 (which only has ~148 units).
-- Expected Result: Procedure raises SQLSTATE '45000', transaction rolls back,
--                  and Batch 1 stock remains completely unchanged.
-- ----------------------------------------------------------------------------
SELECT '--- TEST 1: Insufficient Stock Rollback ---' AS Test;
SELECT quantity INTO @qty_before_test1 FROM batch WHERE batch_id = 1;

DROP PROCEDURE IF EXISTS test_case_insufficient_stock;
DELIMITER //
CREATE PROCEDURE test_case_insufficient_stock(OUT p_result VARCHAR(100))
BEGIN
    DECLARE v_error_occurred BOOLEAN DEFAULT FALSE;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
        SET v_error_occurred = TRUE;
    END;

    CALL sp_process_sale(1, 3, 'Cash', 0.00, '[{"batch_id": 1, "quantity": 9999}]', @dummy_id, @dummy_inv, @dummy_tot);

    IF v_error_occurred THEN
        SET p_result = 'PASSED: Insufficient stock rejected and transaction rolled back';
    ELSE
        SET p_result = 'FAILED: Insufficient stock was not rejected!';
    END IF;
END //
DELIMITER ;

CALL test_case_insufficient_stock(@test1_result);
SELECT @test1_result AS 'Test 1 Result';

SELECT quantity INTO @qty_after_test1 FROM batch WHERE batch_id = 1;
SELECT 
    @qty_before_test1 AS stock_before, 
    @qty_after_test1 AS stock_after, 
    IF(@qty_before_test1 = @qty_after_test1, 'PASSED: Zero Stock Leakage', 'FAILED: Stock corrupted') AS rollback_verification;

DROP PROCEDURE IF EXISTS test_case_insufficient_stock;

-- ----------------------------------------------------------------------------
-- TEST 2: EXPIRED BATCH REJECTION IN sp_process_sale
-- Attempt to sell Batch 28 (DL650-OLD which expired in 2024).
-- Expected Result: Procedure rejects expired batch, raises error, rolls back.
-- ----------------------------------------------------------------------------
SELECT '--- TEST 2: Expired Batch Rejection ---' AS Test;
SELECT quantity INTO @qty_before_test2 FROM batch WHERE batch_id = 28;

DROP PROCEDURE IF EXISTS test_case_expired_batch;
DELIMITER //
CREATE PROCEDURE test_case_expired_batch(OUT p_result VARCHAR(100))
BEGIN
    DECLARE v_error_occurred BOOLEAN DEFAULT FALSE;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
        SET v_error_occurred = TRUE;
    END;

    CALL sp_process_sale(1, 3, 'Cash', 0.00, '[{"batch_id": 28, "quantity": 1}]', @dummy_id, @dummy_inv, @dummy_tot);

    IF v_error_occurred THEN
        SET p_result = 'PASSED: Expired batch sale rejected';
    ELSE
        SET p_result = 'FAILED: Expired batch was erroneously permitted!';
    END IF;
END //
DELIMITER ;

CALL test_case_expired_batch(@test2_result);
SELECT @test2_result AS 'Test 2 Result';

SELECT quantity INTO @qty_after_test2 FROM batch WHERE batch_id = 28;
SELECT 
    @qty_before_test2 AS stock_before, 
    @qty_after_test2 AS stock_after, 
    IF(@qty_before_test2 = @qty_after_test2, 'PASSED: Expired lot inventory untouched', 'FAILED') AS rollback_verification;

DROP PROCEDURE IF EXISTS test_case_expired_batch;

-- ----------------------------------------------------------------------------
-- TEST 3: ATOMIC PURCHASE RECORDING VIA sp_record_purchase
-- Record new shipment of 50 units for Dolo 650 (batch DL650A).
-- Expected Result: Stock increases by 50, audit trigger records PURCHASE.
-- ----------------------------------------------------------------------------
SELECT '--- TEST 3: Atomic Purchase & Inventory Restocking ---' AS Test;
SELECT quantity INTO @qty_before_test3 FROM batch WHERE batch_id = 1;

SET @test_pur_inv = CONCAT('PUR-TEST-', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '-', FLOOR(RAND() * 1000));
SET @purch_items = '[{"medicine_id": 1, "batch_number": "DL650A", "expiry_date": "2027-08-15", "quantity": 50, "unit_cost": 1.20, "selling_price": 2.00}]';
CALL sp_record_purchase(1, 4, @test_pur_inv, CURRENT_DATE, 'Automated Test Batch Intake', @purch_items, @new_pur_id, @new_pur_total);

SELECT quantity INTO @qty_after_test3 FROM batch WHERE batch_id = 1;
SELECT 
    @new_pur_id AS generated_purchase_id,
    @new_pur_total AS total_amount,
    @qty_before_test3 AS stock_before,
    @qty_after_test3 AS stock_after,
    IF(@qty_after_test3 = @qty_before_test3 + 50, 'PASSED: Quantity accurately incremented', 'FAILED') AS purchase_verification;

-- ----------------------------------------------------------------------------
-- TEST 4: CUSTOMER RETURN & RESTOCK VIA sp_process_return
-- Customer returns 1 unit of medicine from an existing completed sale.
-- Expected Result: Refund recorded, batch quantity incremented by 1.
-- ----------------------------------------------------------------------------
SELECT '--- TEST 4: Customer Return & Restocking ---' AS Test;
SELECT quantity INTO @qty_before_test4 FROM batch WHERE batch_id = 1;

-- Find any valid sale and sale_item referencing Batch 1
SELECT s.sale_id, si.sale_item_id 
INTO @target_sale_id, @target_sitem_id 
FROM sale s 
JOIN sale_item si ON s.sale_id = si.sale_id 
WHERE si.batch_id = 1 
LIMIT 1;

SET @ret_items = CONCAT('[{"sale_item_id": ', @target_sitem_id, ', "quantity": 1, "restock": true}]');

CALL sp_process_return(@target_sale_id, 2, 'Unopened excess strips', @ret_items, @ret_id, @ref_amt);

SELECT quantity INTO @qty_after_test4 FROM batch WHERE batch_id = 1;
SELECT 
    @ret_id AS generated_return_id,
    @ref_amt AS refund_amount,
    @qty_before_test4 AS stock_before,
    @qty_after_test4 AS stock_after,
    IF(@qty_after_test4 = @qty_before_test4 + 1, 'PASSED: Batch restocked accurately', 'FAILED') AS return_verification;

-- ----------------------------------------------------------------------------
-- TEST 5: CHECK CONSTRAINT ENFORCEMENT (chk_batch_qty: quantity >= 0)
-- ----------------------------------------------------------------------------
SELECT '--- TEST 5: CHECK Constraint Enforcement ---' AS Test;
DROP PROCEDURE IF EXISTS test_case_negative_stock;
DELIMITER //
CREATE PROCEDURE test_case_negative_stock(OUT p_result VARCHAR(100))
BEGIN
    DECLARE v_error_caught BOOLEAN DEFAULT FALSE;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
        SET v_error_caught = TRUE;
    END;

    UPDATE batch SET quantity = -10 WHERE batch_id = 1;

    IF v_error_caught THEN
        SET p_result = 'PASSED: Negative stock rejected by CHECK (quantity >= 0)';
    ELSE
        SET p_result = 'FAILED: Negative stock was allowed!';
    END IF;
END //
DELIMITER ;

CALL test_case_negative_stock(@test5_result);
SELECT @test5_result AS 'Test 5 Result';
DROP PROCEDURE IF EXISTS test_case_negative_stock;

-- ----------------------------------------------------------------------------
-- TEST 6: UNIQUE CONSTRAINT ENFORCEMENT (uq_medicine_batch)
-- ----------------------------------------------------------------------------
SELECT '--- TEST 6: UNIQUE Constraint Enforcement ---' AS Test;
DROP PROCEDURE IF EXISTS test_case_duplicate_batch;
DELIMITER //
CREATE PROCEDURE test_case_duplicate_batch(OUT p_result VARCHAR(100))
BEGIN
    DECLARE v_error_caught BOOLEAN DEFAULT FALSE;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
        SET v_error_caught = TRUE;
    END;

    INSERT INTO batch (medicine_id, supplier_id, batch_number, expiry_date, purchase_price, selling_price, quantity)
    VALUES (1, 1, 'DL650A', '2028-01-01', 1.00, 2.00, 50);

    IF v_error_caught THEN
        SET p_result = 'PASSED: Duplicate batch_number rejected by UNIQUE (medicine_id, batch_number)';
    ELSE
        SET p_result = 'FAILED: Duplicate batch_number was allowed!';
    END IF;
END //
DELIMITER ;

CALL test_case_duplicate_batch(@test6_result);
SELECT @test6_result AS 'Test 6 Result';
DROP PROCEDURE IF EXISTS test_case_duplicate_batch;

-- ----------------------------------------------------------------------------
-- TEST 7: FOREIGN KEY CONSTRAINT ENFORCEMENT (fk_medicine_category)
-- ----------------------------------------------------------------------------
SELECT '--- TEST 7: Foreign Key Constraint Enforcement ---' AS Test;
DROP PROCEDURE IF EXISTS test_case_invalid_fk;
DELIMITER //
CREATE PROCEDURE test_case_invalid_fk(OUT p_result VARCHAR(100))
BEGIN
    DECLARE v_error_caught BOOLEAN DEFAULT FALSE;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
        SET v_error_caught = TRUE;
    END;

    INSERT INTO medicine (category_id, manufacturer_id, name, dosage_form)
    VALUES (99999, 1, 'Invalid Medicine Test', 'Tablet');

    IF v_error_caught THEN
        SET p_result = 'PASSED: Invalid foreign key rejected by fk_medicine_category';
    ELSE
        SET p_result = 'FAILED: Invalid foreign key was accepted!';
    END IF;
END //
DELIMITER ;

CALL test_case_invalid_fk(@test7_result);
SELECT @test7_result AS 'Test 7 Result';
DROP PROCEDURE IF EXISTS test_case_invalid_fk;

SELECT '====================================================================' AS '';
SELECT 'ALL 7 INTEGRITY AND TRANSACTION TESTS COMPLETED!' AS 'SUMMARY';
SELECT '====================================================================' AS '';
