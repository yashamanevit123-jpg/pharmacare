-- ============================================================================
-- PharmaCare Pharmacy Management System
-- Seed Data Script (02_seed.sql)
-- Populates 30+ medicines, 7 categories, 8 manufacturers, 10 suppliers,
-- multiple batches (normal, low-stock, expired, near-expiry),
-- employees, customers, doctors, purchases, sales, returns, and prescriptions.
-- ============================================================================

USE pharmacare_db;

-- ----------------------------------------------------------------------------
-- 1. ROLES
-- ----------------------------------------------------------------------------
INSERT INTO role (role_id, role_name, description) VALUES
(1, 'Administrator', 'Full system access, reports, user and configuration management'),
(2, 'Pharmacist', 'Prescription validation, medicine and batch catalog management, billing'),
(3, 'Cashier', 'Point of sale billing, receipt generation, customer intake'),
(4, 'Inventory Manager', 'Procurement, supplier invoices, batch tracking, stock auditing');

-- ----------------------------------------------------------------------------
-- 2. CATEGORIES (7 Categories)
-- ----------------------------------------------------------------------------
INSERT INTO category (category_id, name, description) VALUES
(1, 'Tablet', 'Solid compressed oral dosage form'),
(2, 'Syrup', 'Liquid oral formulation typically containing dissolved medications'),
(3, 'Capsule', 'Gelatin or vegetarian shell enclosing active pharmaceutical ingredients'),
(4, 'Injection', 'Sterile injectable solution or suspension for parenteral administration'),
(5, 'Ointment', 'Semi-solid preparation for topical dermal application'),
(6, 'Drops', 'Liquid dosage form for ophthalmic or otic administration'),
(7, 'Inhaler', 'Aerosolized respiratory medications for inhalation');

-- ----------------------------------------------------------------------------
-- 3. MANUFACTURERS (8 Pharmaceutical Companies)
-- ----------------------------------------------------------------------------
INSERT INTO manufacturer (manufacturer_id, name, contact_email, phone, country) VALUES
(1, 'Sun Pharma', 'contact@sunpharma.example.com', '+91-22-43244324', 'India'),
(2, 'Cipla', 'info@cipla.example.com', '+91-22-24826000', 'India'),
(3, "Dr. Reddy's Lab", 'support@drreddys.example.com', '+91-40-49002900', 'India'),
(4, 'Lupin Ltd', 'corporate@lupin.example.com', '+91-22-66402222', 'India'),
(5, 'Mankind Pharma', 'help@mankind.example.com', '+91-11-46846700', 'India'),
(6, 'Abbott India', 'contact@abbott.example.com', '+91-22-38162000', 'India'),
(7, 'Glenmark Pharmaceuticals', 'info@glenmark.example.com', '+91-22-40189999', 'India'),
(8, 'Torrent Pharma', 'corporate@torrentpharma.example.com', '+91-79-26599000', 'India');

-- ----------------------------------------------------------------------------
-- 4. SUPPLIERS (10 Wholesale Distributors)
-- ----------------------------------------------------------------------------
INSERT INTO supplier (supplier_id, name, contact_person, phone, email, address, gst_number) VALUES
(1, 'Mumbai Med Distributors', 'Kailash Mehta', '9820011223', 'kailash@mumbaimed.example.com', 'Plot 44, MIDC Andheri East, Mumbai', '27AABCU9603R1ZM'),
(2, 'Delhi Pharma Traders', 'Manish Bansal', '9811022334', 'manish@delhipharma.example.com', 'Shop 12, Bhagirath Palace, Chandni Chowk, Delhi', '07AABCT4321Q1ZN'),
(3, 'Hyderabad Health Suppliers', 'Venkatesh Rao', '9848033445', 'venkat@hydhealth.example.com', 'Koti Medical Market, Hyderabad', '36AABCY7890P1ZP'),
(4, 'Bangalore Lifesciences Wholesalers', 'Anand Kulkarni', '9880044556', 'anand@blrblife.example.com', 'Peenya Industrial Area, Bengaluru', '29AABCZ1234M1ZR'),
(5, 'Pune Pharma Hub', 'Sanjay Joshi', '9822055667', 'sanjay@punepharma.example.com', 'Rasta Peth, Pune', '27AABCJ5678L1ZS'),
(6, 'Gujarat Healthcare Supplies', 'Hitesh Shah', '9825066778', 'hitesh@gujhealth.example.com', 'GIDC Vatva, Ahmedabad', '24AABCG9012K1ZT'),
(7, 'Chennai Remedies Distributors', 'Murugan Swamy', '9841077889', 'murugan@chennairemedies.example.com', 'Broadway, George Town, Chennai', '33AABCR3456J1ZU'),
(8, 'Kolkata Medical Stores Wholesale', 'Debashis Roy', '9830088990', 'debashis@kolkatamed.example.com', 'College Street Medical Complex, Kolkata', '19AABCK7890H1ZV'),
(9, 'Jaipur Drug House', 'Mahesh Agarwal', '9829099001', 'mahesh@jaipurdrug.example.com', 'Johari Bazar, Jaipur', '08AABCJ2345G1ZW'),
(10, 'Lucknow Pharma Logistics', 'Alok Srivastava', '9839010112', 'alok@lucknowpharma.example.com', 'Aminabad, Lucknow', '09AABCL6789F1ZX');

