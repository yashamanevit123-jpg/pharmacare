# PharmaCare Native Android Application

A modern, genuine native Android mobile client built for the **PharmaCare Pharmacy Management System** using **Kotlin** and **Jetpack Compose (Material 3)**.

---

## 🌟 Architecture & Tech Stack

- **UI Layer**: 100% Jetpack Compose with Material 3 theming (Pine green `#12372F`, Mint `#DCEBE3`, semantic status badges).
- **Navigation**: Jetpack Navigation Compose with typed routes and bottom navigation bar.
- **State Management**: Android Architecture Components `ViewModel` exposing immutable `StateFlow<UiState>`.
- **Networking**: Retrofit 2 + OkHttp 4 with `AuthInterceptor` for JWT Bearer token authentication.
- **Serialization**: `kotlinx.serialization` with snake_case and camelCase compatibility.
- **Backend & Database**: Communicates directly with the Express REST API (`http://10.0.2.2:3000/api/`), which connects to the MySQL 8.0 `pharmacare_db` relational database.

---

## 📱 Modules & Screens

| Screen | Route | Key Features & DBMS Integration |
|---|---|---|
| **Login** | `login` | JWT auth with role detection, quick 1-click RBAC fill, and configurable server endpoint. |
| **Dashboard** | `dashboard` | Real-time telemetry (`vw_low_stock`, `vw_expiring_batches`, `fn_inventory_value()`). |
| **Inventory** | `inventory` | Live search, category chips, status filters, and FEFO batch inspection dialog. |
| **POS Billing** | `billing` | Customer selection, FEFO batch picker, cart, and atomic checkout via `sp_process_sale`. |
| **Purchases** | `purchases` | Supplier selection, PO history, and stock reception intake via `sp_record_purchase`. |
| **Prescriptions**| `prescriptions`| Doctor prescriptions with clinical diagnosis notes, patient links, and prescribed items. |
| **Reports** | `reports` | Financial performance aggregations and monthly revenue breakdown via `vw_monthly_sales`. |

---

## 🚀 How to Run in Android Studio

### 1. Prerequisites
- **Android Studio** (Koala, Ladybug, Jellyfish, or newer)
- **JDK 17** (bundled with Android Studio)
- Express API server running locally on port 3000 (`npm start` or `node src/server.js` in `backend/`)
- MySQL 8.0 running with `pharmacare_db` populated

### 2. Opening the Project
1. Open Android Studio.
2. Select **File > Open...** and navigate to:
   ```
   <WORKSPACE_ROOT>/android/PharmaCareAndroid
   ```
3. Allow Gradle to sync dependencies.

### 3. Running on Android Emulator
1. Launch any Android Virtual Device (AVD) running Android 8.0+ (API 26 to 35).
2. The app is preconfigured to use `http://10.0.2.2:3000/api/` which automatically routes to your host machine's port 3000.
3. Click **Run** (`Shift + F10`).

### 4. Running on a Physical Android Device
1. Connect your Android phone and development PC to the same Wi-Fi network.
2. Find your PC's local Wi-Fi IPv4 address (e.g. `ipconfig` -> `192.168.1.5`).
3. On the Login screen in the app, tap **Configure Server Endpoint**.
4. Enter:
   ```
   http://192.168.1.5:3000/api/
   ```
5. Tap **Sign In**.

---

## 👥 Demo User Accounts (RBAC)

Use the quick-fill buttons on the Login screen or the Role Switcher icon in the top app bar:

| Role | Username | Password | Privileges |
|---|---|---|---|
| **Admin** | `admin` | `password123` | Full access across all modules and audit trail |
| **Pharmacist** | `rajesh` | `password123` | POS sales, batch dispensing, prescription verification |
| **Cashier** | `neha` | `password123` | Customer billing, invoice receipt printing |
| **Inventory Manager** | `vikram` | `password123` | Purchase orders, supplier catalog, batch stock intake |
