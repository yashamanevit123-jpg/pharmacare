# PharmaCare — DBMS Concepts & Normalization Guide

This document formally connects the PharmaCare implementation to the database management concepts taught in the engineering syllabus.

---

## 1. Relational Database Normalization

The initial prototype HTML UI stored data in flat, denormalized JavaScript arrays. For example, medicine records combined generic details, company names, supplier strings, batch codes, quantities, and rack locations into a single tuple. This leads directly to database anomalies.

### 1.1 Unnormalized Form (UNF) & The Problems
Consider a naive flat table:
`Medicine_Sales_Flat(SaleID, Date, CustomerName, CustomerPhone, DoctorName, MedName, Category, Company, BatchNo, Expiry, QtySold, Price, Total)`

- **Insertion Anomaly**: We cannot add a new medicine or supplier into the system until a sale or purchase has occurred.
- **Update Anomaly**: If a medicine's manufacturer or category name changes, multiple rows across past sales records must be modified. Incomplete updates result in inconsistent data.
- **Deletion Anomaly**: If a customer deletes their sale record, the only record of a medicine or batch might be permanently lost.

### 1.2 First Normal Form (1NF)
**Definition**: A relation is in 1NF if and only if all attribute values are atomic (no repeating groups, no arrays/lists stored in a single column), and every row has a unique identifier (Primary Key).
- **Implementation in PharmaCare**:
  - The arrays in the prototype (e.g. `items: [[1, 6, 2], [5, 2, 10]]` in sales) are eliminated.
  - Line items are decomposed into relational child tables (`sale_item`, `purchase_item`, `prescription_item`) with atomic attributes (`quantity`, `unit_price`, `subtotal`).
  - Every table possesses an explicit primary key (`INT AUTO_INCREMENT PRIMARY KEY`).

### 1.3 Second Normal Form (2NF)
**Definition**: A relation is in 2NF if it is in 1NF and every non-prime attribute is fully functionally dependent on the entire primary key (no partial functional dependencies on candidate keys).
- **Implementation in PharmaCare**:
  - In a composite table like `SaleItem(sale_id, batch_id, customer_name, employee_name, quantity, unit_price)`:
    - Primary Key: `(sale_id, batch_id)`
    - But `customer_name` and `employee_name` depend only on `sale_id`, not on `batch_id`. This is a partial dependency!
  - **Resolution**: Decompose into:
    - Header Table `sale(sale_id, customer_id, employee_id, sale_date, total_amount)` where PK is `sale_id`.
    - Item Table `sale_item(sale_item_id, sale_id, batch_id, quantity, unit_price, subtotal)` where line details depend on the line item.

### 1.4 Third Normal Form (3NF)
**Definition**: A relation is in 3NF if it is in 2NF and there are no transitive functional dependencies (no non-prime attribute depends on another non-prime attribute: if `X -> Y` and `Y -> Z`, then `Z` must not depend on `X` through `Y`).
- **Implementation in PharmaCare**:
  - In `medicine(medicine_id, name, category_name, category_description, manufacturer_name)`:
    - `medicine_id -> category_name` and `category_name -> category_description`.
    - This is a transitive dependency. If two medicines share the category "Tablet", duplicating the description risks inconsistency.
  - **Resolution**:
    - Extracted into distinct parent tables: `category(category_id, name, description)` and `manufacturer(manufacturer_id, name, contact_email)`.
    - In `medicine`, only foreign keys `category_id` and `manufacturer_id` are retained.
  - In `batch(batch_id, medicine_id, supplier_id, batch_number, expiry_date, quantity, purchase_price, selling_price)`:
    - Batch numbers and expiry dates belong strictly to physical supply lots, while medicine dosage and generic names belong to the `medicine` table.

---

## 2. Relational Algebra Operations

PharmaCare maps relational algebra operators directly to standard SQL queries:

