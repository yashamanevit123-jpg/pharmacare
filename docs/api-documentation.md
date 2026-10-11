# PharmaCare REST API Specification

This document details the RESTful API endpoints, request schemas, response formats, authentication flows, and Role-Based Access Control (RBAC) rules for **PharmaCare**.

---

## 1. Overview & Base Configuration

- **Base URL**: `http://localhost:3000/api`
- **Web Frontend**: `http://localhost:3000/` (served locally by Express)
- **Content-Type**: `application/json`
- **Authoritative Database**: MySQL Server 8.0+ (`pharmacare_db`)
- **Authentication**: Stateless JSON Web Token (JWT) passed via `Authorization: Bearer <token>` header.

---

## 2. Role-Based Access Control (RBAC) Matrix

| Endpoint | Method | Permitted Roles | Business Purpose |
| :--- | :--- | :--- | :--- |
| `/auth/login` | POST | Public | Authenticate user credentials and receive JWT |
| `/auth/me` | GET | All Authenticated | Fetch authenticated user profile |
| `/dashboard` | GET | All Authenticated | Retrieve live summary statistics from SQL views |
| `/medicines` | GET | All Authenticated | Search & filter medicine master catalog |
| `/medicines` | POST | Admin, Pharmacist | Create new medicine in master catalog |
| `/medicines/:id` | PUT | Admin, Pharmacist | Update medicine details |
| `/medicines/:id` | DELETE | Admin | Delete medicine (subject to referential integrity) |
| `/batches` | GET | All Authenticated | Retrieve active and expired inventory batches |
| `/sales` | GET | All Authenticated | View historical sales invoices |
| `/sales` | POST | Admin, Pharmacist, Cashier | Atomic sale billing via `sp_process_sale` |
| `/sales/:id` | GET | All Authenticated | View complete invoice header and line items |
| `/purchases` | GET | All Authenticated | View purchase orders |
| `/purchases` | POST | Admin, Pharmacist, Inventory Mgr | Atomic purchase order via `sp_record_purchase` |
| `/returns` | POST | Admin, Pharmacist, Inventory Mgr | Process customer returns via `sp_process_return` |
| `/reports/*` | GET | All Authenticated | View low-stock, expiry, and sales views |
| `/reports/audit` | GET | Admin, Inventory Mgr | View trigger-generated inventory audit trails |

---

## 3. Authentication Endpoints

### 3.1 Login
- **Endpoint**: `POST /api/auth/login`
- **Auth**: None
- **Request Body**:
```json
{
  "username": "neha",
  "password": "password123"
}
```
- **Success Response (`200 OK`)**:
```json
{
  "success": true,
  "message": "Login successful.",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "employeeId": 3,
    "username": "neha",
    "fullName": "Neha Bansal",
    "roleId": 3,
    "roleName": "Cashier"
  }
}
```
- **Error Responses**:
  - `400 Bad Request`: Missing username or password
  - `401 Unauthorized`: Invalid credentials or inactive user account
  - `429 Too Many Requests`: Exceeded 30 login attempts in 15 minutes

### 3.2 Authenticated Profile
- **Endpoint**: `GET /api/auth/me`
- **Headers**: `Authorization: Bearer <token>`
- **Success Response (`200 OK`)**:
```json
{
  "success": true,
  "user": {
    "employeeId": 1,
    "fullName": "System Administrator",
    "email": "admin@pharmacare.example.com",
    "phone": "9800000001",
    "username": "admin",
    "roleId": 1,
    "roleName": "Administrator"
  }
}
```

---

## 4. Sales & POS Billing Endpoints

### 4.1 Process Sale Invoice
- **Endpoint**: `POST /api/sales`
- **Headers**: `Authorization: Bearer <token>`
- **Permitted Roles**: `Administrator`, `Pharmacist`, `Cashier`
- **Database Engine Execution**: Calls MySQL stored procedure `sp_process_sale` with row locking (`SELECT ... FOR UPDATE`), transaction management, and automatic audit trigger logging.
- **Request Body**:
```json
{
  "customerId": 1,
  "paymentMethod": "UPI",
  "discountAmount": 5.00,
  "items": [
    { "batchId": 1, "quantity": 2 },
    { "batchId": 2, "quantity": 1 }
  ]
}
```
- **Success Response (`201 Created`)**:
```json
{
  "success": true,
  "message": "Sale successfully processed and inventory updated.",
  "data": {
    "saleId": 7,
    "invoiceNumber": "INV-20261011111748-955",
    "totalAmount": 1.50
  }
}
```
- **Error Responses**:
  - `400 Bad Request`: "Transaction Rejected: Insufficient inventory in stock." (Full rollback)
  - `400 Bad Request`: "Transaction Rejected: Batch has expired and cannot be sold." (Clinical safety)

---

## 5. Procurement & Purchases Endpoints

### 5.1 Record Supplier Purchase
- **Endpoint**: `POST /api/purchases`
- **Headers**: `Authorization: Bearer <token>`
- **Permitted Roles**: `Administrator`, `Inventory Manager`, `Pharmacist`
- **Database Engine Execution**: Calls MySQL stored procedure `sp_record_purchase` to atomically record the purchase order and increment batch inventory.
- **Request Body**:
```json
{
  "supplierId": 1,
  "invoiceNumber": "PUR-2026-0099",
  "purchaseDate": "2026-10-11",
  "notes": "Replenishment for seasonal demand",
  "items": [
    {
      "medicineId": 1,
      "batchNumber": "DL650A",
      "expiryDate": "2027-08-15",
      "quantity": 50,
      "unitCost": 1.20,
      "sellingPrice": 2.00
    }
  ]
}
```
- **Success Response (`201 Created`)**:
```json
{
  "success": true,
  "message": "Purchase recorded and stock added atomically.",
  "data": {
    "purchaseId": 6,
    "totalAmount": 60.00
  }
}
```

---

## 6. Dashboard & Reporting Endpoints

### 6.1 Real-Time Dashboard
- **Endpoint**: `GET /api/dashboard`
- **Headers**: `Authorization: Bearer <token>`
- **Success Response (`200 OK`)**:
```json
{
  "success": true,
  "data": {
    "metrics": {
      "totalMedicines": 32,
      "totalSellableStock": 2350,
      "lowStockCount": 5,
      "expiringBatchesCount": 8,
      "todaySalesRevenue": 150.00,
      "todaySalesCount": 2,
      "monthlySalesRevenue": 1044.50,
      "supplierCount": 10,
      "totalInventoryValue": 28450.00
    },
    "lowStockAlerts": [...],
    "expiringAlerts": [...],
    "recentSales": [...],
    "recentPurchases": [...],
    "topSelling": [...]
  }
}
```

### 6.2 Low-Stock View
- **Endpoint**: `GET /api/reports/low-stock`
- **Underlying SQL Object**: View `vw_low_stock`

### 6.3 Expiring Batches View
- **Endpoint**: `GET /api/reports/expiring-batches`
- **Underlying SQL Object**: View `vw_expiring_batches`

### 6.4 Monthly Sales View
- **Endpoint**: `GET /api/reports/monthly-sales`
- **Underlying SQL Object**: View `vw_monthly_sales`

### 6.5 Inventory Audit Trail
- **Endpoint**: `GET /api/reports/audit`
- **Permitted Roles**: `Administrator`, `Inventory Manager`
- **Underlying SQL Object**: Table `inventory_audit` (populated via triggers)