-- ----------------------------------------------------------------------------
-- 5. CUSTOMERS (12 Customers including reference HTML names)
-- ----------------------------------------------------------------------------
INSERT INTO customer (customer_id, name, phone, email, address, age, gender) VALUES
(1, 'Aarav Sharma', '9876543210', 'aarav.sharma@example.com', 'Andheri West, Mumbai', 32, 'Male'),
(2, 'Priya Patel', '9876543211', 'priya.patel@example.com', 'Navrangpura, Ahmedabad', 28, 'Female'),
(3, 'Rohit Verma', '9876543212', 'rohit.verma@example.com', 'Rohini Sector 9, Delhi', 45, 'Male'),
(4, 'Anjali Singh', '9876543213', 'anjali.singh@example.com', 'Gomti Nagar, Lucknow', 35, 'Female'),
(5, 'Vikram Reddy', '9876543214', 'vikram.reddy@example.com', 'Banjara Hills, Hyderabad', 40, 'Male'),
(6, 'Sneha Iyer', '9876543215', 'sneha.iyer@example.com', 'T Nagar, Chennai', 26, 'Female'),
(7, 'Amit Deshmukh', '9876543216', 'amit.deshmukh@example.com', 'Kothrud, Pune', 50, 'Male'),
(8, 'Pooja Mehta', '9876543217', 'pooja.mehta@example.com', 'Malviya Nagar, Jaipur', 29, 'Female'),
(9, 'Suresh Kumar', '9876543218', 'suresh.kumar@example.com', 'Salt Lake, Kolkata', 60, 'Male'),
(10, 'Kavita Joshi', '9876543219', 'kavita.joshi@example.com', 'Dharampeth, Nagpur', 33, 'Female'),
(11, 'Rajesh Nair', '9876543220', 'rajesh.nair@example.com', 'Panampilly Nagar, Kochi', 42, 'Male'),
(12, 'Sunita Menon', '9876543221', 'sunita.menon@example.com', 'Indiranagar, Bengaluru', 38, 'Female');

-- ----------------------------------------------------------------------------
-- 6. EMPLOYEES (Password for all seed users is 'password123' bcrypt hashed)
-- Hash: $2a$10$dukiS2Gu1GPIYggsbUir8Oh7ubtzPD8TE7nv1yHse588PK/yrdIri
-- ----------------------------------------------------------------------------
INSERT INTO employee (employee_id, role_id, full_name, email, phone, username, password_hash, is_active) VALUES
(1, 1, 'System Administrator', 'admin@pharmacare.example.com', '9800000001', 'admin', '$2a$10$dukiS2Gu1GPIYggsbUir8Oh7ubtzPD8TE7nv1yHse588PK/yrdIri', TRUE),
(2, 2, 'Rajesh Gupta', 'rajesh.gupta@pharmacare.example.com', '9800000002', 'rajesh', '$2a$10$dukiS2Gu1GPIYggsbUir8Oh7ubtzPD8TE7nv1yHse588PK/yrdIri', TRUE),
(3, 3, 'Neha Bansal', 'neha.bansal@pharmacare.example.com', '9800000003', 'neha', '$2a$10$dukiS2Gu1GPIYggsbUir8Oh7ubtzPD8TE7nv1yHse588PK/yrdIri', TRUE),
(4, 4, 'Vikram Malhotra', 'vikram.m@pharmacare.example.com', '9800000004', 'vikram', '$2a$10$dukiS2Gu1GPIYggsbUir8Oh7ubtzPD8TE7nv1yHse588PK/yrdIri', TRUE);

