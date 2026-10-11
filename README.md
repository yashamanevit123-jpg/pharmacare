# PharmaCare — Pharmacy Management System

A production-grade, mid-scale **Pharmacy Management System** built for engineering college DBMS coursework. The system features a responsive web frontend (preserving the original green-and-white visual identity and typography), an Express.js REST API backend, an authoritative MySQL 8.0+ relational database demonstrating core DBMS concepts, and a native Kotlin Android application built with Jetpack Compose.

---

## 🌟 System Architecture Overview

PharmaCare employs a strict **Three-Tier Architecture** ensuring that no client connects directly to MySQL:

```
┌─────────────────────────────────┐       ┌───────────────────────────────────┐
│     PharmaCare Web Client       │       │    Native Android Mobile App      │
│  (HTML5 / CSS3 / Vanilla JS)    │       │   (Kotlin / Jetpack Compose M3)   │
└────────────────┬────────────────┘       └─────────────────┬─────────────────┘
                 │ HTTP / JSON                              │ HTTP / JSON
                 │ (http://localhost:3000)                  │ (http://10.0.2.2:3000)
                 ▼                                          ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    Express.js REST API Backend Service                      │
│   • JWT Authentication & RBAC Middleware (Admin, Pharmacist, Cashier, etc.) │
│   • Request Validation & Sanitization                                       │
│   • Connection Pool (mysql2/promise)                                        │
│   • Static Asset Serving                                                    │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ SQL Connection (Port 3306)
                                       │ User: pharmacare_user
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                       Authoritative MySQL 8.0 Engine                        │
│   • 18 Normalized Tables (3NF) with Foreign Keys & Check Constraints        │
│   • Stored Procedures with ACID Transactions & Row Locks (sp_process_sale)  │
│   • Active Event Triggers for Tamper-Evident Auditing (inventory_audit)     │
│   • Relational Views (vw_low_stock, vw_monthly_sales, vw_expiring_batches)  │
│   • Stored Deterministic Functions (fn_inventory_value, fn_stock_status)    │
│   • B-Tree Composite FEFO Indexes for O(log N) Batch Optimization           │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📁 Repository Structure

```text
PharmaCare/
├── source-ui/                  # Preserved reference UI backup
│   └── PharmaCare – Pharmacy Management.html
├── web/                        # Production Web Dashboard
│   ├── index.html              # Preserved visual layout & HTML structure
│   ├── css/style.css           # Pine, Mint, Status semantic styles & typography
│   └── js/
│       ├── api.js              # REST API client with JWT handling
│       └── app.js              # Interactive DOM controller, POS cart, demo switcher
├── backend/                    # Express.js REST API Service
│   ├── src/
│   │   ├── config/database.js  # mysql2 connection pool with pharmacare_user
│   │   ├── middleware/auth.js  # JWT verification & RBAC authorization
│   │   ├── controllers/        # Auth, Medicine, Batch, Sale, Purchase, Report
│   │   ├── routes/             # REST endpoint routing
│   │   ├── app.js              # Express app & static web serving
│   │   └── server.js           # Server bootstrap (Port 3000)
│   ├── tests/
│   │   └── api_test_runner.js  # Automated 26-test API integration suite
│   ├── .env                    # Local environment config
│   └── package.json            # Dependencies: express, mysql2, bcryptjs, jsonwebtoken
├── database/                   # Authoritative MySQL 8.0 Database Scripts
│   ├── 01_schema.sql           # 18 normalized 3NF tables with constraints
│   ├── 02_seed.sql             # 32 medicines, 10 suppliers, 33 batches, sales, rx
│   ├── 03_views.sql            # Low stock, expiring batches, monthly revenue views
│   ├── 04_functions.sql        # fn_stock_status, fn_inventory_value, discount
│   ├── 05_triggers.sql         # trg_after_batch_stock_update, expired batch guard
│   ├── 06_procedures.sql       # sp_process_sale (ACID), sp_record_purchase, returns
│   ├── 07_indexes.sql          # Composite FEFO indexes & EXPLAIN execution plans
│   ├── 08_dbms_demo.sql        # Live faculty demonstration queries
│   ├── setup.sql               # Master user provisioning & database builder
│   └── test_runner.sql         # Automated SQL test suite
├── android/                    # Native Android Mobile Subproject
│   └── PharmaCareAndroid/      # Independent Gradle Kotlin Jetpack Compose project
│       ├── app/src/main/       # Manifest, Theme, Models, Repositories, Screens
│       ├── build.gradle.kts    # Kotlin DSL configuration
│       └── README.md           # Android Studio build and run guide
├── docs/                       # Project Documentation & Faculty Deliverables
│   ├── architecture.md         # Multi-client system architecture & RBAC
│   ├── er-diagram.md           # Mermaid ER diagram & cardinalities
│   ├── dbms-concepts.md        # Normalization (1NF, 2NF, 3NF), ACID, Relational algebra
│   ├── api-documentation.md    # Complete REST API specification
│   ├── testing.md              # Test execution proofs and results
│   ├── viva-questions.md       # 19 comprehensive DBMS viva questions with answers
│   └── faculty-demonstration-guide.md # 15-minute live demonstration script
├── .agents/skills/             # Project skills (dbms, web-ui, android-compose, testing)
├── AGENTS.md                   # Permanent architectural constraints and rules
├── PLAN.md                     # Implementation blueprint & verification matrix
└── README.md                   # Master project readme
```

---

## ⚡ Quick Start Guide

### Step 1: Database Setup (MySQL 8.0+)
Open PowerShell or Command Prompt as Administrator:
```powershell
# Execute the master setup script as MySQL root
mysql -u root -p < database/setup.sql
```
This creates:
- Database: `pharmacare_db`
- Application User: `pharmacare_user`@`localhost` with password `PharmaCare@2026!`
- 18 normalized 3NF tables, constraints, triggers, procedures, views, functions, indexes, and comprehensive seed data.

To verify the database:
```powershell
mysql -u root -p < database/test_runner.sql
```

---

### Step 2: Start the Express Backend & Web Server
```powershell
cd backend
npm install
npm start
```
The server will start on port 3000:
- **Web Frontend Dashboard**: [http://localhost:3000/](http://localhost:3000/)
- **REST API Endpoints**: [http://localhost:3000/api/](http://localhost:3000/api/)
- **Health Check**: [http://localhost:3000/api/health](http://localhost:3000/api/health)

To run the automated 26-test API integration suite:
```powershell
cd backend
npm test
```

---

### Step 3: Run the Native Android App (Optional / Mobile Demo)
1. Launch **Android Studio** (Koala, Ladybug, or newer).
2. Open the project located at:
   ```
   <WORKSPACE_ROOT>/android/PharmaCareAndroid
   ```
3. Allow Gradle to sync.
4. Launch any Android Virtual Device (AVD) running API 26–35.
5. Click **Run** (`Shift + F10`). The app is preconfigured to connect to `http://10.0.2.2:3000/api/`.

