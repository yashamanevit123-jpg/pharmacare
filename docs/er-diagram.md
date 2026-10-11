# PharmaCare Entity-Relationship (ER) Model

This document specifies the relational model, cardinalities, primary keys, foreign keys, and structural integrity constraints of the **PharmaCare** database.

## 1. Mermaid Entity-Relationship Diagram

```mermaid
erDiagram
    ROLE ||--o{ EMPLOYEE : "assigned to"
    CATEGORY ||--o{ MEDICINE : "categorizes"
    MANUFACTURER ||--o{ MEDICINE : "manufactures"
    SUPPLIER ||--o{ BATCH : "supplies"
    SUPPLIER ||--o{ PURCHASE : "receives order"
    
    MEDICINE ||--o{ BATCH : "has lots of"
    MEDICINE ||--o{ PURCHASE_ITEM : "ordered in"
    MEDICINE ||--o{ SALE_ITEM : "billed in"
    MEDICINE ||--o{ PRESCRIPTION_ITEM : "prescribed in"
    
    BATCH ||--o{ SALE_ITEM : "dispensed from"
    BATCH ||--o{ RETURN_ITEM : "restocked to"
    BATCH ||--o{ INVENTORY_AUDIT : "tracked in"
    
    EMPLOYEE ||--o{ SALE : "processes"
    EMPLOYEE ||--o{ PURCHASE : "creates"
    EMPLOYEE ||--o{ SALES_RETURN : "approves"
    
    CUSTOMER ||--o{ SALE : "purchases"
    CUSTOMER ||--o{ PRESCRIPTION : "belongs to"
    CUSTOMER ||--o{ SALES_RETURN : "initiates"
    
    DOCTOR ||--o{ PRESCRIPTION : "issues"
    PRESCRIPTION ||--|{ PRESCRIPTION_ITEM : "contains"
    
    PURCHASE ||--|{ PURCHASE_ITEM : "contains"
    SALE ||--|{ SALE_ITEM : "contains"
    
    SALE ||--o{ SALES_RETURN : "referenced in"
    SALES_RETURN ||--|{ RETURN_ITEM : "contains"
    SALE_ITEM ||--o{ RETURN_ITEM : "returned from"

    ROLE {
        int role_id PK
        varchar role_name UK
        varchar description
    }

    EMPLOYEE {
        int employee_id PK
        int role_id FK
        varchar full_name
        varchar email UK
        varchar phone
        varchar username UK
        varchar password_hash
        boolean is_active
        datetime created_at
    }

    CATEGORY {
        int category_id PK
        varchar name UK
        varchar description
    }

    MANUFACTURER {
        int manufacturer_id PK
        varchar name UK
        varchar contact_email
        varchar phone
    }

    SUPPLIER {
        int supplier_id PK
        varchar name
        varchar contact_person
        varchar phone
        varchar email
        varchar address
        varchar gst_number UK
    }

    MEDICINE {
        int medicine_id PK
        int category_id FK
        int manufacturer_id FK
        varchar name
        varchar generic_name
        varchar dosage_form
        varchar unit_pack
        int reorder_level
        varchar rack_location
        boolean is_prescription_required
        boolean is_active
        datetime created_at
    }

    BATCH {
        int batch_id PK
        int medicine_id FK
        int supplier_id FK
        varchar batch_number UK
        date manufacturing_date
        date expiry_date
        decimal purchase_price
        decimal selling_price
        int quantity
        varchar status
        datetime created_at
    }

    CUSTOMER {
        int customer_id PK
        varchar name
        varchar phone UK
        varchar email
        varchar address
        int age
        varchar gender
        datetime created_at
    }

    DOCTOR {
        int doctor_id PK
        varchar name
        varchar specialization
        varchar hospital_clinic
        varchar phone
        varchar email
    }

    PRESCRIPTION {
        int prescription_id PK
        int customer_id FK
        int doctor_id FK
        date prescription_date
        text diagnosis_notes
        datetime created_at
    }

    PRESCRIPTION_ITEM {
        int prescription_item_id PK
        int prescription_id FK
        int medicine_id FK
        varchar dosage_instruction
        varchar duration
        int quantity_prescribed
    }

    PURCHASE {
        int purchase_id PK
        int supplier_id FK
        int employee_id FK
        varchar invoice_number UK
        date purchase_date
        decimal total_amount
        varchar payment_status
        text notes
        datetime created_at
    }

    PURCHASE_ITEM {
        int purchase_item_id PK
        int purchase_id FK
        int medicine_id FK
        varchar batch_number
        date expiry_date
        int quantity
        decimal unit_cost
        decimal total_cost
    }

    SALE {
        int sale_id PK
        varchar invoice_number UK
        int customer_id FK
        int employee_id FK
        datetime sale_date
        varchar payment_method
        decimal subtotal
        decimal tax_amount
        decimal discount_amount
        decimal total_amount
        varchar status
        datetime created_at
    }

    SALE_ITEM {
        int sale_item_id PK
        int sale_id FK
        int batch_id FK
        int medicine_id FK
        int quantity
        decimal unit_price
        decimal subtotal
    }

    SALES_RETURN {
        int return_id PK
        int sale_id FK
        int customer_id FK
        int employee_id FK
        datetime return_date
        decimal refund_amount
        varchar reason
        varchar status
        datetime created_at
    }

    RETURN_ITEM {
        int return_item_id PK
        int return_id FK
        int sale_item_id FK
        int batch_id FK
        int quantity
        decimal refund_unit_price
        decimal subtotal
        boolean restocked
    }

    INVENTORY_AUDIT {
        bigint audit_id PK
        int batch_id FK
        int medicine_id FK
        varchar change_type
        int old_quantity
        int new_quantity
        int quantity_change
        varchar changed_by
        datetime changed_at
        text notes
    }
```

