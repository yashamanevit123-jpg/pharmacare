-- ============================================================================
-- PharmaCare Pharmacy Management System
-- Stored Procedures Script (06_procedures.sql)
-- Target RDBMS: MySQL 8.0+
-- Demonstrates: Stored Procedures, ACID Transactions (START TRANSACTION,
--               COMMIT, ROLLBACK), Row-Level Locking (SELECT ... FOR UPDATE),
--               JSON Parameters, Error Handlers (DECLARE EXIT HANDLER)
-- ============================================================================

USE pharmacare_db;

DROP PROCEDURE IF EXISTS sp_process_return;
DROP PROCEDURE IF EXISTS sp_get_low_stock;
DROP PROCEDURE IF EXISTS sp_record_purchase;
DROP PROCEDURE IF EXISTS sp_process_sale;

DELIMITER //

-- ============================================================================
-- 1. PROCEDURE: sp_process_sale
-- Purpose: Atomically creates an invoice, validates batch viability and stock,
--          applies row-level locks to prevent race conditions, deducts stock,
--          and commits all changes. If any item fails, rolls back completely.
-- Parameters:
--   IN  p_customer_id     INT
--   IN  p_employee_id     INT
--   IN  p_payment_method  VARCHAR(20)
--   IN  p_discount_amount DECIMAL(10,2)
--   IN  p_items_json      JSON  -- Format: [{"batch_id": 1, "quantity": 2}, ...]
--   OUT p_sale_id         INT
--   OUT p_invoice_number  VARCHAR(60)
--   OUT p_total_amount    DECIMAL(12,2)
-- ============================================================================
CREATE PROCEDURE sp_process_sale(
    IN  p_customer_id     INT,
    IN  p_employee_id     INT,
    IN  p_payment_method  VARCHAR(20),
    IN  p_discount_amount DECIMAL(10,2),
    IN  p_items_json      JSON,
    OUT p_sale_id         INT,
    OUT p_invoice_number  VARCHAR(60),
    OUT p_total_amount    DECIMAL(12,2)
)
proc_label: BEGIN
    DECLARE v_item_count INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    DECLARE v_batch_id INT;
    DECLARE v_qty INT;
    DECLARE v_curr_qty INT;
    DECLARE v_med_id INT;
    DECLARE v_sell_price DECIMAL(10,2);
    DECLARE v_exp DATE;
    DECLARE v_subtotal DECIMAL(12,2) DEFAULT 0.00;
    DECLARE v_gross DECIMAL(12,2) DEFAULT 0.00;
    DECLARE v_net DECIMAL(12,2) DEFAULT 0.00;
    DECLARE v_item_subtotal DECIMAL(12,2);
    DECLARE v_batch_num VARCHAR(50);
    DECLARE v_discount DECIMAL(10,2);

    -- Transaction rollback handler on any SQL exception
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    -- Validate input parameters
    IF p_customer_id IS NULL OR p_employee_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Validation Error: Customer and Employee must be specified.';
    END IF;

    SET v_item_count = JSON_LENGTH(p_items_json);
    IF v_item_count IS NULL OR v_item_count <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Validation Error: Invoice must contain at least one medicine item.';
    END IF;

    SET v_discount = COALESCE(p_discount_amount, 0.00);

    -- Begin atomic transaction
    START TRANSACTION;

    -- Generate unique human-readable invoice code
    SET p_invoice_number = CONCAT('INV-', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '-', LPAD(FLOOR(RAND() * 1000), 3, '0'));

    -- PASS 1: Validate stock, expiry, and compute gross total with exclusive row locks
    WHILE i < v_item_count DO
        SET v_batch_id = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].batch_id')));
        SET v_qty = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].quantity')));

        IF v_qty <= 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Validation Error: Quantity must be greater than zero.';
        END IF;

        -- Acquire row-level exclusive lock on batch record
        SELECT quantity, selling_price, expiry_date, medicine_id, batch_number
        INTO v_curr_qty, v_sell_price, v_exp, v_med_id, v_batch_num
        FROM batch
        WHERE batch_id = v_batch_id
        FOR UPDATE;

        IF v_curr_qty IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Transaction Error: Designated medicine batch does not exist.';
        END IF;

        -- Clinical safety check: reject expired batches
        IF v_exp < CURRENT_DATE THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Transaction Rejected: Batch has expired and cannot be sold.';
        END IF;

        -- Stock availability check
        IF v_curr_qty < v_qty THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Transaction Rejected: Insufficient inventory in stock.';
        END IF;

        SET v_item_subtotal = ROUND(v_qty * v_sell_price, 2);
        SET v_gross = v_gross + v_item_subtotal;
        SET i = i + 1;
    END WHILE;

    -- Calculate net invoice total
    IF v_discount > v_gross THEN
        SET v_discount = v_gross;
    END IF;
    SET v_net = v_gross - v_discount;

    -- Set application context for audit triggers
    SET @audit_change_type = 'SALE';

    -- Insert sale invoice header
    INSERT INTO sale (
        invoice_number,
        customer_id,
        employee_id,
        sale_date,
        payment_method,
        subtotal,
        tax_amount,
        discount_amount,
        total_amount,
        status
    ) VALUES (
        p_invoice_number,
        p_customer_id,
        p_employee_id,
        NOW(),
        COALESCE(p_payment_method, 'Cash'),
        v_gross,
        0.00,
        v_discount,
        v_net,
        'Completed'
    );

    SET p_sale_id = LAST_INSERT_ID();
    SET p_total_amount = v_net;

    -- PASS 2: Deduct inventory and insert line items
    SET i = 0;
    WHILE i < v_item_count DO
        SET v_batch_id = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].batch_id')));
        SET v_qty = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].quantity')));

        SELECT selling_price, medicine_id
        INTO v_sell_price, v_med_id
        FROM batch
        WHERE batch_id = v_batch_id;

        SET v_item_subtotal = ROUND(v_qty * v_sell_price, 2);

        -- Insert sale line item
        INSERT INTO sale_item (
            sale_id,
            batch_id,
            medicine_id,
            quantity,
            unit_price,
            subtotal
        ) VALUES (
            p_sale_id,
            v_batch_id,
            v_med_id,
            v_qty,
            v_sell_price,
            v_item_subtotal
        );

        -- Decrement batch quantity (fires trg_after_batch_stock_update)
        UPDATE batch
        SET quantity = quantity - v_qty
        WHERE batch_id = v_batch_id;

        SET i = i + 1;
    END WHILE;

    -- Commit transaction
    COMMIT;
    SET @audit_change_type = NULL;
