-- ============================================================================
-- PharmaCare Pharmacy Management System
-- Triggers Definition Script (05_triggers.sql)
-- Target RDBMS: MySQL 8.0+
-- Demonstrates: AFTER UPDATE, AFTER INSERT, BEFORE INSERT Triggers,
--               Automated Auditing, Business Rule Enforcement with SIGNAL
-- ============================================================================

USE pharmacare_db;

DROP TRIGGER IF EXISTS trg_prevent_expired_batch_sale;
DROP TRIGGER IF EXISTS trg_after_batch_stock_update;
DROP TRIGGER IF EXISTS trg_after_batch_insert;

DELIMITER //

-- ============================================================================
-- 1. TRIGGER: trg_after_batch_stock_update
-- Purpose: Automatically audits every modification to batch quantity into
--          the inventory_audit table with old, new, and delta quantities.
-- ============================================================================
CREATE TRIGGER trg_after_batch_stock_update
AFTER UPDATE ON batch
FOR EACH ROW
BEGIN
    DECLARE v_diff INT;
    DECLARE v_type VARCHAR(20);
    DECLARE v_user VARCHAR(100);

    -- Only audit when the inventory count has genuinely changed
    IF OLD.quantity <> NEW.quantity THEN
        SET v_diff = NEW.quantity - OLD.quantity;

        -- Capture application user session variable if defined, else system user
        SET v_user = COALESCE(@app_user, SUBSTRING_INDEX(USER(), '@', 1));

        -- Infer transaction nature if not explicitly flagged in session variable
        IF @audit_change_type IS NOT NULL THEN
            SET v_type = @audit_change_type;
        ELSEIF v_diff < 0 THEN
            SET v_type = 'SALE';
        ELSE
            SET v_type = 'PURCHASE';
        END IF;

        INSERT INTO inventory_audit (
            batch_id,
            medicine_id,
            change_type,
            old_quantity,
            new_quantity,
            quantity_change,
            changed_by,
            changed_at,
            notes
        ) VALUES (
            NEW.batch_id,
            NEW.medicine_id,
            v_type,
            OLD.quantity,
            NEW.quantity,
            v_diff,
            v_user,
            NOW(),
            CONCAT('Batch ', NEW.batch_number, ' adjusted by ', v_diff, ' units')
        );
    END IF;
END //

-- ============================================================================
-- 2. TRIGGER: trg_after_batch_insert
-- Purpose: Logs opening inventory balance when a new batch is created.
-- ============================================================================
CREATE TRIGGER trg_after_batch_insert
AFTER INSERT ON batch
FOR EACH ROW
BEGIN
    DECLARE v_user VARCHAR(100);
    SET v_user = COALESCE(@app_user, SUBSTRING_INDEX(USER(), '@', 1));

    IF NEW.quantity > 0 THEN
        INSERT INTO inventory_audit (
            batch_id,
            medicine_id,
            change_type,
            old_quantity,
            new_quantity,
            quantity_change,
            changed_by,
            changed_at,
            notes
        ) VALUES (
            NEW.batch_id,
            NEW.medicine_id,
            'INITIAL_STOCK',
            0,
            NEW.quantity,
            NEW.quantity,
            v_user,
            NOW(),
            CONCAT('New batch lot ', NEW.batch_number, ' received into inventory')
        );
    END IF;
END //

-- ============================================================================
-- 3. TRIGGER: trg_prevent_expired_batch_sale
-- Purpose: Intercepts sale item creation at the engine level. If an attempt
--          is made to sell an expired batch, aborts with SQLSTATE '45000'.
-- ============================================================================
CREATE TRIGGER trg_prevent_expired_batch_sale
BEFORE INSERT ON sale_item
FOR EACH ROW
BEGIN
    DECLARE v_exp DATE;
    DECLARE v_batch_num VARCHAR(50);

    SELECT expiry_date, batch_number 
    INTO v_exp, v_batch_num
    FROM batch 
    WHERE batch_id = NEW.batch_id;

    IF v_exp IS NOT NULL AND v_exp < CURRENT_DATE THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'CLINICAL SAFETY ERROR: Cannot dispense or sell an expired medicine batch!';
    END IF;
END //

DELIMITER ;