-- ----------------------------------------------------------------------------
-- 7. DOCTORS (5 Doctors including reference HTML entries)
-- ----------------------------------------------------------------------------
INSERT INTO doctor (doctor_id, name, specialization, hospital_clinic, phone, email) VALUES
(1, 'Dr. Sanjay Agarwal', 'General Physician', 'Apollo Hospital, Delhi', '9811100001', 'dr.agarwal@example.com'),
(2, 'Dr. Sunita Rao', 'Cardiologist', 'Fortis Hospital, Mumbai', '9822200002', 'dr.rao@example.com'),
(3, 'Dr. Arvind Das', 'Pulmonologist', 'AIIMS, Delhi', '9833300003', 'dr.das@example.com'),
(4, 'Dr. Meera Kulkarni', 'Endocrinologist', 'Manipal Hospital, Bangalore', '9844400004', 'dr.kulkarni@example.com'),
(5, 'Dr. Rajesh Nambiar', 'Orthopedic Surgeon', 'Apollo Hospital, Chennai', '9855500005', 'dr.nambiar@example.com');

-- ----------------------------------------------------------------------------
-- 8. MEDICINES (32 Master Catalog Medicines)
-- ----------------------------------------------------------------------------
INSERT INTO medicine (medicine_id, category_id, manufacturer_id, name, generic_name, dosage_form, unit_pack, reorder_level, rack_location, is_prescription_required) VALUES
(1, 1, 1, 'Dolo 650mg', 'Paracetamol 650mg', 'Tablet', '15 Tablets', 20, 'R1-A1', FALSE),
(2, 1, 2, 'Crocin Advance 500mg', 'Paracetamol 500mg', 'Tablet', '15 Tablets', 20, 'R1-A2', FALSE),
(3, 1, 3, 'Azithral 500mg', 'Azithromycin 500mg', 'Tablet', '5 Tablets', 15, 'R1-B1', TRUE),
(4, 1, 4, 'Augmentin 625 Duo', 'Amoxicillin + Clavulanic Acid', 'Tablet', '10 Tablets', 15, 'R1-B2', TRUE),
(5, 3, 1, 'Pantocid 40mg', 'Pantoprazole 40mg', 'Capsule', '15 Capsules', 15, 'R2-A1', FALSE),
(6, 1, 4, 'Telma 40mg', 'Telmisartan 40mg', 'Tablet', '15 Tablets', 15, 'R2-A2', TRUE),
(7, 1, 5, 'Glycomet 500mg', 'Metformin 500mg', 'Tablet', '20 Tablets', 20, 'R2-B1', TRUE),
(8, 1, 1, 'Atorva 10mg', 'Atorvastatin 10mg', 'Tablet', '15 Tablets', 15, 'R2-B2', TRUE),
(9, 2, 2, 'Calpol Syrup 120mg', 'Paracetamol Pediatric Oral Susp.', 'Syrup', '60 ml', 10, 'R3-A1', FALSE),
(10, 2, 3, 'Benadryl Cough Syrup', 'Diphenhydramine + Ammonium Chloride', 'Syrup', '100 ml', 10, 'R3-A2', FALSE),
(11, 5, 5, 'Betadine Ointment 20g', 'Povidone Iodine 5% w/w', 'Ointment', '20 g Tube', 10, 'R4-A1', FALSE),
(12, 5, 4, 'Moov Pain Relief Ointment', 'Diclofenac + Methyl Salicylate', 'Ointment', '30 g Tube', 10, 'R4-A2', FALSE),
(13, 4, 1, 'Human Mixtard Insulin', 'Biphasic Isophane Insulin 100IU/ml', 'Injection', '10 ml Vial', 10, 'R5-A1', TRUE),
(14, 1, 2, 'Allegra 120mg', 'Fexofenadine Hydrochloride 120mg', 'Tablet', '10 Tablets', 15, 'R1-C1', FALSE),
(15, 2, 5, 'Digene Syrup 200ml', 'Magnesium Hydroxide + Simethicone', 'Syrup', '200 ml Bottle', 10, 'R3-B1', FALSE),
(16, 1, 1, 'Montair LC 10mg', 'Montelukast + Levocetirizine', 'Tablet', '10 Tablets', 15, 'R1-C2', TRUE),
(17, 1, 6, 'Shelcal 500mg', 'Calcium 500mg + Vitamin D3', 'Tablet', '15 Tablets', 20, 'R2-C1', FALSE),
(18, 3, 1, 'Pan-D Capsule', 'Pantoprazole + Domperidone', 'Capsule', '15 Capsules', 15, 'R2-C2', TRUE),
(19, 1, 7, 'Voveran SR 100mg', 'Diclofenac Sodium 100mg', 'Tablet', '10 Tablets', 10, 'R1-D1', TRUE),
(20, 1, 2, 'Azee 250mg', 'Azithromycin 250mg', 'Tablet', '6 Tablets', 10, 'R1-D2', TRUE),
(21, 2, 1, 'Montek-LC Kid Syrup', 'Montelukast + Levocetirizine 60ml', 'Syrup', '60 ml Bottle', 10, 'R3-B2', TRUE),
(22, 1, 6, 'Neurobion Forte', 'Vitamin B-Complex + B12', 'Tablet', '30 Tablets', 25, 'R2-D1', FALSE),
(23, 3, 6, 'Becosules Capsules', 'B-Complex with Vitamin C', 'Capsule', '20 Capsules', 20, 'R2-D2', FALSE),
(24, 1, 5, 'Paracetamol 500mg Generic', 'Paracetamol 500mg', 'Tablet', '10 Tablets', 20, 'R1-E1', FALSE),
(25, 3, 2, 'Amoxicillin 500mg', 'Amoxicillin Trihydrate 500mg', 'Capsule', '10 Capsules', 15, 'R2-E1', TRUE),
(26, 1, 4, 'Cetirizine 10mg', 'Cetirizine Hydrochloride 10mg', 'Tablet', '10 Tablets', 15, 'R1-E2', FALSE),
(27, 3, 3, 'Omeprazole 20mg', 'Omeprazole 20mg', 'Capsule', '15 Capsules', 15, 'R2-E2', FALSE),
(28, 1, 5, 'Metformin 1000mg', 'Metformin Hydrochloride 1000mg', 'Tablet', '10 Tablets', 15, 'R2-F1', TRUE),
(29, 1, 8, 'Losartan 50mg', 'Losartan Potassium 50mg', 'Tablet', '15 Tablets', 15, 'R2-F2', TRUE),
(30, 7, 2, 'Asthalin Inhaler 100mcg', 'Salbutamol 100mcg CFC-Free', 'Inhaler', '200 Metered Doses', 10, 'R5-B1', TRUE),
(31, 6, 2, 'Ciplox Eye Drops 10ml', 'Ciprofloxacin 0.3% w/v', 'Drops', '10 ml Bottle', 10, 'R4-B1', TRUE),
(32, 5, 1, 'Volini Gel 30g', 'Diclofenac Diethylamine + Linseed Oil', 'Ointment', '30 g Tube', 10, 'R4-B2', FALSE);