END //

-- ============================================================================
-- 2. PROCEDURE: sp_record_purchase
-- Purpose: Atomically records a wholesale purchase order from a supplier and
--          upserts the batch quantities into available inventory.
-- Parameters:
--   IN  p_supplier_id    INT
--   IN  p_employee_id    INT
--   IN  p_invoice_number VARCHAR(60)
--   IN  p_purchase_date  DATE
--   IN  p_notes          TEXT
--   IN  p_items_json     JSON -- [{"medicine_id": 1, "batch_number": "B1", "expiry_date": "2027-01-01", "quantity": 100, "unit_cost": 1.20, "selling_price": 2.00}]
--   OUT p_purchase_id    INT
--   OUT p_total_amount   DECIMAL(12,2)
-- ============================================================================
CREATE PROCEDURE sp_record_purchase(
    IN  p_supplier_id    INT,
    IN  p_employee_id    INT,
    IN  p_invoice_number VARCHAR(60),
    IN  p_purchase_date  DATE,
    IN  p_notes          TEXT,
    IN  p_items_json     JSON,
    OUT p_purchase_id    INT,
    OUT p_total_amount   DECIMAL(12,2)
)
BEGIN
    DECLARE v_item_count INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    DECLARE v_med_id INT;
    DECLARE v_batch_num VARCHAR(50);
    DECLARE v_exp DATE;
    DECLARE v_qty INT;
    DECLARE v_cost DECIMAL(10,2);
    DECLARE v_sell DECIMAL(10,2);
    DECLARE v_line_cost DECIMAL(12,2);
    DECLARE v_grand_total DECIMAL(12,2) DEFAULT 0.00;
    DECLARE v_existing_batch_id INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SET v_item_count = JSON_LENGTH(p_items_json);
    IF v_item_count IS NULL OR v_item_count <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Validation Error: Purchase order must contain at least one item.';
    END IF;

    START TRANSACTION;

    -- Calculate total cost
    WHILE i < v_item_count DO
        SET v_qty = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].quantity')));
        SET v_cost = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].unit_cost')));
        SET v_grand_total = v_grand_total + ROUND(v_qty * v_cost, 2);
        SET i = i + 1;
    END WHILE;

    -- Insert purchase order header
    INSERT INTO purchase (
        supplier_id,
        employee_id,
        invoice_number,
        purchase_date,
        total_amount,
        payment_status,
        notes
    ) VALUES (
        p_supplier_id,
        p_employee_id,
        p_invoice_number,
        COALESCE(p_purchase_date, CURRENT_DATE),
        v_grand_total,
        'Paid',
        p_notes
    );

    SET p_purchase_id = LAST_INSERT_ID();
    SET p_total_amount = v_grand_total;

    -- Set audit session flag
    SET @audit_change_type = 'PURCHASE';

    -- Process each item and adjust batch inventory
    SET i = 0;
    WHILE i < v_item_count DO
        SET v_med_id = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].medicine_id')));
        SET v_batch_num = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].batch_number')));
        SET v_exp = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].expiry_date')));
        SET v_qty = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].quantity')));
        SET v_cost = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].unit_cost')));
        SET v_sell = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].selling_price')));
        SET v_line_cost = ROUND(v_qty * v_cost, 2);

        -- Insert purchase item
        INSERT INTO purchase_item (
            purchase_id,
            medicine_id,
            batch_number,
            expiry_date,
            quantity,
            unit_cost,
            total_cost
        ) VALUES (
            p_purchase_id,
            v_med_id,
            v_batch_num,
            v_exp,
            v_qty,
            v_cost,
            v_line_cost
        );

        -- Upsert batch: check if this batch already exists for the medicine
        SELECT batch_id INTO v_existing_batch_id
        FROM batch
        WHERE medicine_id = v_med_id AND batch_number = v_batch_num;

        IF v_existing_batch_id IS NOT NULL THEN
            UPDATE batch
            SET quantity = quantity + v_qty,
                purchase_price = v_cost,
                selling_price = COALESCE(v_sell, selling_price)
            WHERE batch_id = v_existing_batch_id;
        ELSE
            INSERT INTO batch (
                medicine_id,
                supplier_id,
                batch_number,
                manufacturing_date,
                expiry_date,
                purchase_price,
                selling_price,
                quantity,
                status
            ) VALUES (
                v_med_id,
                p_supplier_id,
                v_batch_num,
                CURRENT_DATE,
                v_exp,
                v_cost,
                COALESCE(v_sell, ROUND(v_cost * 1.35, 2)),
                v_qty,
                'Available'
            );
        END IF;

        SET i = i + 1;
    END WHILE;

    COMMIT;
    SET @audit_change_type = NULL;
