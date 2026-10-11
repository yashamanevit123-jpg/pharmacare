# MASTER PROMPT — PHARMACARE WEB + ANDROID PHARMACY MANAGEMENT SYSTEM

Act as a senior full-stack software architect, MySQL/DBMS expert, Android developer, security engineer and technical mentor. You are working inside Google Antigravity with access to my existing project files.

Build a complete, working engineering college project named:

**PharmaCare — Mid-Scale Pharmacy Management System with Web and Native Android Applications**

The project must clearly demonstrate DBMS concepts taught in our engineering syllabus. We are second-year engineering students, so the implementation must be technically correct, understandable, well-documented and suitable for a live faculty demonstration.

Do not deliver only a design, static interface, or pseudocode. Generate the real code, connect the components, run appropriate tests, fix errors and document exactly how to execute the system.

## 1. First inspect my existing website

I have supplied a prebuilt frontend file named:

`PharmaCare – Pharmacy Management.html`

Inspect this file and all other files in the open workspace before modifying anything.

My existing website contains:
- PharmaCare branding
- A green and white pharmacy dashboard
- A navigation menu and summary cards
- Medicine inventory with search and filters
- Billing and sales history
- Prescription records
- Purchase history
- Responsive styling and a light/dark theme

The existing HTML contains JavaScript arrays holding sample medicines, customers, purchases, sales and prescription information. Some operations only modify in-memory JavaScript data; they do not persist to MySQL.

### UI preservation rules

1. Treat the supplied HTML as the reference design.
2. Preserve its branding, colour palette, typography, layout, responsiveness and familiar navigation wherever practical.
3. Make a backup of the original file before restructuring or editing it.
4. You may separate its HTML, CSS and JavaScript into maintainable files, but do not discard existing functionality unnecessarily.
5. Improve usability where necessary without replacing the site with a completely unrelated dashboard template.
6. Do not leave hard-coded JavaScript arrays as the production data source after API integration.
7. Keep seed data in the database or test fixtures, not as fake live statistics.
8. Explain any important changes made to the original UI.

First prepare a short workspace inspection report, a proposed architecture, a database design and an implementation checklist. Then continue with implementation in the stages specified below. Do not stop after generating a plan.

## 2. Required technology stack

Use this stack consistently throughout the project.

### Web frontend
- HTML5
- CSS3
- Vanilla JavaScript
- Fetch API for backend requests

Keep my existing frontend design. Do not migrate it to React, Angular or another framework unless there is a compelling technical reason, which must be explained first.

### Backend
- Node.js LTS
- Express.js
- MySQL2 using its promise-based API
- bcrypt for password hashing
- JWT-based authentication with appropriate token handling
- Input validation
- Environment variables
- Parameterized SQL queries
- Centralized error handling
- Role-based backend authorization
- Helmet and suitable API rate limiting

Use a clean, modular backend rather than putting the entire server in one file.

### Database
- MySQL 8.4 LTS
- MySQL Workbench for database inspection and testing
- Explicit schema and seed scripts
- SQL views, stored procedures, stored functions, triggers, transactions, constraints and indexes

### Native Android application
Develop a separate native Android application in Android Studio using:

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- ViewModel
- Kotlin Coroutines and StateFlow
- Retrofit and OkHttp for REST API communication
- A consistent JSON serialization library
- Gradle Kotlin DSL

Use Android's modern application architecture with a UI layer, ViewModels, repositories and a data layer.

Do not create the mobile app using Flutter, React Native or a WebView wrapper. I specifically want a native Android application that can be opened, built, run, debugged and demonstrated in Android Studio.

Choose compatible stable library and build-tool versions based on the installed Android Studio and its supported Gradle/JDK configuration. Do not blindly copy old dependency versions from tutorials.

### Important restriction

Both the web and Android applications must use the same Express API and MySQL database. Neither frontend may access MySQL directly.

Do not add Firebase, Supabase, MongoDB or SQLite as replacement databases. An optional local cache may be proposed separately, but MySQL remains the authoritative data source.

## 3. Architecture and folder structure

