# PharmaCare — Faculty Demonstration & Review Guide

This guide is designed for the final faculty evaluation, laboratory assessment, and viva examination. It links every DBMS theoretical concept directly to live, executable demonstrations across the database, Express API, web frontend, and native Android application.

---

## 🎯 Demonstration Agenda (15-Minute Walkthrough)

```text
[1. Architecture & Schema] ──> [2. Constraints & Keys] ──> [3. ACID Transactions]
                                                                      │
[6. Dual-Client Sync]     <── [5. Index Optimization]   <── [4. Triggers & Audit]
```

---

## 🔬 Demonstration 1: Normalized Relational Schema & 3NF
**DBMS Concept**: 3rd Normal Form (3NF), Entity-Relationship Cardinalities, Least-Privilege Provisioning.

### Action:
Open MySQL Workbench or PowerShell:
```powershell
mysql -u pharmacare_user -pPharmaCare@2026! pharmacare_db
```

### Run Query:
```sql
-- 1. Verify schema tables
SHOW FULL TABLES WHERE Table_type = 'BASE TABLE';

-- 2. Verify foreign key constraints and normalization separation
SELECT 
    m.name AS medicine_name,
    c.name AS category,
    mf.name AS manufacturer,
    m.dosage_form,
    m.rack_location
FROM medicine m
JOIN category c ON m.category_id = c.category_id
JOIN manufacturer mf ON m.manufacturer_id = mf.manufacturer_id
LIMIT 5;
```

### Explaining to Faculty:
- Point out that `medicine` has no transitive dependencies: category names and descriptions reside in `category`, manufacturer details reside in `manufacturer`.
- Point out that medicines are separated from physical batches (`batch` table), ensuring batches with different manufacturing dates, expiries, and costs do not duplicate base medicine metadata.

---

## 🔬 Demonstration 2: ACID Transactions & Atomic Rollback
**DBMS Concept**: Atomicity, Consistency, Isolation, Row-Level Locking (`SELECT ... FOR UPDATE`), Stored Procedures (`sp_process_sale`).

### Action:
Demonstrate both a **successful sale** and a **deliberately failed sale** that triggers an atomic rollback.

### 1. Test Successful Sale:
```sql
-- Check current stock of Batch 1 (Paracetamol)
SELECT batch_id, batch_number, quantity FROM batch WHERE batch_id = 1;

-- Call sp_process_sale for 2 units
CALL sp_process_sale(
    1,                      -- customer_id (Ramesh Patel)
    1,                      -- employee_id (Admin)
    'Cash',                 -- payment_method
    0.00,                   -- discount_amount
    '[{"batchId": 1, "quantity": 2}]',
    @sale_id,
    @inv_num,
    @total
);

-- View output parameters
SELECT @sale_id AS new_sale_id, @inv_num AS invoice_number, @total AS total_amount;

-- Verify stock was decremented by 2
SELECT batch_id, batch_number, quantity FROM batch WHERE batch_id = 1;
```

### 2. Test Failed Sale (Rollback Verification):
```sql
-- Deliberately attempt to buy 99,999 units (exceeding stock)
CALL sp_process_sale(
    1, 1, 'Cash', 0.00,
    '[{"batchId": 1, "quantity": 99999}]',
    @sale_id, @inv_num, @total
);
-- Result: SQL Error 45000: "Insufficient stock for batch 1. Requested: 99999, Available: ..."

-- Verify that no orphan invoice was created and stock did not change
SELECT COUNT(*) FROM sale WHERE total_amount = 0 OR customer_id IS NULL;
SELECT batch_id, quantity FROM batch WHERE batch_id = 1;
```

---

## 🔬 Demonstration 3: MySQL Triggers & Audit Logging
**DBMS Concept**: Active Database Rules, Event-Condition-Action (ECA), Row-Level Triggers (`AFTER UPDATE`), Tamper-Evident Audit Trails.

### Action:
Inspect the `inventory_audit` table to prove that the trigger `trg_after_batch_stock_update` fired automatically:

### Run Query:
```sql
SELECT 
    audit_id,
    batch_id,
    old_quantity,
    new_quantity,
    quantity_delta,
    change_type,
    user_action,
    created_at
FROM inventory_audit
ORDER BY audit_id DESC
LIMIT 5;
```

### Explaining to Faculty:
- Point out that neither the Node.js backend nor the Android client inserted this row directly.
- The MySQL trigger intercepted the stock modification and recorded the exact delta, old quantity, new quantity, and timestamp at the database engine level.

---

## 🔬 Demonstration 4: Database Views & Stored Functions
**DBMS Concept**: Views (`vw_low_stock`, `vw_monthly_sales`), Stored Functions (`fn_stock_status`, `fn_inventory_value`), Determinism.

### Action:
Execute the stored functions and query the live views:

### Run Query:
```sql
-- 1. Stored Function: Compute total monetary valuation of pharmacy stock
SELECT fn_inventory_value() AS total_valuation_inr;

-- 2. Stored Function: Stock status categorisation
SELECT 
    name, 
    fn_stock_status(total_stock, reorder_level) AS status_classification
FROM vw_medicine_inventory
LIMIT 5;

-- 3. Database View: Low stock watchlist
SELECT * FROM vw_low_stock LIMIT 5;

-- 4. Database View: Aggregated monthly revenue and invoice metrics
SELECT * FROM vw_monthly_sales;
```

---

## 🔬 Demonstration 5: B-Tree Indexing & Query Execution Plans (EXPLAIN)
**DBMS Concept**: Index structures, Sequential Scan vs Index Scan, Composite Index for FEFO (First-Expiring First-Out) batch sorting.

### Action:
Run `EXPLAIN` to demonstrate the optimization achieved by the composite index `idx_batch_fefo`:

### Run Query:
```sql
-- Explain query used by POS Billing to select best unexpired batch
EXPLAIN SELECT batch_id, batch_number, expiry_date, quantity, selling_price
FROM batch
WHERE medicine_id = 1 AND status = 'Available' AND expiry_date > CURDATE()
ORDER BY expiry_date ASC;
```

### Explaining to Faculty:
- Point out the `key: idx_batch_fefo`.
- Explain that the composite index allows MySQL to satisfy the filter (`medicine_id`, `status`, `expiry_date`) and retrieve the pre-sorted order without executing an expensive in-memory or on-disk `filesort`.

---

## 🔬 Demonstration 6: Cross-Platform Full-Stack Sync (Web + Android)
**DBMS Concept**: Single Source of Truth, REST API abstraction, Role-Based Access Control (RBAC).

### Action:
1. Open the Web Dashboard at `http://localhost:3000/`.
2. Login as **Pharmacist** (`rajesh` / `password123`).
3. Complete a sale for **Paracetamol 500mg (1 unit)** via the POS screen.
4. Notice the invoice generated (`INV-...`) and the stock decrement on the dashboard.
5. In Android Studio or on the Android app, log in as **Admin** (`admin` / `password123`).
6. Navigate to **Inventory** -> Search **Paracetamol**.
7. Tap **View Batches (FEFO)**: Observe the decremented quantity reflecting the exact same number from the MySQL database.
8. Switch to **Cashier** (`neha`) in the app and attempt to view purchase order creation: observe backend RBAC enforcing restricted permissions.

---

## 📋 Comprehensive Concept Mapping Table

| DBMS Syllabus Topic | PharmaCare Implementation File | Concrete Demonstration Element |
| :--- | :--- | :--- |
| **Relational Data Model & 3NF** | `database/01_schema.sql` | 18 normalized tables, entity separation (`medicine`, `batch`, `category`) |
| **Integrity Constraints** | `database/01_schema.sql` | `CHECK (quantity >= 0)`, `CHECK (price >= 0)`, `UNIQUE (batch_number)` |
| **Referential Integrity** | `database/01_schema.sql` | Foreign keys with `ON DELETE RESTRICT` and `ON DELETE CASCADE` |
| **Relational Views** | `database/03_views.sql` | `vw_low_stock`, `vw_expiring_batches`, `vw_monthly_sales` |
| **Stored Functions** | `database/04_functions.sql` | `fn_stock_status()`, `fn_inventory_value()`, `fn_calculate_discount()` |
| **Active Triggers** | `database/05_triggers.sql` | `trg_after_batch_stock_update` recording to `inventory_audit` |
| **Stored Procedures & ACID** | `database/06_procedures.sql` | `sp_process_sale`, `sp_record_purchase` with row locking & rollback |
| **B-Tree Indexing & Optimization** | `database/07_indexes.sql` | `idx_batch_fefo`, `idx_sale_date`, `EXPLAIN` execution plans |
| **Security & RBAC** | `database/setup.sql`, `backend/` | Least-privilege account `pharmacare_user`, bcrypt hashing, JWT auth |
| **Application Integration** | `backend/src/`, `web/`, `android/` | Express REST API serving Web frontend and native Jetpack Compose Android app |

---

## ⚠️ Known Limitations & Future Enhancements

### Known Limitations:
1. **Single Node Deployment**: The database runs on a single authoritative MySQL instance; distributed replication (MySQL Group Replication) is not configured in this local academic environment.
2. **Barcode Scanner Hardware**: Barcode inputs in the web and mobile POS use standard text and keyboard input rather than a native hardware laser scanner driver.

### Future Enhancements:
1. **Automated Purchase Reordering**: A scheduled event (`EVENT_DAILY_REORDER`) that automatically drafts purchase orders when stock reaches critical thresholds.
2. **Offline-First Android Cache**: Integrating Room DB on Android for offline cart staging with background synchronization when network re-establishes.
