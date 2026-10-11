/**
 * PharmaCare Express API Comprehensive Test Suite (api_test_runner.js)
 * Tests:
 * 1. Health check
 * 2. Authentication (valid & invalid credentials)
 * 3. Token authorization & profile
 * 4. Role-Based Access Control (RBAC - Cashier vs Admin/Inventory routes)
 * 5. Input validation error handling
 * 6. Atomic Sale Processing (sp_process_sale via API) & trigger auditing
 * 7. Transaction Rollback on insufficient stock (Zero stock leakage)
 * 8. Clinical safety rollback on expired batch
 * 9. Atomic Purchase Processing (sp_record_purchase via API) & stock intake
 * 10. Dashboard & View-backed Report retrieval
 * 11. Static Web file serving
 */

const app = require('../src/app');
const { pool, query } = require('../src/config/db');

const TEST_PORT = 3999;
const BASE_URL = `http://localhost:${TEST_PORT}`;

let server;
let adminToken = '';
let cashierToken = '';
let invMgrToken = '';

let totalTests = 0;
let passedTests = 0;
let failedTests = 0;

function assert(condition, testName, details = '') {
  totalTests++;
  if (condition) {
    passedTests++;
    console.log(`  [PASS] ${testName}`);
  } else {
    failedTests++;
    console.error(`  [FAIL] ${testName} - ${details}`);
  }
}