Create a maintainable monorepo. Adapt the structure to my current files rather than deleting or overwriting them.

Use a structure similar to:

```text
PharmaCare/
├── source-ui/
│   └── PharmaCare – Pharmacy Management.html
├── web/
│   ├── index.html
│   ├── css/
│   ├── js/
│   └── assets/
├── backend/
│   ├── src/
│   │   ├── config/
│   │   ├── middleware/
│   │   ├── routes/
│   │   ├── controllers/
│   │   ├── services/
│   │   ├── repositories/
│   │   ├── validators/
│   │   └── app.js
│   ├── tests/
│   ├── package.json
│   └── .env.example
├── database/
│   ├── 01_schema.sql
│   ├── 02_seed.sql
│   ├── 03_views.sql
│   ├── 04_functions.sql
│   ├── 05_triggers.sql
│   ├── 06_procedures.sql
│   ├── 07_indexes.sql
│   ├── 08_dbms_demo.sql
│   └── setup.sql
├── android/
│   └── PharmaCareAndroid/
├── docs/
│   ├── architecture.md
│   ├── er-diagram.md
│   ├── api-documentation.md
│   ├── dbms-concepts.md
│   ├── testing.md
│   └── viva-questions.md
├── .agents/
│   └── skills/
├── AGENTS.md
├── .gitignore
└── README.md
```

Modify the structure if required, but preserve clear boundaries between web, backend, database and Android code.

The README must explain how to open the Android subproject independently in Android Studio.

## 4. Web application features

Preserve the existing dashboard and expand the website into a functional pharmacy management system.

### A. Dashboard
Show live database results for:
- Total medicines
- Total sellable stock
- Low-stock medicines
- Expired and near-expiry batches
- Today's sales revenue
- Monthly sales revenue
- Recent sales and purchases
- Top-selling medicines
- Supplier count

Use real SQL-backed API responses.

### B. Medicine management
Support creating, viewing, updating, searching and deleting eligible medicine records.

Store medicine name, category, manufacturer, dosage form, unit information and reorder level.

Do not allow deletion if existing transactions or referential integrity rules prohibit it.

### C. Batch inventory
Maintain batches separately from medicine master records. A medicine can have several batches.

Store batch number, manufacturing date when available, expiry date, purchase cost, selling price and current quantity.

Provide:
- Batch-wise quantity
- Total stock per medicine
- Low-stock filtering
- Near-expiry alerts
- Expired inventory identification
- Stock movement and audit history

Expired batches must not be sold.

### D. Billing and sales
Allow the cashier to:
- Search for medicines
- Select eligible batches or use earliest-expiry-first allocation
- Add several items to an invoice
- Change quantities and remove items
- Calculate line totals and invoice totals
- Select a payment method
- Record the sale
- View the final invoice
- Print the bill

Validate all stock quantities on the backend and in the database operation, not just in the UI.

If any item cannot be fulfilled, reject the whole invoice and roll back its database changes.

### E. Purchases and suppliers
Support supplier records, purchase invoice headers, multiple purchase items, batch details, expiry dates, purchase quantities and prices.

Stock must increase only after a purchase is successfully recorded.

### F. Customers and returns
Support basic customer records and purchase history.

Implement sales returns that reference an original invoice. Record returned items, quantities, dates, reasons and status. Increase sellable stock only if the return is approved and the goods are eligible for resale.

### G. Prescriptions
Preserve the existing prescriptions screen where practical. Store prescription headers, doctors and prescribed items using related database tables.

Treat these as demonstration records. Do not add diagnostic or medical-treatment recommendations.

### H. Employees and roles
Implement roles such as administrator, pharmacist, cashier and inventory manager.

Enforce permissions on the backend. Hiding a button on the frontend is not a security control.

### I. Reports
Implement:
- Daily sales
- Monthly sales
- Top five medicines by quantity sold
- Supplier-wise purchases
- Low-stock report
- Near-expiry and expired batches
- Inventory valuation
- Audit history

Use date filters and actual query results.

Every visible button, form, search, filter and report must perform its intended operation.