| Relational Operator | Relational Algebra Notation | SQL Equivalent | PharmaCare Application |
| :--- | :--- | :--- | :--- |
| **Selection** | $\sigma_{condition}(R)$ | `WHERE` clause | Filtering near-expiry batches: `WHERE expiry_date <= DATE_ADD(CURRENT_DATE, INTERVAL 90 DAY)` |
| **Projection** | $\pi_{attributes}(R)$ | `SELECT col1, col2` | Displaying medicine names and selling prices on billing UI |
| **Cartesian Product** | $R \times S$ | `CROSS JOIN` | Used internally in relational query optimizers |
| **Natural / Equi-Join** | $R \bowtie_{R.id = S.id} S$ | `INNER JOIN` | Joining `sale` with `customer` and `employee` |
| **Left Outer Join** | $R \text{ } ⟕ \text{ } S$ | `LEFT JOIN` | Querying medicines with or without active inventory batches |
| **Union** | $R \cup S$ | `UNION` / `UNION ALL` | Consolidating incoming purchase movements and outgoing sales in stock ledgers |
| **Aggregation** | $\gamma_{groupBy, sum(attr)}(R)$ | `GROUP BY`, `SUM()`, `COUNT()` | Computing monthly revenue in `vw_monthly_sales` |

---

## 3. ACID Properties in Transaction Processing

PharmaCare guarantees ACID compliance during pharmacy sales (`sp_process_sale`) and supplier purchases (`sp_record_purchase`):

1. **Atomicity (All or Nothing)**:
   - When an invoice with 3 medicines is checked out, inserting the sale record, inserting 3 sale items, and decrementing 3 inventory batches must succeed together.
   - If unit stock is insufficient for the 3rd medicine, MySQL executes `ROLLBACK`. Zero partial bills are created and no stock is deducted.
2. **Consistency (Preserving Invariants)**:
   - Stock invariants: `CHECK (quantity >= 0)`.
   - Referential integrity: Foreign keys enforce that sales reference valid employees and registered customers.
3. **Isolation (Concurrent Safety)**:
   - To prevent the "lost update" anomaly when two cashiers sell the last box of Dolo 650 simultaneously, `sp_process_sale` executes:
     `SELECT quantity FROM batch WHERE batch_id = p_batch_id FOR UPDATE;`
   - Row-level exclusive locks ensure serialization of concurrent updates.
4. **Durability (Committed Changes Persist)**:
   - Once `COMMIT` executes, MySQL flush mechanisms (InnoDB redo logs and write-ahead logging) guarantee that data survives power failure or server restarts.

---

## 4. Database Programmatic Objects

### 4.1 SQL Views
Views provide virtual abstraction layers over underlying normalized tables:
- `vw_low_stock`: Computes aggregate stock per medicine across non-expired batches and filters where stock is below reorder level.
- `vw_expiring_batches`: Classifies batches expiring within 90 days.
- `vw_monthly_sales`: Aggregates transactions, gross revenue, discounts, and net totals by year and month.
- `vw_supplier_purchase_summary`: Aggregates purchase totals and fulfilled order counts per supplier.

### 4.2 Stored Functions
- `fn_stock_status(p_batch_id INT) RETURNS VARCHAR(20)`:
  Evaluates whether a batch is 'EXPIRED', 'EXPIRING_SOON', 'OUT_OF_STOCK', 'LOW_STOCK', or 'IN_STOCK'.
- `fn_inventory_value(p_medicine_id INT) RETURNS DECIMAL(12,2)`:
  Calculates the total monetary value of current available stock based on batch purchase price.

### 4.3 Stored Procedures
- `sp_process_sale`: Encapsulates atomic invoice creation, batch locking, inventory decrements, and automated rollback upon error.
- `sp_record_purchase`: Encapsulates atomic supplier intake, purchase order creation, and batch inventory increments.
- `sp_get_low_stock`: Parametric search for low-stock medicines given a customizable threshold.

### 4.4 Triggers
- `trg_after_batch_stock_update`: Automatically captures every inventory adjustment on `batch` into `inventory_audit` table (`old_quantity`, `new_quantity`, `quantity_change`, timestamp, and user context).
- `trg_prevent_expired_batch_sale`: Enforces clinical safety by intercepting sale item inserts and signaling an error if the batch expiry date is in the past.

---

## 5. Indexes and Query Optimization

- **B-Tree Indexes**:
  - Primary keys (`INT AUTO_INCREMENT`) have clustered B-tree indexes.
  - Foreign key columns (`medicine_id`, `supplier_id`, `customer_id`, `employee_id`) are indexed to optimize relational joins.
  - Search indexes (`medicine.name`, `batch.batch_number`, `batch.expiry_date`, `sale.sale_date`) accelerate range queries and search filters.
- **EXPLAIN Analysis**:
  - Run `EXPLAIN SELECT ...` to verify that index lookups (`type: ref` or `range`) replace full table scans (`type: ALL`).
