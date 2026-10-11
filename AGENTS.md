# PharmaCare Agent Rules and Guidelines

These persistent rules govern all development, architectural decisions, and code modifications for the PharmaCare project.

## 1. UI Preservation & Visual Identity
- The reference HTML UI located in `source-ui/PharmaCare – Pharmacy Management.html` defines the layout, color palette (pine, mint, mute, ink, ok, warn, bad), typography (IBM Plex Sans, Bricolage Grotesque), and user workflows.
- Web UI evolutions must preserve this design and branding.
- Never replace the website with an unrelated generic bootstrap/dashboard template.
- Improve usability and accessibility incrementally while maintaining familiar navigation.

## 2. Relational Integrity & Single Source of Truth
- MySQL is the single authoritative data store for all platforms (Web and Android).
- No frontend (Web or Android) may connect directly to MySQL; all interactions must pass through the Express REST API.
- Do NOT use in-memory mock arrays, local storage as production data, Firebase, Supabase, or SQLite.
- Keep seed fixtures in the database, never fake live statistics.

## 3. Transactional Consistency & Business Rules
- Inventory deductions for sales and inventory additions for purchases MUST be handled atomically within database transactions (`START TRANSACTION`, `COMMIT`, `ROLLBACK`).
- Stored procedures (`sp_process_sale`, `sp_record_purchase`, etc.) enforce ACID compliance and row-level locking (`SELECT ... FOR UPDATE`).
- If any line item in a multi-item invoice cannot be fulfilled due to insufficient stock or batch expiry, the entire invoice transaction MUST be rolled back.
- Stock quantities must never drop below 0 (enforced by SQL `CHECK (quantity >= 0)` constraint).
- Expired medicine batches must never be dispensed or sold.
- Batch stock modifications must be audited via triggers (`trg_after_batch_stock_update`) into the `inventory_audit` table.

## 4. API & Database Naming Conventions
- Database tables: snake_case (`medicine`, `batch`, `sale`, `sale_item`, `purchase`, `inventory_audit`).
- Database columns: snake_case (`medicine_id`, `batch_number`, `expiry_date`, `selling_price`).
- JSON API fields: camelCase (`medicineId`, `batchNumber`, `expiryDate`, `sellingPrice`) or documented consistent snake_case.
- Consistent HTTP status codes: `200 OK`, `201 Created`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `409 Conflict`, `500 Internal Server Error`.

## 5. Security & Reliability
- Enforce Role-Based Access Control (RBAC) on the backend for all protected routes (Admin, Pharmacist, Cashier, Inventory Manager).
- Never trust frontend validation alone; validate all inputs on the backend.
- Passwords must be hashed using bcrypt with salt rounds >= 10.
- All SQL queries in Express must use parameterized prepared statements to prevent SQL injection.
- Keep application secrets in environment variables (`.env`), documented in `.env.example`.
- Dedicated application MySQL user (`pharmacare_user`) with least-privilege permissions; keep administrative root credentials out of the application.

## 6. Native Android Development
- The mobile app must be built as a genuine native Android application in Kotlin using Jetpack Compose, Material 3, ViewModel, StateFlow, Coroutines, and Retrofit.
- Do NOT use WebView wrappers, Flutter, or React Native.
- The Android project in `android/PharmaCareAndroid` must build and sync cleanly in Android Studio.
- Emulator endpoint: `http://10.0.2.2:3000/api` for local Express API.

## 7. Testing & Verification Standards
- Test all schemas, views, stored functions, stored procedures, and triggers directly against the live MySQL instance before declaring completion.
- Explicitly execute and report test results; never claim a test passed without running it.
- Destructive tests must run against dedicated test environments or explicit rollback transactions.

## 8. Mentorship & Academic Integrity
- Code comments, SQL scripts, and documentation must explain DBMS concepts clearly for second-year engineering students and viva presentations.
- Clearly report any errors, environmental constraints, or incomplete work.