## 5. MySQL database design

Design a normalized relational database containing at least:

1. `Category`
2. `Medicine`
3. `Batch`
4. `Supplier`
5. `Purchase`
6. `PurchaseItem`
7. `Customer`
8. `Role`
9. `Employee`
10. `Sale`
11. `SaleItem`
12. `Return`
13. `ReturnItem`
14. `Doctor`
15. `Prescription`
16. `PrescriptionItem`
17. `InventoryAudit`

Add supporting tables if a sound design requires them.

### Database requirements
- Define primary keys and foreign keys.
- Use appropriate data types.
- Apply `NOT NULL`, `UNIQUE`, `CHECK` and `DEFAULT` constraints where appropriate.
- Use appropriate foreign-key delete/update behavior.
- Separate medicine details from batch details.
- Separate purchase and sale headers from their line items.
- Use appropriate decimal types for monetary values.
- Prevent negative stock.
- Prevent duplicate invoice items where the business rules require uniqueness.
- Use consistent naming conventions.

Create an ER diagram in Mermaid ER notation. Include cardinalities, primary keys and foreign keys. Keep the diagram consistent with the SQL schema actually created.

Explain 1NF, 2NF and 3NF using examples from the final tables. Show how the schema avoids insertion, update and deletion anomalies.

## 6. Mandatory DBMS concepts

The main objective is to make our taught DBMS concepts visible in the actual implementation.

### A. SQL and relational operations
Include executable examples of:
- INSERT, SELECT, UPDATE and DELETE
- WHERE, ORDER BY and GROUP BY
- HAVING
- COUNT, SUM, AVG, MIN and MAX
- INNER JOIN and LEFT JOIN
- Subqueries
- Date and string functions
- Suitable aggregate and multi-table reports

Use these queries in the application wherever appropriate.

### B. Views
Create and test:
- `vw_low_stock`
- `vw_expiring_batches`
- `vw_monthly_sales`
- `vw_supplier_purchase_summary`

Use the views where appropriate in actual reports.

### C. Stored procedures
Create and test:
- `sp_process_sale`
- `sp_record_purchase`
- `sp_get_low_stock`

The sale operation must validate available stock, reject expired batches, create the invoice and items, adjust inventory consistently, and handle failures without partially saving the sale.

Use an appropriate transaction and error handler. Use row-level locking where needed for concurrent inventory changes.

The purchase operation must save the purchase and adjust inventory atomically.

Call these database operations from Express where appropriate, instead of reimplementing their critical business rules independently in JavaScript.

Ensure that transaction ownership is clear: do not accidentally nest transaction handling between the procedure and backend or deduct stock twice.

### D. Stored functions
Create and test:
- `fn_stock_status`
- `fn_inventory_value`

Use them in SQL queries or report calculations. Explain their parameters, return values and purpose.

### E. Triggers
Create genuine MySQL triggers to audit relevant batch stock changes.

The audit records should include batch reference, old quantity, new quantity, quantity difference and timestamp. Capture the initiating application user where safely available; otherwise identify the change as system-generated rather than inventing a user.

Test the triggers with actual database inserts or updates and demonstrate the resulting audit rows.

Avoid duplicate stock adjustments or duplicate audit records caused by competing implementations.

### F. Transactions
Demonstrate:
- `START TRANSACTION`
- `COMMIT`
- `ROLLBACK`

Test a successful sale and a deliberately failed sale. Confirm that a failed transaction leaves no partial invoice or unintended stock change.

### G. Indexes and constraints
Create appropriate indexes for frequently queried names, dates, expiry information and foreign-key relationships.

Use `EXPLAIN` on selected queries. Explain the intended benefits, and do not claim a performance improvement without measuring it.

Demonstrate constraint violations such as an invalid foreign key, duplicate unique value and insufficient stock.

### H. Database security
Use a dedicated application database user with limited privileges. Keep administrative database credentials out of the application.

Protect credentials with environment variables. Use parameterized queries, password hashing, request validation and suitable access control.

## 7. Backend API