-- ----------------------------------------------------------------------------
-- 9. BATCHES (Multiple lots: Normal, Low Stock, Expired, Near-Expiry)
-- Note: Base dates are anchored relative to current production schedule.
-- ----------------------------------------------------------------------------
INSERT INTO batch (batch_id, medicine_id, supplier_id, batch_number, manufacturing_date, expiry_date, purchase_price, selling_price, quantity, status) VALUES
-- Normal Stock Batches (Healthy inventory)
(1, 1, 1, 'DL650A', '2025-08-01', '2027-08-15', 1.20, 2.00, 150, 'Available'),
(2, 2, 1, 'CR500B', '2025-05-01', '2027-05-20', 1.50, 2.50, 120, 'Available'),
(3, 3, 2, 'AZ500C', '2024-11-01', '2026-11-30', 12.00, 18.50, 45, 'Available'),
(4, 4, 2, 'AG625D', '2024-09-01', '2026-12-10', 18.00, 25.00, 60, 'Available'),
(5, 5, 1, 'PAN40E', '2025-01-01', '2027-01-15', 6.50, 10.00, 80, 'Available'),
(6, 6, 3, 'TEL40F', '2025-03-01', '2027-03-20', 8.00, 12.50, 70, 'Available'),
(7, 7, 3, 'GLY500G', '2024-12-01', '2026-12-25', 3.00, 5.00, 90, 'Available'),
(8, 8, 2, 'ATR10H', '2025-02-01', '2027-02-10', 7.00, 11.00, 55, 'Available'),
(9, 14, 2, 'ALL120N', '2025-04-01', '2027-04-12', 9.00, 14.00, 100, 'Available'),
(10, 15, 1, 'DIG200O', '2024-10-01', '2026-12-05', 75.00, 98.00, 25, 'Available'),
(11, 16, 4, 'MON10P', '2025-02-15', '2027-02-14', 11.50, 17.00, 85, 'Available'),
(12, 17, 6, 'SHL500Q', '2025-01-10', '2027-06-30', 7.20, 11.50, 110, 'Available'),
(13, 18, 5, 'PAND40R', '2025-03-20', '2027-05-15', 12.00, 19.00, 75, 'Available'),
(14, 22, 6, 'NEU100S', '2025-05-01', '2027-11-30', 2.80, 4.50, 200, 'Available'),
(15, 23, 6, 'BEC20T', '2025-04-10', '2027-09-30', 2.50, 4.00, 180, 'Available'),
(16, 30, 7, 'AST100U', '2025-06-01', '2027-05-31', 110.00, 148.00, 40, 'Available'),
(17, 31, 2, 'CIP10V', '2025-07-01', '2027-06-30', 14.00, 22.00, 65, 'Available'),
(18, 32, 5, 'VOL30W', '2025-03-15', '2027-02-28', 85.00, 125.00, 50, 'Available'),