*(To test on a physical Android phone, connect your phone and PC to the same Wi-Fi and enter `http://<YOUR_PC_IP>:3000/api/` in the in-app server settings box).*

---

## 👥 Demo User Accounts (Role-Based Access Control)

Both the Web UI (via the **Switch Role** demo button in the header) and the Android app support 1-click account switching to demonstrate backend RBAC:

| Role | Username | Password | Permitted Modules & Privileges |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `password123` | Full privileges: Inventory, POS, Purchases, Reports, Audit Trail, User management |
| **Pharmacist** | `rajesh` | `password123` | POS Sales, Dispensing, Prescriptions, Batch inspections, Returns |
| **Cashier** | `neha` | `password123` | POS Sales Billing, Customer lookups, Invoice printing (No PO entry) |
| **Inventory Manager** | `vikram` | `password123` | Purchase Orders, Supplier catalog, Stock reception, Low-stock alerts |

---

## 🎓 Faculty Review & Viva Deliverables

The documentation suite is ready for second- and third-year engineering evaluations:

1. **[Faculty Demonstration Guide](file:///d:/D%20Drive/VIT_Stuff/TY/2nd_Sem/DBMS/CP/pharmacare/docs/faculty-demonstration-guide.md)**: A step-by-step 15-minute live demonstration script covering ACID transactions, atomic rollback on insufficient stock, trigger execution, database views, and cross-platform synchronization.
2. **[DBMS Viva Questions & Answers](file:///d:/D%20Drive/VIT_Stuff/TY/2nd_Sem/DBMS/CP/pharmacare/docs/viva-questions.md)**: 19 detailed, syllabus-aligned questions covering Normalization (1NF, 2NF, 3NF), Relational Algebra, ACID properties, row-level locking, Triggers, Views, and Indexing.
3. **[ER Diagram & Cardinality Specification](file:///d:/D%20Drive/VIT_Stuff/TY/2nd_Sem/DBMS/CP/pharmacare/docs/er-diagram.md)**: Mermaid ER diagram, primary/foreign key mappings, and table specifications.
4. **[DBMS Concepts Implementation Guide](file:///d:/D%20Drive/VIT_Stuff/TY/2nd_Sem/DBMS/CP/pharmacare/docs/dbms-concepts.md)**: Mathematical and conceptual explanations of normal forms and relational algebra.
5. **[REST API Specification](file:///d:/D%20Drive/VIT_Stuff/TY/2nd_Sem/DBMS/CP/pharmacare/docs/api-documentation.md)**: Endpoints, schemas, and HTTP status codes.
6. **[Testing & Verification Report](file:///d:/D%20Drive/VIT_Stuff/TY/2nd_Sem/DBMS/CP/pharmacare/docs/testing.md)**: Output proofs for all automated database and API integration tests.