Build a working REST API with consistent response formats and appropriate HTTP status codes.

Provide endpoints similar to:

```text
POST   /api/auth/login
GET    /api/dashboard
GET    /api/categories
GET    /api/medicines
POST   /api/medicines
PUT    /api/medicines/:id
DELETE /api/medicines/:id
GET    /api/batches
GET    /api/suppliers
POST   /api/purchases
GET    /api/purchases
POST   /api/sales
GET    /api/sales
GET    /api/sales/:id
POST   /api/returns
GET    /api/prescriptions
GET    /api/reports/low-stock
GET    /api/reports/expiring-batches
GET    /api/reports/monthly-sales
GET    /api/audit
```

Add all other endpoints required for the actual UI and Android screens.

Use backend validation and authentication. Handle database errors safely and never expose raw secrets, password hashes or unnecessary internal stack traces in responses.

Document every endpoint, its method, input schema, output and likely errors. Provide an OpenAPI specification if practical.

Create a `.env.example` without real credentials. Keep the actual `.env` file out of version control.

## 8. Native Android Studio application

Create a genuine, independently buildable Android project in:

`android/PharmaCareAndroid/`

Use Kotlin and Jetpack Compose with Material 3.

The application should visually complement the existing PharmaCare web UI while being designed for a phone. Do not simply shrink the desktop website onto a mobile screen.

### Android screens

Implement:
1. Login
2. Dashboard
3. Medicine search and inventory
4. Batch details and expiry information
5. Low-stock and near-expiry alerts
6. Sales and billing
7. Sales history and invoice details
8. Purchase entry for authorized employees
9. Supplier list and details
10. Customer lookup
11. Sales returns, where permitted
12. Reports
13. Profile and logout

Use role-based navigation and permissions.

### Android architecture
- Jetpack Compose UI
- ViewModels for screen state
- Repositories for data access
- Retrofit and OkHttp for network communication
- Kotlin serialization or another single, consistent JSON converter
- Coroutines and StateFlow for asynchronous work
- Navigation Compose for screen transitions
- Material 3 theme and reusable components

Include loading states, empty states, validation messages, retry handling and network error messages.

Never perform network requests on the UI thread. Do not hard-code real user credentials or production server addresses.

Configure the Android internet permission. For local development, make the API base URL configurable and document the emulator address `http://10.0.2.2:3000/api`. If temporary HTTP access is necessary for local testing, keep that exception limited to a debug configuration; production builds must use HTTPS.

Ensure the app uses the shared backend. A purchase entered on the website must be reflected in the Android inventory after refreshing, and a sale created from Android must update the website's database-backed figures.

### Android testing and delivery
- Build and sync the Gradle project.
- Run on an Android emulator or physical device if available.
- Test login, inventory retrieval, error states and at least one transaction flow.
- Fix compilation errors and broken navigation.
- Provide the generated APK path and the exact build command.
- Explain how to open the project in Android Studio and install the APK on a device.

If the emulator or SDK is unavailable in the environment, still create a valid Android Studio project and provide the precise steps needed to complete the device test. Clearly identify any test that could not be executed.

## 9. Sample database and reports

Create fictional seed data containing:
- At least five medicine categories
- At least 30 medicines
- At least 10 suppliers
- Several batches per medicine where appropriate
- Normal, low-stock, expired and near-expiry inventory examples
- Multiple purchases and sales
- Customers, doctors, prescriptions and employee roles

Generate enough coherent transaction data for sales totals, stock reports, joins, triggers and stored procedures to be demonstrated.

Ensure sample transactions and quantities are internally consistent. Do not use real customer credentials or present fictional records as live real-world information.

## 10. Security and reliability

- Validate requests on the backend.
- Use parameterized SQL queries.
- Hash passwords with bcrypt.
- Enforce access permissions on every protected endpoint.
- Use environment variables for secrets.
- Keep administrative MySQL credentials outside the application.
- Avoid logging passwords or authentication tokens.
- Handle missing records, duplicate values, insufficient stock and expired batches appropriately.
- Protect transaction integrity under concurrent stock changes.
- Use suitable CORS restrictions and API error handling.
- Keep generated dependency versions compatible with the installed toolchains.
- Never run destructive database commands against a database that has not been explicitly identified as a disposable development or test database.