-- Low Stock Batches (Under 10 quantity threshold)
(19, 9, 1, 'CAL120I', '2024-04-01', '2026-12-15', 35.00, 48.00, 4, 'Available'),
(20, 10, 2, 'BEN100J', '2024-05-01', '2026-11-20', 85.00, 115.00, 6, 'Available'),
(21, 13, 3, 'INS100M', '2024-09-01', '2026-11-15', 320.00, 410.00, 8, 'Available'),
(22, 19, 7, 'VOV100X', '2025-01-15', '2027-01-14', 6.00, 9.50, 5, 'Available'),
(23, 20, 2, 'AZE250Y', '2024-10-10', '2026-12-10', 8.50, 13.00, 7, 'Available'),

-- Near-Expiry Batches (Expiring within the next 90 days from Oct 2026)
(24, 11, 1, 'BET20K', '2024-11-01', '2026-11-10', 45.00, 65.00, 25, 'Available'),
(25, 12, 3, 'MOOV30L', '2024-12-01', '2026-11-25', 55.00, 75.00, 30, 'Available'),
(26, 21, 1, 'MNK60Z', '2025-01-05', '2026-12-01', 48.00, 68.00, 14, 'Available'),
(27, 24, 5, 'PAR500AA', '2025-01-01', '2026-11-18', 0.90, 1.80, 40, 'Available'),

-- Expired Batches (Dates strictly prior to CURRENT_DATE, status Expired)
(28, 1, 1, 'DL650-OLD', '2023-01-01', '2024-06-30', 1.00, 1.80, 12, 'Expired'),
(29, 9, 2, 'CAL120-OLD', '2023-03-01', '2024-09-15', 32.00, 44.00, 5, 'Expired'),
(30, 26, 4, 'CET10-OLD', '2023-05-10', '2025-05-09', 2.00, 3.50, 18, 'Expired'),