END //

-- ============================================================================
-- 3. PROCEDURE: sp_get_low_stock
-- Purpose: Returns all inventory items whose stock is at or below threshold.
-- Parameters:
--   IN p_threshold INT - Custom limit. If NULL, uses medicine.reorder_level.
-- ============================================================================
CREATE PROCEDURE sp_get_low_stock(IN p_threshold INT)
BEGIN
    SELECT 
        m.medicine_id,
        m.name AS medicine_name,
        c.name AS category_name,
        man.name AS manufacturer_name,
        b.batch_id,
        b.batch_number,
        b.quantity,
        m.reorder_level,
        m.rack_location,
        b.expiry_date
    FROM batch b
    JOIN medicine m ON b.medicine_id = m.medicine_id
    JOIN category c ON m.category_id = c.category_id
    JOIN manufacturer man ON m.manufacturer_id = man.manufacturer_id
    WHERE b.quantity <= COALESCE(p_threshold, m.reorder_level)
      AND b.status = 'Available'
      AND b.expiry_date >= CURRENT_DATE
    ORDER BY b.quantity ASC;
END //

-- ============================================================================
-- 4. PROCEDURE: sp_process_return
-- Purpose: Atomically authorizes a return against a sale invoice and restocks
--          undamaged goods into inventory.
-- Parameters:
--   IN  p_sale_id         INT
--   IN  p_employee_id     INT
--   IN  p_reason          VARCHAR(255)
--   IN  p_items_json      JSON -- [{"sale_item_id": 1, "quantity": 1, "restock": true}]
--   OUT p_return_id       INT
--   OUT p_refund_amount   DECIMAL(12,2)
-- ============================================================================
CREATE PROCEDURE sp_process_return(
    IN  p_sale_id         INT,
    IN  p_employee_id     INT,
    IN  p_reason          VARCHAR(255),
    IN  p_items_json      JSON,
    OUT p_return_id       INT,
    OUT p_refund_amount   DECIMAL(12,2)
)
BEGIN
    DECLARE v_item_count INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    DECLARE v_cust_id INT;
    DECLARE v_sitem_id INT;
    DECLARE v_ret_qty INT;
    DECLARE v_restock BOOLEAN;
    DECLARE v_batch_id INT;
    DECLARE v_unit_price DECIMAL(10,2);
    DECLARE v_orig_qty INT;
    DECLARE v_line_refund DECIMAL(12,2);
    DECLARE v_tot_refund DECIMAL(12,2) DEFAULT 0.00;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    SELECT customer_id INTO v_cust_id FROM sale WHERE sale_id = p_sale_id;
    IF v_cust_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Sale invoice not found.';
    END IF;

    SET v_item_count = JSON_LENGTH(p_items_json);
    IF v_item_count IS NULL OR v_item_count <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No items specified for return.';
    END IF;

    START TRANSACTION;

    -- Calculate total refund
    WHILE i < v_item_count DO
        SET v_sitem_id = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].sale_item_id')));
        SET v_ret_qty = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].quantity')));

        SELECT unit_price, quantity INTO v_unit_price, v_orig_qty
        FROM sale_item WHERE sale_item_id = v_sitem_id AND sale_id = p_sale_id;

        IF v_unit_price IS NULL OR v_ret_qty > v_orig_qty THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid return quantity or item.';
        END IF;

        SET v_line_refund = ROUND(v_ret_qty * v_unit_price, 2);
        SET v_tot_refund = v_tot_refund + v_line_refund;
        SET i = i + 1;
    END WHILE;

    INSERT INTO sales_return (
        sale_id,
        customer_id,
        employee_id,
        return_date,
        refund_amount,
        reason,
        status
    ) VALUES (
        p_sale_id,
        v_cust_id,
        p_employee_id,
        NOW(),
        v_tot_refund,
        COALESCE(p_reason, 'Customer Return'),
        'Approved'
    );

    SET p_return_id = LAST_INSERT_ID();
    SET p_refund_amount = v_tot_refund;

    SET @audit_change_type = 'RETURN';

    -- Process items and restock
    SET i = 0;
    WHILE i < v_item_count DO
        SET v_sitem_id = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].sale_item_id')));
        SET v_ret_qty = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].quantity')));
        
        -- Safely parse boolean / integer restock flag
        IF LOWER(COALESCE(JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', i, '].restock'))), 'true')) IN ('false', '0') THEN
            SET v_restock = FALSE;
        ELSE
            SET v_restock = TRUE;
        END IF;

        SELECT batch_id, unit_price INTO v_batch_id, v_unit_price
        FROM sale_item WHERE sale_item_id = v_sitem_id;

        SET v_line_refund = ROUND(v_ret_qty * v_unit_price, 2);

        INSERT INTO return_item (
            return_id,
            sale_item_id,
            batch_id,
            quantity,
            refund_unit_price,
            subtotal,
            restocked
        ) VALUES (
            p_return_id,
            v_sitem_id,
            v_batch_id,
            v_ret_qty,
            v_unit_price,
            v_line_refund,
            v_restock
        );

        IF v_restock = TRUE THEN
            UPDATE batch
            SET quantity = quantity + v_ret_qty
            WHERE batch_id = v_batch_id;
        END IF;

        SET i = i + 1;
    END WHILE;

    -- Update parent sale status
    UPDATE sale SET status = 'Partially_Refunded' WHERE sale_id = p_sale_id;

    COMMIT;
    SET @audit_change_type = NULL;
END //

DELIMITER ;