## 11. Antigravity instructions, rules and skills

Create `AGENTS.md` at the project root with persistent rules that require:
- Preservation of the original UI
- Consistent API and database naming
- No fake live data
- No frontend-to-MySQL direct access
- Proper transactional stock handling
- Native Kotlin Android development
- Tests before declaring features complete
- Beginner-friendly explanations
- Explicit reporting of errors and incomplete work

Create project-specific skills in `.agents/skills/`:
- `pharmacy-dbms/SKILL.md`
- `pharmacy-web-ui/SKILL.md`
- `android-compose/SKILL.md`
- `project-testing/SKILL.md`

Each skill must have valid YAML frontmatter with a name and description, followed by clear instructions. Keep the skills focused on their respective tasks.

Inspect the official or installed Android and web guidance integrations, and use them if available. Do not assume that an integration is installed if it cannot be found.

Do not automatically trust random community plugins or execute privileged installation commands without permission. Prefer a small, reliable toolchain.

## 12. Mandatory automated and manual testing

Create a testing checklist and executable tests where practical.

Test:
- Login with valid credentials
- Login with invalid credentials
- Role-restricted operations
- Medicine create, read, update and eligible delete
- Search and filtering
- Purchase creation and stock increase
- Successful sale and stock decrease
- Insufficient stock rejection
- Expired-batch rejection
- Failed multi-item sale rollback
- Inventory audit trigger
- Low-stock and expiry views
- Stored function results
- Monthly sales aggregation
- Foreign-key and uniqueness constraints
- Website loading and network errors
- Android API connectivity and navigation

Use a separate test database for destructive test cases. Do not claim a test passed unless it was actually executed and the result was checked.

## 13. Documentation and faculty demonstration

Generate the following deliverables:

**Database**
- Complete SQL setup scripts
- Schema documentation
- ER diagram
- Normalization explanation
- Sample SQL queries
- Stored procedure, function, trigger, transaction and index demonstrations

**Website and backend**
- Complete source code
- Installation and configuration instructions
- API documentation
- Database connection instructions
- Test results

**Android**
- Complete Android Studio project
- Build and run instructions
- Emulator and physical-device networking instructions
- APK generation instructions

**Faculty review**
- A mapping table linking each taught DBMS concept to its implementation
- A live demonstration sequence
- Expected results for successful and failed transactions
- Important SQL commands to execute during the demonstration
- DBMS viva questions with beginner-friendly answers
- A list of known limitations and future enhancements

## 14. Implementation order

Work in these phases:

**Phase 1 — Inspect and design**
Inspect all project files, preserve the source UI, confirm the toolchain and document the target architecture and ER diagram.

**Phase 2 — Database**
Implement the schema, constraints, seed data, views, functions, triggers, procedures and demonstration queries. Test the SQL against the selected MySQL version.

**Phase 3 — Backend**
Create the Express API, database connection, authentication, validation, transaction handling and API tests.

**Phase 4 — Web integration**
Connect the existing website to the API and verify the real CRUD operations, purchases, billing and reports. Preserve the design.

**Phase 5 — Android**
Create the native Kotlin application, connect it to the same API and test its main user flows.

**Phase 6 — Final verification**
Run the available builds and tests. Fix errors, document missing prerequisites, verify the setup instructions and prepare the final faculty demonstration guide.

Keep all filenames, schemas, models, endpoint names and API response formats consistent.

At the end of every phase, provide a short summary of files created or changed, commands executed, successful tests, remaining errors and the next phase.

Do not stop after producing a plan, a dashboard mockup or a partial code sample. Continue through implementation as far as the available tools and environment permit, and honestly report any task that remains incomplete.

**Final objective:** Deliver one Pharmacy Management System with a preserved and functional web UI, a real Express backend, a MySQL database demonstrating DBMS concepts, and a separate native Android Studio app that shares the same backend and database.