-- Second Batches for Popular Medicines (Demonstrating multiple batches per medicine)
(31, 1, 2, 'DL650B', '2026-01-10', '2028-01-09', 1.30, 2.00, 250, 'Available'),
(32, 2, 3, 'CR500C', '2026-02-01', '2028-01-31', 1.55, 2.50, 180, 'Available'),
(33, 5, 2, 'PAN40F', '2026-03-01', '2028-02-28', 6.70, 10.00, 120, 'Available');

-- ----------------------------------------------------------------------------
-- 10. PURCHASES (Header Records from Suppliers)
-- ----------------------------------------------------------------------------
INSERT INTO purchase (purchase_id, supplier_id, employee_id, invoice_number, purchase_date, total_amount, payment_status, notes) VALUES
(1, 1, 4, 'PUR-2026-001', '2026-09-01', 15000.00, 'Paid', 'Standard replenishment for pain and pediatric meds'),
(2, 2, 4, 'PUR-2026-002', '2026-09-15', 22000.00, 'Paid', 'Antibiotic and respiratory seasonal order'),
(3, 3, 4, 'PUR-2026-003', '2026-09-22', 18500.00, 'Paid', 'Hypertension, diabetes, and cold syrups'),
(4, 6, 4, 'PUR-2026-004', '2026-10-01', 34200.00, 'Paid', 'Nutritional and multivitamin bulk dispatch'),
(5, 7, 4, 'PUR-2026-005', '2026-10-05', 12800.00, 'Paid', 'Inhalers and dermatological supplies');

-- ----------------------------------------------------------------------------
-- 11. PURCHASE ITEMS (Line Items)
-- ----------------------------------------------------------------------------
INSERT INTO purchase_item (purchase_item_id, purchase_id, medicine_id, batch_number, expiry_date, quantity, unit_cost, total_cost) VALUES
(1, 1, 1, 'DL650A', '2027-08-15', 100, 1.20, 120.00),
(2, 1, 9, 'CAL120I', '2026-12-15', 20, 35.00, 700.00),
(3, 2, 3, 'AZ500C', '2026-11-30', 50, 12.00, 600.00),
(4, 2, 10, 'BEN100J', '2026-11-20', 30, 85.00, 2550.00),
(5, 3, 6, 'TEL40F', '2027-03-20', 40, 8.00, 320.00),
(6, 3, 7, 'GLY500G', '2026-12-25', 60, 3.00, 180.00),
(7, 4, 22, 'NEU100S', '2027-11-30', 200, 2.80, 560.00),
(8, 4, 23, 'BEC20T', '2027-09-30', 180, 2.50, 450.00),
(9, 5, 30, 'AST100U', '2027-05-31', 40, 110.00, 4400.00);

-- ----------------------------------------------------------------------------
-- 12. SALES (Historical Invoices matching and expanding reference HTML)
-- ----------------------------------------------------------------------------
INSERT INTO sale (sale_id, invoice_number, customer_id, employee_id, sale_date, payment_method, subtotal, tax_amount, discount_amount, total_amount, status) VALUES
(1, 'INV-20260929-0001', 1, 2, '2026-09-29 10:15:00', 'Cash', 32.00, 0.00, 0.00, 32.00, 'Completed'),
(2, 'INV-20260930-0002', 2, 3, '2026-09-30 11:30:00', 'UPI', 230.00, 0.00, 0.00, 230.00, 'Completed'),
(3, 'INV-20260930-0003', 5, 2, '2026-09-30 14:45:00', 'Card', 410.00, 0.00, 0.00, 410.00, 'Completed'),
(4, 'INV-20260930-0004', 3, 2, '2026-09-30 17:20:00', 'Cash', 37.50, 0.00, 0.00, 37.50, 'Completed'),
(5, 'INV-20261002-0005', 4, 3, '2026-10-02 09:10:00', 'UPI', 185.00, 0.00, 0.00, 185.00, 'Completed'),
(6, 'INV-20261005-0006', 7, 3, '2026-10-05 16:05:00', 'Cash', 150.00, 0.00, 0.00, 150.00, 'Completed');