---

## 2. Cardinalities and Relational Semantics

| Parent Entity | Child Entity | Cardinality | Business Meaning |
| :--- | :--- | :--- | :--- |
| `CATEGORY` | `MEDICINE` | 1 to Many (1:N) | A category groups multiple medicines; every medicine belongs to exactly one category. |
| `MANUFACTURER` | `MEDICINE` | 1 to Many (1:N) | A pharmaceutical manufacturer produces multiple medicines. |
| `MEDICINE` | `BATCH` | 1 to Many (1:N) | A medicine has multiple production batches with distinct expiration dates. |
| `SUPPLIER` | `BATCH` | 1 to Many (1:N) | A supplier delivers batches of medicines. |
| `SUPPLIER` | `PURCHASE` | 1 to Many (1:N) | A supplier receives multiple purchase order invoices. |
| `PURCHASE` | `PURCHASE_ITEM` | 1 to Many (1:N) | A purchase order invoice contains one or more line items. |
| `CUSTOMER` | `SALE` | 1 to Many (1:N) | A customer may make zero, one, or multiple purchases over time. |
| `EMPLOYEE` | `SALE` | 1 to Many (1:N) | A cashier/pharmacist processes multiple sales bills. |
| `SALE` | `SALE_ITEM` | 1 to Many (1:N) | An invoice bill consists of one or more purchased batch items. |
| `BATCH` | `SALE_ITEM` | 1 to Many (1:N) | Units from a specific batch can be sold across multiple customer sales. |
| `DOCTOR` | `PRESCRIPTION` | 1 to Many (1:N) | A doctor writes prescriptions for patients. |
| `CUSTOMER` | `PRESCRIPTION` | 1 to Many (1:N) | A patient/customer can have multiple prescription records. |
| `PRESCRIPTION` | `PRESCRIPTION_ITEM`| 1 to Many (1:N) | A prescription includes multiple prescribed medicines with dosages. |
| `SALE` | `SALES_RETURN` | 1 to Many (1:N) | An invoice may have an associated return record for damaged/refunded items. |
| `SALES_RETURN` | `RETURN_ITEM` | 1 to Many (1:N) | A return transaction details individual items and quantities returned. |
| `BATCH` | `INVENTORY_AUDIT` | 1 to Many (1:N) | Every stock alteration on a batch produces an immutable audit record via triggers. |

---

## 3. Key Constraints & Data Integrity

1. **Primary Keys**: Every table uses a surrogate integer auto-increment key (`INT AUTO_INCREMENT PRIMARY KEY`) or composite business unique constraint.
2. **Foreign Key Integrity**:
   - `ON DELETE RESTRICT` on master tables (`medicine`, `customer`, `employee`, `supplier`) to prevent accidental deletion of referenced entities.
   - `ON DELETE CASCADE` for line items (`purchase_item`, `sale_item`, `prescription_item`, `return_item`) linked strictly to their parent transaction header if headers are purged during cleanups.
3. **Check Constraints**:
   - `batch.quantity >= 0`: Ensures stock never becomes negative under any circumstance.
   - `batch.purchase_price >= 0` and `batch.selling_price >= 0`: Prevents invalid prices.
   - `purchase_item.quantity > 0` and `sale_item.quantity > 0`: Disallows zero or negative quantity line items.
4. **Unique Constraints**:
   - `UNIQUE (medicine_id, batch_number)`: Enforces batch identity per medicine.
   - `UNIQUE (sale_id, batch_id)`: Prevents duplicate line items for the same batch on a single invoice.
   - `employee.username`, `employee.email`, `customer.phone`: Enforce uniqueness.