async function request(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`;
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };
  const config = {
    method: options.method || 'GET',
    headers,
    ...(options.body ? { body: JSON.stringify(options.body) } : {}),
  };
  const res = await fetch(url, config);
  let data = null;
  const contentType = res.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    data = await res.json();
  } else {
    data = await res.text();
  }
  return { status: res.status, data };
}

async function runTests() {
  console.log('================================================================');
  console.log(' STARTING PHARMACARE EXPRESS API TEST SUITE');
  console.log(` Target Server: ${BASE_URL}`);
  console.log('================================================================\n');

  // Start test server on ephemeral port
  await new Promise((resolve) => {
    server = app.listen(TEST_PORT, () => {
      console.log(`[Test Server] Listening on port ${TEST_PORT}\n`);
      resolve();
    });
  });

  try {
    // ------------------------------------------------------------------------
    // 1. HEALTH CHECK
    // ------------------------------------------------------------------------
    console.log('--- Suite 1: Health & Static Serving ---');
    const health = await request('/api/health');
    assert(health.status === 200 && health.data.status === 'UP', 'GET /api/health returns UP status');

    const web = await request('/');
    assert(web.status === 200 && typeof web.data === 'string' && web.data.includes('PharmaCare'), 'GET / successfully serves preserved web frontend index.html');

    // ------------------------------------------------------------------------
    // 2. AUTHENTICATION & LOGIN
    // ------------------------------------------------------------------------
    console.log('\n--- Suite 2: Authentication ---');
    const invalidLogin = await request('/api/auth/login', {
      method: 'POST',
      body: { username: 'admin', password: 'wrongpassword' },
    });
    assert(invalidLogin.status === 401 && invalidLogin.data.success === false, 'POST /api/auth/login rejects incorrect password with 401');

    const adminLogin = await request('/api/auth/login', {
      method: 'POST',
      body: { username: 'admin', password: 'password123' },
    });
    assert(adminLogin.status === 200 && adminLogin.data.token && adminLogin.data.user.roleName === 'Administrator', 'POST /api/auth/login logs in Administrator and returns JWT');
    adminToken = adminLogin.data.token;

    const cashierLogin = await request('/api/auth/login', {
      method: 'POST',
      body: { username: 'neha', password: 'password123' },
    });
    assert(cashierLogin.status === 200 && cashierLogin.data.user.roleName === 'Cashier', 'POST /api/auth/login logs in Cashier (Neha Bansal)');
    cashierToken = cashierLogin.data.token;

    const invLogin = await request('/api/auth/login', {
      method: 'POST',
      body: { username: 'vikram', password: 'password123' },
    });
    assert(invLogin.status === 200 && invLogin.data.user.roleName === 'Inventory Manager', 'POST /api/auth/login logs in Inventory Manager (Vikram Malhotra)');
    invMgrToken = invLogin.data.token;

    const profile = await request('/api/auth/me', {
      headers: { Authorization: `Bearer ${adminToken}` },
    });
    assert(profile.status === 200 && profile.data.user.username === 'admin', 'GET /api/auth/me returns profile for authenticated Bearer token');

    const noToken = await request('/api/auth/me');
    assert(noToken.status === 401, 'GET /api/auth/me rejects request missing Bearer token with 401');

    // ------------------------------------------------------------------------
    // 3. ROLE-BASED ACCESS CONTROL (RBAC)
    // ------------------------------------------------------------------------
    console.log('\n--- Suite 3: Role-Based Authorization (RBAC) ---');
    // Cashier attempting to add medicine (Restricted to Admin / Pharmacist)
    const cashierAddMed = await request('/api/medicines', {
      method: 'POST',
      headers: { Authorization: `Bearer ${cashierToken}` },
      body: { categoryId: 1, manufacturerId: 1, name: 'Unauthorized Drug', dosageForm: 'Tablet' },
    });
    assert(cashierAddMed.status === 403, 'POST /api/medicines forbids Cashier role with 403');

    // Cashier attempting to create supplier purchase (Restricted to Admin / Inv Mgr)
    const cashierPur = await request('/api/purchases', {
      method: 'POST',
      headers: { Authorization: `Bearer ${cashierToken}` },
      body: { supplierId: 1, invoiceNumber: 'PUR-UNAUTH', items: [] },
    });
    assert(cashierPur.status === 403, 'POST /api/purchases forbids Cashier role with 403');

    // Administrator allowed to view medicines
    const adminMeds = await request('/api/medicines', {
      headers: { Authorization: `Bearer ${adminToken}` },
    });
    assert(adminMeds.status === 200 && adminMeds.data.count >= 30, 'GET /api/medicines allows authenticated access and returns 30+ medicines');

    // ------------------------------------------------------------------------
    // 4. VALIDATION HANDLING
    // ------------------------------------------------------------------------
    console.log('\n--- Suite 4: Request Validation ---');
    const badSale = await request('/api/sales', {
      method: 'POST',
      headers: { Authorization: `Bearer ${cashierToken}` },
      body: { customerId: null, items: [] },
    });
    assert(badSale.status === 400, 'POST /api/sales rejects missing customer and empty items with 400 Bad Request');

    // ------------------------------------------------------------------------
    // 5. ATOMIC SALE PROCESSING & AUDIT TRIGGER
    // ------------------------------------------------------------------------
    console.log('\n--- Suite 5: Atomic Sale Processing & Auditing ---');
    // Get stock before sale
    const [b1Before] = await query('SELECT quantity FROM batch WHERE batch_id = 1');
    const [b2Before] = await query('SELECT quantity FROM batch WHERE batch_id = 2');

    const validSale = await request('/api/sales', {
      method: 'POST',
      headers: { Authorization: `Bearer ${cashierToken}` },
      body: {
        customerId: 1,
        paymentMethod: 'UPI',
        discountAmount: 2.00,
        items: [
          { batchId: 1, quantity: 2 },
          { batchId: 2, quantity: 1 },
        ],
      },
    });

    assert(validSale.status === 201 && validSale.data.success === true, 'POST /api/sales executes sp_process_sale and returns 201 Created');
    assert(Boolean(validSale.data.data.saleId && validSale.data.data.invoiceNumber), 'POST /api/sales returns generated invoice number and saleId');

    // Verify stock decremented accurately in database
    const [b1After] = await query('SELECT quantity FROM batch WHERE batch_id = 1');
    const [b2After] = await query('SELECT quantity FROM batch WHERE batch_id = 2');

    assert(b1After.quantity === b1Before.quantity - 2, 'Batch 1 stock accurately decremented by 2 in MySQL');
    assert(b2After.quantity === b2Before.quantity - 1, 'Batch 2 stock accurately decremented by 1 in MySQL');

    // Verify trigger logged this adjustment into inventory_audit with user context
    const recentAudit = await query(`
      SELECT * FROM inventory_audit 
      WHERE batch_id = 1 AND change_type = 'SALE' 
      ORDER BY audit_id DESC LIMIT 1
    `);
    assert(recentAudit.length > 0 && recentAudit[0].changed_by === 'neha', 'Trigger trg_after_batch_stock_update logged audit record with initiating cashier username');

    // ------------------------------------------------------------------------
    // 6. TRANSACTION ROLLBACK TESTS
    // ------------------------------------------------------------------------
    console.log('\n--- Suite 6: Transactional Rollback & Clinical Safety ---');
    const [b1PriorFail] = await query('SELECT quantity FROM batch WHERE batch_id = 1');

    // Attempt to sell 99999 units (Insufficient stock)
    const overSale = await request('/api/sales', {
      method: 'POST',
      headers: { Authorization: `Bearer ${cashierToken}` },
      body: {
        customerId: 1,
        paymentMethod: 'Cash',
        items: [{ batchId: 1, quantity: 99999 }],
      },
    });

    assert(overSale.status === 400 && overSale.data.error.includes('Insufficient inventory'), 'POST /api/sales rejects excessive quantity and raises Insufficient inventory error');

    // Verify zero stock leakage
    const [b1PostFail] = await query('SELECT quantity FROM batch WHERE batch_id = 1');
    assert(b1PriorFail.quantity === b1PostFail.quantity, 'Rollback verified: Zero stock leakage on failed sale');

    // Attempt to sell expired batch (Batch 28)
    const expiredSale = await request('/api/sales', {
      method: 'POST',
      headers: { Authorization: `Bearer ${cashierToken}` },
      body: {
        customerId: 1,
        paymentMethod: 'Cash',
        items: [{ batchId: 28, quantity: 1 }],
      },
    });
    assert(expiredSale.status === 400 && (expiredSale.data.error.includes('expired') || expiredSale.data.error.includes('SAFETY')), 'POST /api/sales clinical safety: Expired batch sale rejected with 400');

    // ------------------------------------------------------------------------
    // 7. ATOMIC PURCHASE PROCESSING (sp_record_purchase)
    // ------------------------------------------------------------------------
    console.log('\n--- Suite 7: Procurement & Stock Addition ---');
    const [b1PrePur] = await query('SELECT quantity FROM batch WHERE batch_id = 1');
    const purInvNum = `PUR-API-${Date.now()}`;

    const purchaseRes = await request('/api/purchases', {
      method: 'POST',
      headers: { Authorization: `Bearer ${invMgrToken}` },
      body: {
        supplierId: 1,
        invoiceNumber: purInvNum,
        purchaseDate: '2026-10-11',
        notes: 'Express API procurement test order',
        items: [
          {
            medicineId: 1,
            batchNumber: 'DL650A',
            expiryDate: '2027-08-15',
            quantity: 40,
            unitCost: 1.20,
            sellingPrice: 2.00,
          },
        ],
      },
    });

    assert(purchaseRes.status === 201 && purchaseRes.data.success === true, 'POST /api/purchases executes sp_record_purchase and returns 201 Created');

    const [b1PostPur] = await query('SELECT quantity FROM batch WHERE batch_id = 1');
    assert(b1PostPur.quantity === b1PrePur.quantity + 40, 'Batch 1 stock incremented by 40 units atomically in MySQL');

    // ------------------------------------------------------------------------
    // 8. DASHBOARD & SQL VIEW-BACKED REPORTS
    // ------------------------------------------------------------------------
    console.log('\n--- Suite 8: Dashboard & Reports Integration ---');
    const dashboard = await request('/api/dashboard', {
      headers: { Authorization: `Bearer ${adminToken}` },
    });
    assert(dashboard.status === 200 && dashboard.data.data.metrics.totalMedicines >= 30, 'GET /api/dashboard returns live metrics from database tables');

    const lowStock = await request('/api/reports/low-stock', {
      headers: { Authorization: `Bearer ${adminToken}` },
    });
    assert(lowStock.status === 200 && Array.isArray(lowStock.data.data), 'GET /api/reports/low-stock queries vw_low_stock view');

    const monthlySales = await request('/api/reports/monthly-sales', {
      headers: { Authorization: `Bearer ${adminToken}` },
    });
    assert(monthlySales.status === 200 && Array.isArray(monthlySales.data.data), 'GET /api/reports/monthly-sales queries vw_monthly_sales view');

    const auditTrail = await request('/api/reports/audit', {
      headers: { Authorization: `Bearer ${adminToken}` },
    });
    assert(auditTrail.status === 200 && auditTrail.data.count > 0, 'GET /api/reports/audit returns trigger audit log rows');

    console.log('\n================================================================');
    console.log(` TEST SUMMARY: ${passedTests}/${totalTests} Passed (${failedTests} Failed)`);
    console.log('================================================================');

    if (failedTests > 0) {
      process.exitCode = 1;
    }
  } catch (err) {
    console.error('\n[Unhandled Test Error]:', err);
    process.exitCode = 1;
  } finally {
    // Teardown
    if (server) {
      server.close();
    }
    await pool.end();
  }
}

runTests();