-- ----------------------------------------------------------------------------
-- 13. SALE ITEMS (Line Items linked to Batches)
-- ----------------------------------------------------------------------------
INSERT INTO sale_item (sale_item_id, sale_id, batch_id, medicine_id, quantity, unit_price, subtotal) VALUES
(1, 1, 1, 1, 6, 2.00, 12.00),
(2, 1, 5, 5, 2, 10.00, 20.00),
(3, 2, 20, 10, 2, 115.00, 230.00),
(4, 3, 21, 13, 1, 410.00, 410.00),
(5, 4, 6, 6, 3, 12.50, 37.50),
(6, 5, 3, 3, 10, 18.50, 185.00),
(7, 6, 12, 17, 10, 11.50, 115.00),
(8, 6, 2, 2, 14, 2.50, 35.00);

-- ----------------------------------------------------------------------------
-- 14. SALES RETURN & RETURN ITEMS
-- ----------------------------------------------------------------------------
INSERT INTO sales_return (return_id, sale_id, customer_id, employee_id, return_date, refund_amount, reason, status) VALUES
(1, 1, 1, 2, '2026-09-29 15:30:00', 10.00, 'Excess tablets purchased by customer', 'Approved');

INSERT INTO return_item (return_item_id, return_id, sale_item_id, batch_id, quantity, refund_unit_price, subtotal, restocked) VALUES
(1, 1, 2, 5, 1, 10.00, 10.00, TRUE);

-- ----------------------------------------------------------------------------
-- 15. PRESCRIPTIONS (Patient Medical Prescriptions)
-- ----------------------------------------------------------------------------
INSERT INTO prescription (prescription_id, customer_id, doctor_id, prescription_date, diagnosis_notes) VALUES
(1, 1, 1, '2026-09-28', 'Acute upper respiratory viral infection, mild fever'),
(2, 2, 3, '2026-09-29', 'Dry cough, seasonal bronchial congestion'),
(3, 3, 2, '2026-09-30', 'Essential hypertension maintenance, follow-up after 1 month'),
(4, 4, 4, '2026-10-01', 'Type-2 diabetes mellitus routine therapy'),
(5, 5, 5, '2026-10-03', 'Post-operative orthopedic pain and joint stiffness');

-- ----------------------------------------------------------------------------
-- 16. PRESCRIPTION ITEMS
-- ----------------------------------------------------------------------------
INSERT INTO prescription_item (prescription_item_id, prescription_id, medicine_id, dosage_instruction, duration, quantity_prescribed) VALUES
(1, 1, 1, '1+0+1 after food', '5 Days', 10),
(2, 1, 5, '1+0+0 before breakfast', '10 Days', 10),
(3, 2, 10, '10ml thrice daily', '7 Days', 1),
(4, 3, 6, '1+0+0 morning', '30 Days', 30),
(5, 4, 7, '1+0+1 with meals', '30 Days', 60),
(6, 5, 12, 'Apply locally twice daily', '14 Days', 1);

-- ----------------------------------------------------------------------------
-- 17. INITIAL INVENTORY AUDIT RECORDS (Seed state)
-- ----------------------------------------------------------------------------
INSERT INTO inventory_audit (batch_id, medicine_id, change_type, old_quantity, new_quantity, quantity_change, changed_by, notes) VALUES
(1, 1, 'INITIAL_STOCK', 0, 150, 150, 'SYSTEM', 'Opening inventory calibration'),
(2, 2, 'INITIAL_STOCK', 0, 120, 120, 'SYSTEM', 'Opening inventory calibration'),
(3, 3, 'INITIAL_STOCK', 0, 45, 45, 'SYSTEM', 'Opening inventory calibration'),
(4, 4, 'INITIAL_STOCK', 0, 60, 60, 'SYSTEM', 'Opening inventory calibration'),
(5, 5, 'INITIAL_STOCK', 0, 80, 80, 'SYSTEM', 'Opening inventory calibration'),
(19, 9, 'SALE', 10, 4, -6, 'neha', 'Dispensed in historical invoice');
