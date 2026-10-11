---
name: pharmacy-dbms
description: Guidelines and workflows for designing, maintaining, and testing the PharmaCare MySQL database, including schema normalization, views, procedures, functions, triggers, and query performance.
---

# Pharmacy DBMS Skill

This skill guides development and maintenance of the PharmaCare relational database in MySQL 8.0+.

## Database Architecture Principles
1. **Schema Normalization**: Maintain tables in 3NF to avoid update, insertion, and deletion anomalies. Separate master entities (`medicine`, `supplier`, `customer`, `employee`) from transactional entities (`purchase`, `sale`, `sales_return`) and item-level details (`purchase_item`, `sale_item`, `return_item`).
2. **Batch-Level Tracking**: Each physical supply of medicine has an independent record in the `batch` table with its own expiry date, purchase cost, selling price, and real-time quantity.
3. **Data Integrity**: Enforce constraints strictly at the database engine level:
   - `CHECK (quantity >= 0)` prevents negative inventory.
   - `CHECK (selling_price >= 0)` and `CHECK (purchase_price >= 0)` prevent negative pricing.
   - `UNIQUE (medicine_id, batch_number)` guarantees batch uniqueness per product.
   - Foreign key actions default to `RESTRICT` or `ON DELETE CASCADE` for parent-child invoice items.

## Key Programmatic Objects
- **Stored Procedures**:
  - `sp_process_sale`: Atomically creates invoice, verifies available non-expired stock with row locking (`FOR UPDATE`), deducts batch inventory, records items, and rolls back upon any error.
  - `sp_record_purchase`: Atomically enters purchase orders and upserts batch quantities.
  - `sp_get_low_stock`: Retrieves batches and medicines below designated reorder threshold.
- **Triggers**:
  - `trg_after_batch_stock_update`: Automatically audits inventory adjustments in `inventory_audit` table.
  - `trg_prevent_expired_batch_sale`: Guards against sales of batches whose expiry date has passed.
- **Views**:
  - `vw_low_stock`, `vw_expiring_batches`, `vw_monthly_sales`, `vw_supplier_purchase_summary`.
- **Functions**:
  - `fn_stock_status`: Evaluates stock status enum based on current levels and expiry date.
  - `fn_inventory_value`: Calculates cumulative purchase valuation of stock.

## Testing & Execution Workflow
- Test scripts using the CLI:
  ```powershell
  mysql -u root -ppassword < database/01_schema.sql
  mysql -u root -ppassword < database/02_seed.sql
  ```
- Always verify transactional rollback and trigger audit rows after running procedures.
