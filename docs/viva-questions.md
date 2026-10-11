# PharmaCare — Comprehensive DBMS Viva Questions & Answers

This guide provides in-depth, beginner-friendly explanations of all Database Management Systems (DBMS) concepts implemented in **PharmaCare**, specifically tailored for 2nd- and 3rd-year engineering coursework, laboratory examinations, and project viva presentations.

---

## 📚 Table of Contents
1. [Relational Model & System Architecture](#1-relational-model--system-architecture)
2. [Database Normalization (1NF, 2NF, 3NF) & Anomalies](#2-database-normalization-1nf-2nf-3nf--anomalies)
3. [Keys, Constraints & Referential Integrity](#3-keys-constraints--referential-integrity)
4. [SQL Queries, Joins & Aggregations](#4-sql-queries-joins--aggregations)
5. [Database Views (Simple vs Complex)](#5-database-views-simple-vs-complex)
6. [Stored Procedures vs Stored Functions](#6-stored-procedures-vs-stored-functions)
7. [MySQL Triggers & Audit Logging](#7-mysql-triggers--audit-logging)
8. [Transaction Management & ACID Properties](#8-transaction-management--acid-properties)
9. [Indexing, B-Trees & Query Optimization (EXPLAIN)](#9-indexing-b-trees--query-optimization-explain)
10. [Database Security & Access Control (RBAC)](#10-database-security--access-control-rbac)

---

## 1. Relational Model & System Architecture

### Q1: What architecture does PharmaCare use, and why?
**Answer:**
PharmaCare uses a **Three-Tier Client-Server Architecture**:
1. **Presentation Tier (Clients)**: The Web Dashboard (HTML5/CSS3/JavaScript) and Native Android Mobile App (Kotlin / Jetpack Compose).
2. **Application Tier (Logic)**: Node.js / Express.js REST API providing business routing, JWT authentication, and input validation.
3. **Data Tier (Storage)**: MySQL 8.0 relational database holding normalized tables, stored procedures, triggers, views, and functions.

**Why?**
- Prevents direct exposure of the database to public network clients.
- Centralizes security, role-based authorization, and connection pooling.
- Allows both Web and Android frontends to share one single source of truth without duplicating transactional inventory logic.

### Q2: What is a Relational Database, and why is MySQL chosen over NoSQL for a pharmacy?
**Answer:**
A Relational Database stores data in structured tables (relations) consisting of rows (tuples) and columns (attributes), enforcing mathematical integrity constraints.

**Why MySQL over NoSQL?**
- **Strict ACID Guarantees**: A pharmacy cannot tolerate eventual consistency. When an antibiotic batch is sold, stock must decrement immediately; duplicate sales of the same expired pill or negative stock must never happen.
- **Foreign Key Integrity**: Medicines, Batches, Purchases, and Invoices must have referential relationships enforced at the engine level.

---

## 2. Database Normalization (1NF, 2NF, 3NF) & Anomalies

### Q3: What is Database Normalization, and what problems does it solve?
**Answer:**
Normalization is the systematic process of decomposing tables to minimize **data redundancy** and avoid **data modification anomalies**:
1. **Insertion Anomaly**: Inability to record certain facts without adding unrelated data (e.g., unable to add a new category without adding a dummy medicine).
2. **Update Anomaly**: Changing a piece of information in one place leaves other copies unchanged, causing data inconsistency (e.g., updating a supplier's phone number in 500 purchase rows).
3. **Deletion Anomaly**: Deleting a record unintentionally wipes out unrelated facts (e.g., deleting the last medicine from a manufacturer deletes the manufacturer itself).

### Q4: How are 1NF, 2NF, and 3NF applied in PharmaCare?
**Answer:**

#### 1. First Normal Form (1NF)
- **Rule**: Each column must contain atomic (indivisible) values, and each record must have a primary key with no repeating groups.
- **In PharmaCare**: Rather than storing multiple items as a comma-separated string `sale.items = "Paracetamol (2), Amoxicillin (1)"`, we created a dedicated table `sale_item` where each row represents exactly one item.

#### 2. Second Normal Form (2NF)
- **Rule**: Must be in 1NF, and all non-key attributes must be **fully functionally dependent** on the primary key (no partial dependencies on a composite key).
- **In PharmaCare**: In `sale_item(sale_id, batch_id, quantity, unit_price)`, the composite key is `(sale_id, batch_id)`. We do *not* store the customer name or invoice date in `sale_item` because those depend only on `sale_id`, not on `batch_id`. Customer details reside in `sale(sale_id)`.

#### 3. Third Normal Form (3NF)
- **Rule**: Must be in 2NF, and there must be **no transitive dependencies** ($X \rightarrow Y$ and $Y \rightarrow Z$, where non-key attribute $Z$ depends on non-key attribute $Y$).
- **In PharmaCare**: In `medicine`, we store `category_id`, but we do *not* store `category_name` or `category_description`. If we did, `medicine_id -> category_id -> category_description` would be a transitive dependency. Instead, category details reside strictly in `category`.

---

## 3. Keys, Constraints & Referential Integrity

### Q5: What is the difference between Primary Key, Candidate Key, and Unique Key?
**Answer:**
- **Candidate Key**: Any minimal set of attributes that uniquely identifies a row (e.g., in `employee`, both `employee_id` and `email` can uniquely identify an employee).
- **Primary Key**: The specific candidate key chosen by the database designer as the primary identifier (e.g., `medicine_id INT AUTO_INCREMENT`). Cannot be `NULL`.
- **Unique Key**: A constraint ensuring column values are distinct across rows (e.g., `batch(batch_number)` or `employee(email)`), but allows at most one `NULL` value in SQL standard.

### Q6: What is Referential Integrity and Foreign Key Cascading?
**Answer:**
Referential Integrity requires that a foreign key value must either match an existing primary key value in the referenced parent table or be `NULL`.

**PharmaCare Cascade Decisions**:
- `sale_item` references `sale(sale_id) ON DELETE CASCADE`: If a sale record is erased, its constituent line items are cleanly removed.
- `batch` references `medicine(medicine_id) ON DELETE RESTRICT`: Prevents accidentally deleting a medicine if historical batch inventory or sales exist for it.
- `CHECK (quantity >= 0)`: Database-level constraint guaranteeing that stock can never drop below zero, preventing stock leakage bugs.

---

## 4. SQL Queries, Joins & Aggregations

### Q7: What is the difference between INNER JOIN and LEFT JOIN? Give examples from PharmaCare.
**Answer:**
- **INNER JOIN**: Returns only rows where there is a match in **both** tables.
  - *Example*: Finding medicines that currently have batches:
    ```sql
    SELECT m.name, b.batch_number, b.quantity
    FROM medicine m
    INNER JOIN batch b ON m.medicine_id = b.medicine_id;
    ```
- **LEFT JOIN (or LEFT OUTER JOIN)**: Returns **all** rows from the left table, and matching rows from the right table (or `NULL` if no match exists).
  - *Example*: Catalog listing including medicines that currently have 0 stock or no batches:
    ```sql
    SELECT m.name, COALESCE(SUM(b.quantity), 0) AS total_stock
    FROM medicine m
    LEFT JOIN batch b ON m.medicine_id = b.medicine_id AND b.status = 'Available'
    GROUP BY m.medicine_id, m.name;
    ```

### Q8: What is the difference between WHERE and HAVING?
**Answer:**
- `WHERE` filters rows **before** any aggregation (GROUP BY) takes place. It operates on individual tuples.
- `HAVING` filters group rows **after** aggregation has occurred. It operates on aggregate functions (`SUM`, `COUNT`, `AVG`).
- *Example in PharmaCare*:
  ```sql
  SELECT m.name, SUM(b.quantity) AS stock
  FROM medicine m
  JOIN batch b ON m.medicine_id = b.medicine_id
  WHERE b.expiry_date > CURDATE()        -- WHERE filters unexpired batches before grouping
  GROUP BY m.medicine_id, m.name
  HAVING stock < 50;                     -- HAVING filters grouped result where sum < 50
  ```

---

## 5. Database Views (Simple vs Complex)

### Q9: What is a Database View, and what are its advantages?
**Answer:**
A View is a **virtual table** based on the result-set of a pre-compiled SQL query. It does not store data on disk physically (unless it is an indexed/materialized view).

**Advantages**:
1. **Security**: Hides sensitive columns (e.g., exposing medicine inventory without exposing profit margin or unit cost).
2. **Simplicity**: Encapsulates complex joins, CASE statements, and calculations into a single clean table name.
3. **Maintainability**: If the underlying schema changes, updating the view keeps frontend queries unbroken.

### Q10: What views exist in PharmaCare?
**Answer:**
1. `vw_low_stock`: Aggregates active batches and filters medicines where total stock $\le$ reorder level.
2. `vw_expiring_batches`: Selects unexpired batches expiring within the next 90 days, computing `days_remaining` and status badges.
3. `vw_monthly_sales`: Aggregates invoice sub-totals, discounts, net revenue, and invoice count per calendar month.
4. `vw_supplier_purchase_summary`: Summarizes purchase orders, total expenditure, and items received per supplier.

---

## 6. Stored Procedures vs Stored Functions

### Q11: What is the difference between a Stored Procedure and a Stored Function?
**Answer:**

| Characteristic | Stored Function (`FUNCTION`) | Stored Procedure (`PROCEDURE`) |
| :--- | :--- | :--- |
| **Return Value** | Must return exactly **one** scalar value using `RETURNS` and `RETURN`. | Does not use `RETURN`; returns results via `SELECT` or `OUT` parameters. |
| **Usage in SQL** | Can be called directly inside `SELECT`, `WHERE`, `ORDER BY` clauses. | Called explicitly using `CALL procedure_name(...)`. |
| **Transactions** | Cannot manage transactions (`START TRANSACTION`, `COMMIT`, `ROLLBACK` not permitted). | Can fully control transactions, commit, rollback, and use row locks. |
| **PharmaCare Example**| `fn_inventory_value()`, `fn_stock_status()` | `sp_process_sale()`, `sp_record_purchase()` |

### Q12: Why use `sp_process_sale` instead of running `UPDATE` and `INSERT` directly in Node.js?
**Answer:**
1. **Network Roundtrips**: A multi-item sale in Node.js would require 5–10 roundtrips over TCP. In a stored procedure, one call executes all operations inside the database engine memory.
2. **ACID Transaction Isolation**: Row-level locks (`SELECT ... FOR UPDATE`) and immediate rollback happen directly in the database engine, avoiding distributed transaction failures if Node.js crashes mid-flight.
3. **Platform Independence**: Both Web and Android clients benefit from the exact same business logic without duplicating validation rules in JavaScript and Kotlin.

---

## 7. MySQL Triggers & Audit Logging

### Q13: What is a Trigger? How does it differ from a Stored Procedure?
**Answer:**
- A Trigger is a stored program invoked **automatically** by the DBMS in response to an event (`INSERT`, `UPDATE`, `DELETE`) on a specific table.
- Unlike a stored procedure which is called explicitly by application code, a trigger cannot be bypassed by a rogue query or administrator script.

### Q14: Explain `trg_after_batch_stock_update` in PharmaCare.
**Answer:**
- **Event**: `AFTER UPDATE ON batch FOR EACH ROW`.
- **Logic**: Checks `IF OLD.quantity <> NEW.quantity`.
- **Action**: Inserts an immutable row into `inventory_audit` recording:
  - `batch_id`, `old_quantity`, `new_quantity`, `quantity_delta` (`NEW - OLD`)
  - `change_type` (`SALE_DEDUCTION`, `PURCHASE_RECEIPT`, `RETURN_RESTOCK`, or `CORRECTION`)
  - `user_action` (`CURRENT_USER()`), timestamp (`NOW()`).
- **Benefit**: Establishes a tamper-evident audit log for strict pharmaceutical compliance (Schedule H drug tracking).

---

## 8. Transaction Management & ACID Properties

### Q15: Explain the ACID properties with PharmaCare examples.
**Answer:**

1. **Atomicity (All or Nothing)**:
   - In `sp_process_sale`, if a customer purchases 3 medicines and 2 are in stock but the 3rd batch has only 1 pill remaining when 2 are requested:
   - The entire transaction is rolled back (`ROLLBACK`). No invoice is created, and the first 2 items are not deducted.

2. **Consistency (Valid State to Valid State)**:
   - Enforced by database integrity constraints: foreign keys exist, `quantity >= 0` check constraint passes, and batch expiry date is strictly in the future.

3. **Isolation (Concurrent Transactions Don't Interfere)**:
   - Two cashiers trying to dispense the last 10 units of Amoxicillin simultaneously are managed using row-level locking:
   ```sql
   SELECT quantity, expiry_date FROM batch WHERE batch_id = p_batch_id FOR UPDATE;
   ```
   - Cashier 1 acquires the exclusive row lock; Cashier 2 must wait until Cashier 1 commits, preventing race conditions and double-spending.

4. **Durability (Committed Changes Survive Crashes)**:
   - Once `COMMIT` executes, MySQL writes the transaction to the Redo Log (WAL - Write-Ahead Logging) on non-volatile disk. Even if power fails 1 millisecond later, the sale is not lost.

---

## 9. Indexing, B-Trees & Query Optimization (EXPLAIN)

### Q16: How does a B-Tree index work?
**Answer:**
A B-Tree (Balanced Tree) is a self-balancing search tree data structure.
- Leaf nodes contain search keys in sorted order with pointers to actual table rows.
- Search time complexity is reduced from $O(N)$ (Full Table Scan) to $O(\log N)$ (Index Lookup).
- Leaf nodes are linked via a doubly linked list, making range queries (`BETWEEN`, `>`, `<`) extremely efficient.

### Q17: What is the FEFO composite index in PharmaCare, and how does `EXPLAIN` prove its benefit?
**Answer:**
- FEFO stands for **First-Expiring First-Out**.
- In pharmacy operations, medicines must be dispensed in order of earliest expiration.
- Composite Index:
  ```sql
  CREATE INDEX idx_batch_fefo ON batch (medicine_id, status, expiry_date, quantity);
  ```
- **EXPLAIN Analysis**:
  - Without Index: `type: ALL`, scanning all 33+ rows, `Using filesort`.
  - With Index: `type: ref`, scanning only relevant rows for that medicine, `Using index condition`, eliminating disk-based temporary sorting.

---

## 10. Database Security & Access Control (RBAC)

### Q18: What is the Principle of Least Privilege in DBMS?
**Answer:**
Users and applications should be granted only the minimum permissions necessary to perform their legitimate tasks.

**In PharmaCare**:
- `root` (Database Administrator): Has `ALL PRIVILEGES` including `GRANT OPTION`, schema alteration, and user creation. Administrative credentials never touch the web server or application code.
- `pharmacare_user`@`localhost` (Application Service Account): Granted only `SELECT, INSERT, UPDATE, DELETE, EXECUTE` on `pharmacare_db`. Cannot drop tables, alter schema, or access server-wide data.

### Q19: How does PharmaCare prevent SQL Injection?
**Answer:**
1. All queries executed by the Node.js backend use **Prepared Statements with Parameter Placeholders (`?`)**:
   ```javascript
   const [rows] = await db.query(
     'SELECT * FROM medicine WHERE category_id = ? AND status = ?',
     [categoryId, status]
   );
   ```
2. The MySQL client library sends the SQL structure and data values in separate network packets. Input strings (e.g. `' OR '1'='1`) are treated as literal text values, completely eliminating SQL injection vectors.
