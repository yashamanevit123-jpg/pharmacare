---
name: project-testing
description: Comprehensive testing procedures for PharmaCare, verifying database integrity, transactional rollback, API endpoint responses, authentication, and cross-platform consistency.
---

# Project Testing Skill

This skill provides step-by-step verification protocols across the database, backend, and clients.

## 1. Database Verification Protocols
- **Schema & Constraints**:
  - Test insertion of negative quantity -> expect check constraint violation (`CHECK (quantity >= 0)`).
  - Test insertion of duplicate batch number for same medicine -> expect unique constraint violation (`UNIQUE (medicine_id, batch_number)`).
  - Test deletion of category referenced by medicines -> expect foreign key violation (`RESTRICT`).
- **Transactional Rollback (`sp_process_sale`)**:
  - Scenario A: Attempt to sell more units than currently in stock. Verify procedure raises SQLSTATE '45000' and rolls back completely; zero records in `sale` or `sale_item`, batch stock untouched.
  - Scenario B: Attempt to sell expired batch. Verify procedure rejects transaction and rolls back.
  - Scenario C: Successful sale of multiple items. Verify invoice created, batch quantities decremented, and trigger logs written to `inventory_audit`.
- **Trigger Verification**:
  - Update batch quantity directly or via procedure -> verify new record in `inventory_audit` with matching `old_quantity`, `new_quantity`, and `quantity_change`.
- **Views & Stored Functions**:
  - Query `vw_low_stock`, `vw_expiring_batches`, `vw_monthly_sales`.
  - Execute `fn_stock_status()` and `fn_inventory_value()` across various batch states.

## 2. Express Backend Verification Protocols
- Test login with valid credentials (returns JWT token and employee role).
- Test login with invalid password (returns 401 Unauthorized).
- Test RBAC: Cashier attempting supplier creation (returns 403 Forbidden).
- Test atomic sale processing endpoint `/api/sales` with valid and invalid payloads.

## 3. Reporting Guidelines
- Record actual execution logs, status codes, query execution times, and affected row counts.
- Never state that a test passed without running the command.
