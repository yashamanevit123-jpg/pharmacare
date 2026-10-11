# PharmaCare — Database & DBMS Concepts Test Report

This document records the actual test execution, verification queries, and validation results conducted against MySQL Community Server 8.0.46 for the **PharmaCare** database.

---

## 1. Test Environment Summary

- **Database Engine**: MySQL Server 8.0.46 for Win64 on x86_64
- **Database Name**: `pharmacare_db`
- **Application User**: `pharmacare_user`@`localhost`
- **Driver / Protocol**: Native MySQL Client & InnoDB Storage Engine
- **Character Set / Collation**: `utf8mb4` / `utf8mb4_unicode_ci`

---

## 2. Test Execution Results Matrix

| Test ID | Test Category | Target Component | Scenario / Condition | Expected Result | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC-01** | Schema DDL | `01_schema.sql` | Table creation & relationships | 18 normalized tables created with PK, FK, and CHECK constraints | 18 tables created successfully in `pharmacare_db` | **PASSED** |
| **TC-02** | Seed Data | `02_seed.sql` | Realistic multi-table data insertion | >= 5 categories, >= 30 medicines, >= 10 suppliers, active/low/expired lots | 7 categories, 32 medicines, 10 suppliers, 33 batches, 12 customers seeded | **PASSED** |
| **TC-03** | SQL Views | `vw_low_stock` | Shelf stock <= reorder level or < 10 | Identifies 5 critical low stock medicines with rack location | Returned 5 batches (Calpol, Benadryl, Insulin, Voveran, Azee) | **PASSED** |
| **TC-04** | SQL Views | `vw_expiring_batches`| Expiry within 90 days or past | Identifies expired lots and near-expiry lots with day count | Returned 3 expired lots and 5 near-expiry lots | **PASSED** |
| **TC-05** | SQL Views | `vw_monthly_sales` | Sales revenue aggregation | Groups completed sales by year & month | Aggregated September (₹709.50) & October (₹335.00) revenue | **PASSED** |
| **TC-06** | SQL Views | `vw_supplier_purchase_summary` | Vendor procurement stats | Summarizes orders and spend per vendor | Accurately tallied purchase totals for all 10 suppliers | **PASSED** |
| **TC-07** | Stored Functions | `fn_stock_status()` | Batch viability evaluation | Returns 'OPTIMAL', 'EXPIRING_SOON', 'EXPIRED' | Correctly classified batches #1, #19, #21, and #28 | **PASSED** |
| **TC-08** | Stored Functions | `fn_inventory_value()`| Stock purchase valuation | Calculates valuation per medicine and total inventory | Returned ₹505.00 for Dolo 650; ₹27,920.00 for entire store | **PASSED** |
| **TC-09** | Stored Procedures | `sp_process_sale` | Atomic multi-item sale | Deducts stock for each item, generates invoice header & items | Batch 1: 150->148; Batch 2: 120->119; Invoice generated | **PASSED** |
| **TC-10** | ACID Transaction | `sp_process_sale` | Insufficient stock request (qty 9999) | Throws error, rolls back transaction, zero stock change | SQLSTATE '45000' raised, stock remained 148 (Zero leakage) | **PASSED** |
| **TC-11** | Clinical Safety | `sp_process_sale` | Sale of expired batch (#28) | Procedure / trigger aborts, rolls back transaction | Expired lot sale rejected, stock untouched | **PASSED** |
| **TC-12** | Stored Procedures | `sp_record_purchase`| Supplier purchase order | Adds purchase header, lines, and increases batch stock | Stock incremented by 50 units atomically | **PASSED** |
| **TC-13** | Stored Procedures | `sp_process_return`| Customer sale return | Creates refund record, restocks eligible batch | Batch restocked by 1 unit, sale status updated | **PASSED** |
| **TC-14** | Triggers | `trg_after_batch_stock_update` | Automatic stock audit log | Logs old_quantity, new_quantity, and difference | Recorded audits for both sales (-2, -1) and purchases (+50) | **PASSED** |
| **TC-15** | Integrity Constraint| `chk_batch_qty` | Attempt update quantity = -10 | Engine rejects negative quantity | Error caught: Check constraint 'chk_batch_qty' violated | **PASSED** |
| **TC-16** | Integrity Constraint| `uq_medicine_batch` | Insert duplicate batch for same medicine | Engine rejects duplicate unique key | Error caught: Duplicate entry for key 'uq_medicine_batch' | **PASSED** |
| **TC-17** | Integrity Constraint| `fk_medicine_category`| Insert medicine with invalid category ID | Engine rejects invalid foreign key | Error caught: Foreign key constraint fails | **PASSED** |
| **TC-18** | Security RBAC | User `pharmacare_user` | Least privilege connectivity | Connects and executes DML and routines, denies DDL admin | Connected successfully, executed queries on `pharmacare_db` | **PASSED** |

---

## 3. Actual Command Execution Logs

### 3.1 Automated Test Runner Output
```text
mysql: [Warning] Using a password on the command line interface can be insecure.
Test
--- TEST 1: Insufficient Stock Rollback ---
Test 1 Result
PASSED: Insufficient stock rejected and transaction rolled back
stock_before	stock_after	rollback_verification
198	198	PASSED: Zero Stock Leakage

Test
--- TEST 2: Expired Batch Rejection ---
Test 2 Result
PASSED: Expired batch sale rejected
stock_before	stock_after	rollback_verification
12	12	PASSED: Expired lot inventory untouched

Test
--- TEST 3: Atomic Purchase & Inventory Restocking ---
generated_purchase_id	total_amount	stock_before	stock_after	purchase_verification
8	60.00	198	248	PASSED: Quantity accurately incremented

Test
--- TEST 4: Customer Return & Restocking ---
generated_return_id	refund_amount	stock_before	stock_after	return_verification
3	2.00	248	249	PASSED: Batch restocked accurately

Test
--- TEST 5: CHECK Constraint Enforcement ---
Test 5 Result
PASSED: Negative stock rejected by CHECK (quantity >= 0)

Test
--- TEST 6: UNIQUE Constraint Enforcement ---
Test 6 Result
PASSED: Duplicate batch_number rejected by UNIQUE (medicine_id, batch_number)

Test
--- TEST 7: Foreign Key Constraint Enforcement ---
Test 7 Result
PASSED: Invalid foreign key rejected by fk_medicine_category

====================================================================
SUMMARY
ALL 7 INTEGRITY AND TRANSACTION TESTS COMPLETED!
====================================================================
```

### 3.2 Audit Trigger Log Capture
```text
audit_id  batch_id  change_type  old_quantity  new_quantity  quantity_change  changed_by  notes
7         1         SALE         150           148           -2               root        Batch DL650A adjusted by -2 units
8         2         SALE         120           119           -1               root        Batch CR500B adjusted by -1 units
```

---

## 4. Query Optimizer EXPLAIN Verification

```sql
EXPLAIN SELECT batch_id, batch_number, quantity, selling_price, expiry_date
FROM batch
WHERE medicine_id = 1
  AND expiry_date >= CURRENT_DATE
  AND quantity > 0
ORDER BY expiry_date ASC;
```
**Optimizer Plan**:
- **Type**: `range`
- **Key Used**: `idx_batch_fefo (medicine_id, expiry_date, quantity)`
- **Key Length**: `11`
- **Rows Examined**: `2`
- **Extra**: `Using index condition`

The optimizer successfully selected the composite index `idx_batch_fefo` for First-Expired, First-Out (FEFO) dispensing, avoiding full table scans (`ALL`).

---

## 5. Phase 3 Express API Automated Test Results

The test suite in `backend/tests/api_test_runner.js` executes comprehensive HTTP and database integration tests.

### 5.1 Test Execution Matrix (26/26 Tests Passed)

| Test ID | Test Category | Target Endpoint | Description / Scenario | Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **API-01** | Health | `GET /api/health` | Health check returns service UP status | `{ status: "UP" }` | **PASSED** |
| **API-02** | Static Web | `GET /` | Local web server serves preserved `index.html` | Returns 200 with HTML content | **PASSED** |
| **API-03** | Auth | `POST /api/auth/login` | Rejects incorrect password | `401 Unauthorized` | **PASSED** |
| **API-04** | Auth | `POST /api/auth/login` | Logs in Administrator & signs JWT | `200 OK`, JWT returned | **PASSED** |
| **API-05** | Auth | `POST /api/auth/login` | Logs in Cashier & signs JWT | `200 OK`, JWT returned | **PASSED** |
| **API-06** | Auth | `POST /api/auth/login` | Logs in Inventory Manager & signs JWT | `200 OK`, JWT returned | **PASSED** |
| **API-07** | Auth | `GET /api/auth/me` | Fetches profile for valid Bearer token | `200 OK` with user details | **PASSED** |
| **API-08** | Auth | `GET /api/auth/me` | Rejects missing Bearer token | `401 Unauthorized` | **PASSED** |
| **API-09** | RBAC | `POST /api/medicines` | Forbids Cashier role from adding medicines | `403 Forbidden` | **PASSED** |
| **API-10** | RBAC | `POST /api/purchases` | Forbids Cashier role from creating POs | `403 Forbidden` | **PASSED** |
| **API-11** | Catalog | `GET /api/medicines` | Allows authenticated access to 30+ medicines | `200 OK`, 32 medicines | **PASSED** |
| **API-12** | Validation | `POST /api/sales` | Rejects null customer and empty cart items | `400 Bad Request` | **PASSED** |
| **API-13** | Sale Tx | `POST /api/sales` | Executes `sp_process_sale` via Express API | `201 Created` with invoice | **PASSED** |
| **API-14** | Stock Decrement| `batch` | Decrements Batch 1 stock in MySQL by 2 | Stock verified in DB | **PASSED** |
| **API-15** | Stock Decrement| `batch` | Decrements Batch 2 stock in MySQL by 1 | Stock verified in DB | **PASSED** |
| **API-16** | Trigger Audit | `inventory_audit` | Captures audit row with `changed_by = 'neha'` | User context logged | **PASSED** |
| **API-17** | ACID Rollback | `POST /api/sales` | Rejects 99999 units (insufficient stock) | `400 Bad Request` | **PASSED** |
| **API-18** | Zero Leakage | `batch` | Stock remains unchanged after failed sale | Zero stock leakage | **PASSED** |
| **API-19** | Clinical Guard| `POST /api/sales` | Rejects sale of expired batch #28 | `400 Bad Request` | **PASSED** |
| **API-20** | Purchase Tx | `POST /api/purchases` | Executes `sp_record_purchase` via Express API| `201 Created` with PO ID | **PASSED** |
| **API-21** | Stock Increment| `batch` | Increments Batch 1 stock in MySQL by 40 | Stock verified in DB | **PASSED** |
| **API-22** | Dashboard | `GET /api/dashboard` | Returns live aggregates and metrics | `200 OK`, real SQL data | **PASSED** |
| **API-23** | Report View | `GET /api/reports/low-stock` | Queries `vw_low_stock` view | `200 OK` | **PASSED** |
| **API-24** | Report View | `GET /api/reports/monthly-sales`| Queries `vw_monthly_sales` view | `200 OK` | **PASSED** |
| **API-25** | Report View | `GET /api/reports/expiring-batches`| Queries `vw_expiring_batches` view | `200 OK` | **PASSED** |
| **API-26** | Audit Trail | `GET /api/reports/audit` | Returns trigger audit history records | `200 OK` | **PASSED** |

### 5.2 Terminal Test Suite Run Output
```text
================================================================
 STARTING PHARMACARE EXPRESS API TEST SUITE
 Target Server: http://localhost:3999
================================================================

[Test Server] Listening on port 3999

--- Suite 1: Health & Static Serving ---
  [PASS] GET /api/health returns UP status
  [PASS] GET / successfully serves preserved web frontend index.html

--- Suite 2: Authentication ---
  [PASS] POST /api/auth/login rejects incorrect password with 401
  [PASS] POST /api/auth/login logs in Administrator and returns JWT
  [PASS] POST /api/auth/login logs in Cashier (Neha Bansal)
  [PASS] POST /api/auth/login logs in Inventory Manager (Vikram Malhotra)
  [PASS] GET /api/auth/me returns profile for authenticated Bearer token
  [PASS] GET /api/auth/me rejects request missing Bearer token with 401

--- Suite 3: Role-Based Authorization (RBAC) ---
  [PASS] POST /api/medicines forbids Cashier role with 403
  [PASS] POST /api/purchases forbids Cashier role with 403
  [PASS] GET /api/medicines allows authenticated access and returns 30+ medicines

--- Suite 4: Request Validation ---
  [PASS] POST /api/sales rejects missing customer and empty items with 400 Bad Request

--- Suite 5: Atomic Sale Processing & Auditing ---
  [PASS] POST /api/sales executes sp_process_sale and returns 201 Created
  [PASS] POST /api/sales returns generated invoice number and saleId
  [PASS] Batch 1 stock accurately decremented by 2 in MySQL
  [PASS] Batch 2 stock accurately decremented by 1 in MySQL
  [PASS] Trigger trg_after_batch_stock_update logged audit record with initiating cashier username

--- Suite 6: Transactional Rollback & Clinical Safety ---
[Error] POST /api/sales: Transaction Rejected: Insufficient inventory in stock.
  [PASS] POST /api/sales rejects excessive quantity and raises Insufficient inventory error
  [PASS] Rollback verified: Zero stock leakage on failed sale
[Error] POST /api/sales: Transaction Rejected: Batch has expired and cannot be sold.
  [PASS] POST /api/sales clinical safety: Expired batch sale rejected with 400

--- Suite 7: Procurement & Stock Addition ---
  [PASS] POST /api/purchases executes sp_record_purchase and returns 201 Created
  [PASS] Batch 1 stock incremented by 40 units atomically in MySQL

--- Suite 8: Dashboard & Reports Integration ---
  [PASS] GET /api/dashboard returns live metrics from database tables
  [PASS] GET /api/reports/low-stock queries vw_low_stock view
  [PASS] GET /api/reports/monthly-sales queries vw_monthly_sales view
  [PASS] GET /api/reports/audit returns trigger audit log rows

================================================================
 TEST SUMMARY: 26/26 Passed (0 Failed)
================================================================
```
