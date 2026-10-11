/**
 * PharmaCare Web Application Logic (app.js)
 * Fully integrates the preserved UI with the Express REST API and MySQL database.
 * Completely replaces hard-coded arrays with live asynchronous backend queries.
 */

// Global App State
const state = {
  curView: 'Dashboard',
  cart: [],
  poCart: [],
  medicines: [],
  categories: [],
  manufacturers: [],
  suppliers: [],
  customers: [],
  batches: [],
  user: null,
};

// Utilities
const $ = (s) => document.querySelector(s);
const inr = (n) => '₹' + Number(n || 0).toFixed(2);
const iso = (d) => new Date(d).toISOString().slice(0, 10);
const TODAY = iso(new Date());

function toast(msg) {
  const el = $('#toast');
  el.textContent = msg;
  el.style.display = 'block';
  clearTimeout(toast.t);
  toast.t = setTimeout(() => (el.style.display = 'none'), 2800);
}

function chip([text, cls]) {
  return `<span class="chip ${cls}">${text}</span>`;
}

function tbl(headers, rows, numCols = []) {
  return `
    <div class="scroll">
      <table>
        <thead>
          <tr>
            ${headers.map((h, i) => `<th class="${numCols.includes(i) ? 'n' : ''}">${h}</th>`).join('')}
          </tr>
        </thead>
        <tbody>
          ${
            rows.length
              ? rows.map((r) => `<tr>${r.map((c, i) => `<td class="${numCols.includes(i) ? 'n' : ''}">${c}</td>`).join('')}</tr>`).join('')
              : `<tr><td colspan="${headers.length}" class="empty">Nothing to show.</td></tr>`
          }
        </tbody>
      </table>
    </div>
  `;
}

const head = (title, sub) => `<h2>${title}</h2><p class="sub">${sub}</p>`;

// ============================================================================
// AUTHENTICATION & ROLE SWITCHER
// ============================================================================

async function initAuth() {
  let user = api.getUser();
  if (!user || !api.getToken()) {
    try {
      // Auto login as Pharmacist for seamless immediate demonstration
      const res = await api.login('rajesh', 'password123');
      user = res.user;
    } catch {
      // Fallback to admin
      const res = await api.login('admin', 'password123');
      user = res.user;
    }
  }
  state.user = user;
  renderUserBadge();
}

function renderUserBadge() {
  const badge = $('#user-badge');
  if (badge && state.user) {
    badge.innerHTML = `
      <span>${state.user.fullName || state.user.username}</span>
      <small>${state.user.roleName} (Click to switch)</small>
    `;
  }
}

function openRoleModal() {
  const modal = $('#role-modal');
  modal.style.display = 'flex';
}

function closeRoleModal() {
  $('#role-modal').style.display = 'none';
}

async function switchRole(username) {
  try {
    const res = await api.login(username, 'password123');
    state.user = res.user;
    renderUserBadge();
    closeRoleModal();
    toast(`Switched role to ${res.user.roleName} (${res.user.fullName})`);
    show(state.curView);
  } catch (err) {
    toast(`Failed to switch role: ${err.message}`);
  }
}

// ============================================================================
// VIEW DEFINITIONS
// ============================================================================

const V = {
  // --------------------------------------------------------------------------
  // 1. DASHBOARD VIEW (Live metrics from MySQL views & tables)
  // --------------------------------------------------------------------------
  async Dashboard() {
    const d = await api.getDashboard();
    const m = d.metrics;

    const lowStockRows = (d.lowStockAlerts || []).map((x) => [
      x.medicine_name,
      `<span class="chip bad">${x.batch_quantity}</span>`,
      x.rack_location,
    ]);

    const expRows = (d.expiringAlerts || []).map((x) => {
      const cls = x.expiry_status === 'EXPIRED' ? 'bad' : 'warn';
      const label = x.expiry_status === 'EXPIRED' ? 'Expired' : `${x.days_remaining}d left`;
      return [x.medicine_name, x.expiry_date, x.quantity, chip([label, cls])];
    });

    return `
      ${head('Dashboard', `Live database metrics as of ${TODAY} from pharmacare_db.`)}
      <div class="grid">
        <div class="stat">
          <b>${inr(m.todaySalesRevenue)}</b>
          <span>Today's sales (${m.todaySalesCount} bills)</span>
        </div>
        <div class="stat">
          <b>${m.totalMedicines}</b>
          <span>Medicines in catalog</span>
        </div>
        <div class="stat ${m.lowStockCount > 0 ? 'bad' : ''}">
          <b>${m.lowStockCount}</b>
          <span>Low stock medicines</span>
        </div>
        <div class="stat ${m.expiringBatchesCount > 0 ? 'warn' : ''}">
          <b>${m.expiringBatchesCount}</b>
          <span>Expired or near-expiry batches</span>
        </div>
      </div>
      <div class="cols">
        <div class="box">
          <h3>Low stock alert (Under reorder level)</h3>
          ${tbl(['Medicine', 'Stock', 'Rack'], lowStockRows, [1])}
        </div>
        <div class="box">
          <h3>Expiry alert (Under 90 days)</h3>
          ${tbl(['Medicine', 'Expiry', 'Qty', 'Status'], expRows, [2])}
        </div>
      </div>
      <div class="cols" style="margin-top: 18px;">
        <div class="box">
          <h3>Top-Selling Medicines</h3>
          ${tbl(
            ['Medicine', 'Category', 'Units Sold', 'Revenue'],
            (d.topSelling || []).map((t) => [
              t.medicine_name,
              t.category_name,
              t.total_quantity_sold,
              inr(t.total_revenue_generated),
            ]),
            [2, 3]
          )}
        </div>
        <div class="box">
          <h3>Total Store Inventory Valuation</h3>
          <div style="padding: 24px 10px;">
            <p style="font-size: 28px; font-weight: 700; color: var(--ok); margin: 0;">
              ${inr(m.totalInventoryValue)}
            </p>
            <p class="sub" style="margin-top: 6px;">
              Calculated via stored function <code>fn_inventory_value()</code> across all active non-expired stock.
            </p>
          </div>
        </div>
      </div>
    `;
  },

  // --------------------------------------------------------------------------
  // 2. INVENTORY VIEW (Medicines, Batches, Filter & Add Medicine)
  // --------------------------------------------------------------------------
  async Inventory() {
    state.categories = await api.getCategories();
    return `
      ${head('Inventory', 'Search, filter, and inspect batches on pharmacy shelves.')}
      <div class="bar">
        <input id="q" placeholder="Search medicine, generic name, or company...">
        <select id="cf">
          <option value="0">All categories</option>
          ${state.categories.map((c) => `<option value="${c.category_id}">${c.name}</option>`).join('')}
        </select>
        <select id="sf">
          <option value="">All statuses</option>
          <option value="low">Low stock</option>
        </select>
        <button class="b" id="btn-add-med">+ Add Medicine</button>
      </div>
      <div class="box" id="inv-table">
        <p class="empty">Loading inventory from MySQL...</p>
      </div>
    `;
  },

  // --------------------------------------------------------------------------
  // 3. BILLING VIEW (POS Sales with sp_process_sale and Printable Invoice)
  // --------------------------------------------------------------------------
  async Billing() {
    state.customers = await api.getCustomers();
    state.medicines = await api.getMedicines();
    const availableMeds = state.medicines.filter((m) => m.total_stock > 0);

    return `
      ${head('Billing', 'Generate sales invoices. Checkouts execute atomic sp_process_sale with row locks.')}
      <div class="cols">
        <div class="box">
          <h3>New Customer Sale</h3>
          <div class="bar">
            <select id="bc">
              ${state.customers.map((c) => `<option value="${c.customer_id}">${c.name} (${c.phone})</option>`).join('')}
            </select>
            <select id="bp">
              <option value="Cash">Cash</option>
              <option value="UPI">UPI</option>
              <option value="Card">Card</option>
            </select>
          </div>
          <div class="bar">
            <select id="bm" style="flex: 2;">
              <option value="">-- Select Medicine --</option>
              ${availableMeds.map((m) => `<option value="${m.medicine_id}">${m.name} (${m.total_stock} in stock)</option>`).join('')}
            </select>
            <select id="bb" style="flex: 2;">
              <option value="">-- Select Batch (FEFO) --</option>
            </select>
            <input id="bq" type="number" min="1" value="1" style="width: 70px;">
            <button class="b" id="btn-add-cart">Add</button>
          </div>
          <div id="cart-container"></div>
        </div>
        <div class="box">
          <h3>Recent Sales Invoices</h3>
          <div id="sales-history">
            <p class="empty">Loading invoices...</p>
          </div>
        </div>
      </div>
    `;
  },

  // --------------------------------------------------------------------------
  // 4. PURCHASES VIEW (Procurement PO with sp_record_purchase)
  // --------------------------------------------------------------------------
  async Purchases() {
    state.suppliers = await api.getSuppliers();
    state.medicines = await api.getMedicines();

    return `
      ${head('Purchases', 'Record wholesale orders from distributors via sp_record_purchase.')}
      <div class="cols">
        <div class="box">
          <h3>Create Purchase Order</h3>
          <div class="bar">
            <select id="po-sup">
              ${state.suppliers.map((s) => `<option value="${s.supplier_id}">${s.name}</option>`).join('')}
            </select>
            <input id="po-inv" placeholder="Supplier Invoice # (e.g. PUR-902)" value="PUR-${Date.now().toString().slice(-6)}">
          </div>
          <div class="bar">
            <select id="po-med" style="flex: 2;">
              ${state.medicines.map((m) => `<option value="${m.medicine_id}">${m.name}</option>`).join('')}
            </select>
            <input id="po-batch" placeholder="Batch No (e.g. B-01)" style="flex: 1;">
            <input id="po-exp" type="date" value="2027-12-31" style="flex: 1;">
          </div>
          <div class="bar">
            <input id="po-qty" type="number" placeholder="Qty" value="50" style="width: 80px;">
            <input id="po-cost" type="number" step="0.01" placeholder="Cost Price (₹)" value="10.00" style="flex: 1;">
            <input id="po-sell" type="number" step="0.01" placeholder="Selling Price (₹)" value="15.00" style="flex: 1;">
            <button class="b" id="btn-add-po">Add Item</button>
          </div>
          <div id="po-cart-container"></div>
        </div>
        <div class="box">
          <h3>Procurement History</h3>
          <div id="purchase-history">
            <p class="empty">Loading orders...</p>
          </div>
        </div>
      </div>
    `;
  },

  // --------------------------------------------------------------------------
  // 5. PRESCRIPTIONS VIEW (Preserved Doctors & Patient Regimens)
  // --------------------------------------------------------------------------
  async Prescriptions() {
    const rxList = await api.getPrescriptions();
    return `
      ${head('Prescriptions', 'Doctor medical prescriptions, patient records, and prescribed regimens.')}
      <div class="cols">
        ${
          rxList.map((r) => `
            <div class="box">
              <h3>${r.customer_name}</h3>
              <div class="rx">
                <p><b>${r.doctor_name}</b> · ${r.specialization} · ${r.hospital_clinic}</p>
                <p>Prescribed on ${r.prescription_date}</p>
                <p style="margin-top: 4px; font-style: italic;">"${r.diagnosis_notes || 'Routine therapy'}"</p>
                <button class="b sm alt" style="margin-top: 10px;" onclick="viewRxDetails(${r.prescription_id})">
                  View Regimen Items (${r.total_items})
                </button>
              </div>
            </div>
          `).join('') || '<p class="empty">No prescription records found.</p>'
        }
      </div>
    `;
  },

  // --------------------------------------------------------------------------
  // 6. RETURNS VIEW (Sales Returns with sp_process_return)
  // --------------------------------------------------------------------------
  async Returns() {
    const sales = await api.getSales();
    const returns = await api.getReturns();

    return `
      ${head('Returns', 'Process customer returns and restock eligible medicines via sp_process_return.')}
      <div class="cols">
        <div class="box">
          <h3>Initiate Sales Return</h3>
          <div class="form-group">
            <label>Select Sale Invoice</label>
            <select id="ret-sale">
              ${sales.map((s) => `<option value="${s.sale_id}">${s.invoice_number} · ${s.customer_name} (${inr(s.total_amount)})</option>`).join('')}
            </select>
          </div>
          <div class="form-group">
            <label>Return Reason</label>
            <input id="ret-reason" placeholder="e.g. Unopened excess strips, doctor changed prescription" value="Customer Return">
          </div>
          <div id="ret-items-container">
            <p class="sub">Select an invoice to load refundable line items.</p>
          </div>
        </div>
        <div class="box">
          <h3>Return History</h3>
          ${tbl(
            ['ID', 'Invoice', 'Customer', 'Refund', 'Reason', 'Status'],
            returns.map((r) => [
              r.return_id,
              r.invoice_number,
              r.customer_name,
              inr(r.refund_amount),
              r.reason,
              chip(['Approved', 'ok']),
            ]),
            [3]
          )}
        </div>
      </div>
    `;
  },

  // --------------------------------------------------------------------------
  // 7. REPORTS VIEW (Monthly Aggregation, Low Stock & Audit Trail)
  // --------------------------------------------------------------------------
  async Reports() {
    const monthly = await api.getMonthlySales();
    const audits = await api.getAudit({ limit: 15 });

    return `
      ${head('Reports', 'Comprehensive database views, monthly financial aggregations, and trigger audit logs.')}
      <div class="box">
        <h3>Monthly Revenue Aggregation (View: vw_monthly_sales)</h3>
        ${tbl(
          ['Year', 'Month', 'Invoices', 'Gross Total', 'Discounts', 'Net Revenue', 'Avg Bill Value'],
          monthly.map((m) => [
            m.sales_year,
            m.month_label,
            m.total_invoices,
            inr(m.gross_subtotal),
            inr(m.total_discounts),
            inr(m.net_revenue),
            inr(m.average_invoice_value),
          ]),
          [2, 3, 4, 5, 6]
        )}
      </div>
      <div class="box" style="margin-top: 18px;">
        <h3>Live Inventory Audit Trail (Populated by Trigger: trg_after_batch_stock_update)</h3>
        ${tbl(
          ['Audit ID', 'Batch', 'Medicine', 'Action', 'Before', 'After', 'Delta', 'Changed By', 'Time'],
          audits.map((a) => {
            const cls = a.quantity_change > 0 ? 'ok' : 'bad';
            return [
              a.audit_id,
              a.batch_number,
              a.medicine_name,
              chip([a.change_type, a.change_type === 'SALE' ? 'bad' : 'ok']),
              a.old_quantity,
              a.new_quantity,
              `<span class="chip ${cls}">${a.quantity_change > 0 ? '+' : ''}${a.quantity_change}</span>`,
              `<b>${a.changed_by}</b>`,
              new Date(a.changed_at).toLocaleTimeString(),
            ];
          }),
          [4, 5, 6]
        )}
      </div>
    `;
  },
};

// ============================================================================
// EVENT HANDLERS & RENDERING HELPERS
// ============================================================================

async function invRender() {
  const q = ($('#q')?.value || '').toLowerCase();
  const c = +($('#cf')?.value || 0);
  const s = $('#sf')?.value || '';

  const meds = await api.getMedicines({ search: q, categoryId: c, status: s });
  const container = $('#inv-table');
  if (!container) return;

  const rows = meds.map((m) => {
    const isLow = m.total_stock <= m.reorder_level;
    const stockChip = isLow
      ? `<span class="chip bad">${m.total_stock} (Low)</span>`
      : `<span class="chip ok">${m.total_stock}</span>`;

    const priceRange = m.min_price === m.max_price
      ? inr(m.min_price || 0)
      : `${inr(m.min_price || 0)} - ${inr(m.max_price || 0)}`;

    return [
      `<b>${m.name}</b><br><small style="color:var(--mute)">${m.generic_name || ''}</small>`,
      m.category_name,
      m.manufacturer_name,
      m.rack_location,
      stockChip,
      priceRange,
      `<button class="b sm alt" onclick="viewBatches(${m.medicine_id})">Batches (${m.batch_count})</button>`,
    ];
  });

  container.innerHTML = tbl(
    ['Medicine', 'Category', 'Company', 'Rack', 'Total Stock', 'Price', 'Actions'],
    rows,
    [4, 5]
  );
}

// POS Cart Operations
function cartRender() {
  const container = $('#cart-container');
  if (!container) return;

  if (!state.cart.length) {
    container.innerHTML = '<p class="empty">The bill is empty. Select a batch and add to start.</p>';
    return;
  }

  const gross = state.cart.reduce((t, i) => t + i.quantity * i.sellingPrice, 0);

  container.innerHTML = `
    ${tbl(
      ['Medicine', 'Batch', 'Qty', 'Unit Price', 'Amount', 'Action'],
      state.cart.map((i, idx) => [
        i.medicineName,
        i.batchNumber,
        i.quantity,
        inr(i.sellingPrice),
        inr(i.quantity * i.sellingPrice),
        `<button class="b sm alt" onclick="removeFromCart(${idx})">Remove</button>`,
      ]),
      [2, 3, 4]
    )}
    <div style="margin-top: 14px; display: flex; justify-content: space-between; align-items: center;">
      <div>
        <label style="font-size: 13px; color: var(--mute);">Discount (₹): </label>
        <input id="sale-discount" type="number" step="0.5" value="0.00" style="width: 80px;" oninput="updateSaleTotal()">
      </div>
      <p style="font-size: 18px; margin: 0;"><b>Net Total: <span id="net-total">${inr(gross)}</span></b></p>
    </div>
    <div class="bar" style="justify-content: flex-end; margin-top: 14px;">
      <button class="b alt" onclick="clearCart()">Clear</button>
      <button class="b" id="btn-complete-sale" onclick="completeSale()">Complete Sale</button>
    </div>
  `;
}

function updateSaleTotal() {
  const gross = state.cart.reduce((t, i) => t + i.quantity * i.sellingPrice, 0);
  const disc = parseFloat($('#sale-discount')?.value || 0) || 0;
  const net = Math.max(0, gross - disc);
  const netEl = $('#net-total');
  if (netEl) netEl.textContent = inr(net);
}

function clearCart() {
  state.cart = [];
  cartRender();
}

function removeFromCart(idx) {
  state.cart.splice(idx, 1);
  cartRender();
}

async function completeSale() {
  if (!state.cart.length) return toast('Cart is empty.');

  const customerId = +$('#bc').value;
  const paymentMethod = $('#bp').value;
  const discountAmount = parseFloat($('#sale-discount')?.value || 0) || 0;

  try {
    const res = await api.createSale({
      customerId,
      paymentMethod,
      discountAmount,
      items: state.cart.map((i) => ({ batchId: i.batchId, quantity: i.quantity })),
    });

    toast(`Invoice #${res.data.invoiceNumber} saved! Total: ${inr(res.data.totalAmount)}`);
    const invoiceData = {
      ...res.data,
      customerName: $('#bc').options[$('#bc').selectedIndex].text,
      paymentMethod,
      items: [...state.cart],
      discountAmount,
    };
    state.cart = [];
    cartRender();
    loadSalesHistory();
    openInvoiceModal(invoiceData);
  } catch (err) {
    toast(`Checkout failed: ${err.message}`);
  }
}

async function loadSalesHistory() {
  const container = $('#sales-history');
  if (!container) return;

  const sales = await api.getSales({ limit: 10 });
  container.innerHTML = tbl(
    ['Invoice', 'Customer', 'Date', 'Payment', 'Total', 'Action'],
    sales.map((s) => [
      `<b>${s.invoice_number}</b>`,
      s.customer_name,
      s.sale_date.split('T')[0] || s.sale_date.slice(0, 10),
      s.payment_method,
      inr(s.total_amount),
      `<button class="b sm alt" onclick="viewInvoice(${s.sale_id})">View Bill</button>`,
    ]),
    [4]
  );
}

// Purchase PO Operations
function poCartRender() {
  const container = $('#po-cart-container');
  if (!container) return;

  if (!state.poCart.length) {
    container.innerHTML = '<p class="empty">No items in purchase order yet.</p>';
    return;
  }

  const total = state.poCart.reduce((t, i) => t + i.quantity * i.unitCost, 0);

  container.innerHTML = `
    ${tbl(
      ['Medicine', 'Batch', 'Expiry', 'Qty', 'Cost', 'Line Total', 'Action'],
      state.poCart.map((i, idx) => [
        i.medicineName,
        i.batchNumber,
        i.expiryDate,
        i.quantity,
        inr(i.unitCost),
        inr(i.quantity * i.unitCost),
        `<button class="b sm alt" onclick="removePoItem(${idx})">Remove</button>`,
      ]),
      [3, 4, 5]
    )}
    <p style="text-align: right; font-size: 16px; margin: 12px 0;"><b>Total Order Amount: ${inr(total)}</b></p>
    <div class="bar" style="justify-content: flex-end;">
      <button class="b" onclick="submitPurchaseOrder()">Submit Purchase Order</button>
    </div>
  `;
}

function removePoItem(idx) {
  state.poCart.splice(idx, 1);
  poCartRender();
}

async function submitPurchaseOrder() {
  if (!state.poCart.length) return toast('PO cart is empty.');
  const supplierId = +$('#po-sup').value;
  const invoiceNumber = $('#po-inv').value.trim();

  if (!invoiceNumber) return toast('Please enter a supplier invoice number.');

  try {
    const res = await api.createPurchase({
      supplierId,
      invoiceNumber,
      items: state.poCart,
    });
    toast(`Purchase Order recorded! Order ID: ${res.data.purchaseId} (${inr(res.data.totalAmount)})`);
    state.poCart = [];
    poCartRender();
    loadPurchaseHistory();
  } catch (err) {
    toast(`Procurement failed: ${err.message}`);
  }
}

async function loadPurchaseHistory() {
  const container = $('#purchase-history');
  if (!container) return;
  const purchases = await api.getPurchases({ limit: 8 });
  container.innerHTML = tbl(
    ['Invoice #', 'Supplier', 'Date', 'Amount', 'Status'],
    purchases.map((p) => [
      `<b>${p.invoice_number}</b>`,
      p.supplier_name,
      p.purchase_date,
      inr(p.total_amount),
      chip([p.payment_status, 'ok']),
    ]),
    [3]
  );
}

// ============================================================================
// MODALS (New Medicine, Batches, Printable Bill, Prescription Regimen)
// ============================================================================

function openModal(id) {
  const el = $(`#${id}`);
  if (el) el.style.display = 'flex';
}

function closeModal(id) {
  const el = $(`#${id}`);
  if (el) el.style.display = 'none';
}

async function viewBatches(medId) {
  const batches = await api.getBatches({ medicineId: medId });
  const modal = $('#batch-modal');
  const body = $('#batch-modal-body');

  body.innerHTML = tbl(
    ['Batch No', 'Expiry Date', 'Status', 'Cost', 'Sell Price', 'Quantity In Stock'],
    batches.map((b) => [
      `<b>${b.batch_number}</b>`,
      b.expiry_date,
      chip([b.computed_status, b.computed_status === 'EXPIRED' ? 'bad' : b.computed_status === 'EXPIRING_SOON' ? 'warn' : 'ok']),
      inr(b.purchase_price),
      inr(b.selling_price),
      b.quantity,
    ]),
    [3, 4, 5]
  );
  openModal('batch-modal');
}

async function viewRxDetails(rxId) {
  const items = await api.getPrescriptionDetails(rxId);
  const body = $('#rx-modal-body');
  body.innerHTML = `
    <ul>
      ${items.map((i) => `<li><b>${i.medicine_name}</b> — ${i.dosage_instruction} (${i.duration}) [Qty: ${i.quantity_prescribed}]</li>`).join('')}
    </ul>
  `;
  openModal('rx-modal');
}

async function viewInvoice(saleId) {
  const sale = await api.getSale(saleId);
  openInvoiceModal({
    invoiceNumber: sale.invoice_number,
    saleDate: sale.sale_date,
    customerName: sale.customer_name,
    customerPhone: sale.customer_phone,
    paymentMethod: sale.payment_method,
    items: (sale.items || []).map((i) => ({
      medicineName: i.medicine_name,
      batchNumber: i.batch_number,
      quantity: i.quantity,
      sellingPrice: i.unit_price,
    })),
    discountAmount: sale.discount_amount,
    totalAmount: sale.total_amount,
  });
}

function openInvoiceModal(data) {
  const body = $('#invoice-modal-body');
  const gross = (data.items || []).reduce((t, i) => t + i.quantity * i.sellingPrice, 0);

  body.innerHTML = `
    <div class="invoice-receipt" style="border: 1px dashed var(--line); padding: 16px; border-radius: 8px;">
      <h2 style="text-align: center; margin-bottom: 2px;">PharmaCare</h2>
      <p style="text-align: center; color: var(--mute); font-size: 12px; margin: 0 0 14px;">
        Pharmacy Management System · Official Tax Invoice
      </p>
      <div style="font-size: 13px; line-height: 1.6; margin-bottom: 12px;">
        <div><b>Invoice:</b> ${data.invoiceNumber}</div>
        <div><b>Date:</b> ${data.saleDate || new Date().toLocaleString()}</div>
        <div><b>Customer:</b> ${data.customerName || 'Retail Customer'}</div>
        <div><b>Payment:</b> ${data.paymentMethod}</div>
      </div>
      ${tbl(
        ['Item', 'Batch', 'Qty', 'Rate', 'Amount'],
        (data.items || []).map((i) => [
          i.medicineName,
          i.batchNumber,
          i.quantity,
          inr(i.sellingPrice),
          inr(i.quantity * i.sellingPrice),
        ]),
        [2, 3, 4]
      )}
      <div style="margin-top: 14px; text-align: right; font-size: 14px;">
        <div>Subtotal: ${inr(gross)}</div>
        ${data.discountAmount > 0 ? `<div style="color:var(--ok)">Discount: -${inr(data.discountAmount)}</div>` : ''}
        <div style="font-size: 18px; font-weight: 700; margin-top: 4px;">Grand Total: ${inr(data.totalAmount)}</div>
      </div>
      <p style="text-align: center; font-size: 11px; color: var(--mute); margin-top: 20px;">
        Thank you for choosing PharmaCare. Medicines dispensed as per prescription.
      </p>
    </div>
  `;
  openModal('invoice-modal');
}

// ============================================================================
// MAIN ROUTING CONTROLLER
// ============================================================================

async function show(name) {
  state.curView = name;
  const main = $('#main');
  main.innerHTML = '<p class="empty" style="padding: 40px 0;">Loading view from database...</p>';

  document.querySelectorAll('nav button').forEach((b) => {
    b.classList.toggle('on', b.textContent.trim() === name);
  });

  if (V[name]) {
    try {
      main.innerHTML = await V[name]();
    } catch (err) {
      main.innerHTML = `
        <div class="box" style="border-color: var(--red);">
          <h3 style="color: var(--red);">Error Loading ${name}</h3>
          <p>${err.message}</p>
        </div>
      `;
      return;
    }
  }

  // View-specific bindings
  if (name === 'Inventory') {
    $('#q').oninput = invRender;
    $('#cf').onchange = invRender;
    $('#sf').onchange = invRender;
    $('#btn-add-med').onclick = () => openModal('add-med-modal');
    invRender();
  }

  if (name === 'Billing') {
    cartRender();
    loadSalesHistory();

    // Populate batches dynamically when a medicine is picked
    $('#bm').onchange = async () => {
      const medId = +$('#bm').value;
      const bSelect = $('#bb');
      bSelect.innerHTML = '<option value="">Loading batches...</option>';
      if (!medId) return;

      const batches = await api.getAvailableBatches(medId);
      if (!batches.length) {
        bSelect.innerHTML = '<option value="">No available batches</option>';
        return;
      }
      bSelect.innerHTML = batches
        .map((b) => `<option value="${b.batch_id}" data-price="${b.selling_price}" data-qty="${b.quantity}" data-num="${b.batch_number}">
            ${b.batch_number} (Exp: ${b.expiry_date}) — ${inr(b.selling_price)} [${b.quantity} left]
          </option>`)
        .join('');
    };

    $('#btn-add-cart').onclick = () => {
      const medId = +$('#bm').value;
      const bSelect = $('#bb');
      const batchId = +bSelect.value;
      const opt = bSelect.options[bSelect.selectedIndex];
      const qty = +$('#bq').value;

      if (!medId || !batchId) return toast('Select a medicine and eligible batch.');
      if (!(qty > 0)) return toast('Quantity must be 1 or more.');

      const avail = +opt.getAttribute('data-qty');
      const existing = state.cart.find((i) => i.batchId === batchId);
      const totalReq = (existing ? existing.quantity : 0) + qty;

      if (totalReq > avail) {
        return toast(`Only ${avail} units available in this batch.`);
      }

      if (existing) {
        existing.quantity = totalReq;
      } else {
        state.cart.push({
          batchId,
          batchNumber: opt.getAttribute('data-num'),
          medicineId: medId,
          medicineName: $('#bm').options[$('#bm').selectedIndex].text.split(' (')[0],
          sellingPrice: +opt.getAttribute('data-price'),
          quantity: qty,
        });
      }
      cartRender();
    };
  }

  if (name === 'Purchases') {
    poCartRender();
    loadPurchaseHistory();

    $('#btn-add-po').onclick = () => {
      const medId = +$('#po-med').value;
      const batchNumber = $('#po-batch').value.trim();
      const expiryDate = $('#po-exp').value;
      const quantity = +$('#po-qty').value;
      const unitCost = +$('#po-cost').value;
      const sellingPrice = +$('#po-sell').value;

      if (!batchNumber) return toast('Enter a batch number.');
      if (!expiryDate) return toast('Enter an expiry date.');
      if (!(quantity > 0)) return toast('Quantity must be greater than zero.');

      state.poCart.push({
        medicineId: medId,
        medicineName: $('#po-med').options[$('#po-med').selectedIndex].text,
        batchNumber,
        expiryDate,
        quantity,
        unitCost,
        sellingPrice,
      });

      $('#po-batch').value = '';
      poCartRender();
    };
  }

  if (name === 'Returns') {
    $('#ret-sale').onchange = async () => {
      const saleId = +$('#ret-sale').value;
      const sale = await api.getSale(saleId);
      const container = $('#ret-items-container');

      container.innerHTML = `
        <div style="margin-top: 14px;">
          <label style="font-weight: 600; font-size: 13px;">Items in Invoice #${sale.invoice_number}:</label>
          ${tbl(
            ['Item', 'Batch', 'Sold Qty', 'Rate', 'Return Qty', 'Restock'],
            (sale.items || []).map((i) => [
              i.medicine_name,
              i.batch_number,
              i.quantity,
              inr(i.unit_price),
              `<input id="ret-qty-${i.sale_item_id}" type="number" min="0" max="${i.quantity}" value="0" style="width: 70px;">`,
              `<input id="ret-chk-${i.sale_item_id}" type="checkbox" checked>`,
            ]),
            [2, 3]
          )}
          <button class="b" style="margin-top: 14px;" onclick="processSalesReturn(${sale.sale_id}, ${JSON.stringify(sale.items).replace(/"/g, '&quot;')})">
            Submit Return
          </button>
        </div>
      `;
    };
    $('#ret-sale').onchange();
  }
}

async function processSalesReturn(saleId, items) {
  const returnItems = [];
  for (const item of items) {
    const qtyInput = $(`#ret-qty-${item.sale_item_id}`);
    const chk = $(`#ret-chk-${item.sale_item_id}`);
    const qty = parseInt(qtyInput ? qtyInput.value : 0, 10);
    if (qty > 0) {
      returnItems.push({
        saleItemId: item.sale_item_id,
        quantity: qty,
        restock: chk ? chk.checked : true,
      });
    }
  }

  if (!returnItems.length) {
    return toast('Specify at least 1 unit to return.');
  }

  const reason = $('#ret-reason')?.value || 'Customer Return';

  try {
    const res = await api.createReturn({
      saleId,
      reason,
      items: returnItems,
    });
    toast(`Return processed! Refund amount: ${inr(res.data.refundAmount)}`);
    show('Returns');
  } catch (err) {
    toast(`Return failed: ${err.message}`);
  }
}

// Add Medicine Form Submission
async function submitNewMedicine() {
  const categoryId = +$('#new-med-cat').value;
  const manufacturerId = +$('#new-med-man').value;
  const name = $('#new-med-name').value.trim();
  const genericName = $('#new-med-gen').value.trim();
  const dosageForm = $('#new-med-form').value;
  const unitPack = $('#new-med-pack').value.trim();
  const rackLocation = $('#new-med-rack').value.trim();
  const reorderLevel = +$('#new-med-reorder').value;
  const isPrescriptionRequired = $('#new-med-rx').checked;

  if (!name) return toast('Medicine name is required.');

  try {
    await api.createMedicine({
      categoryId,
      manufacturerId,
      name,
      genericName,
      dosageForm,
      unitPack,
      rackLocation,
      reorderLevel,
      isPrescriptionRequired,
    });
    toast(`Medicine "${name}" created in catalog.`);
    closeModal('add-med-modal');
    invRender();
  } catch (err) {
    toast(`Failed to create medicine: ${err.message}`);
  }
}

// Initialization on DOM Ready
document.addEventListener('DOMContentLoaded', async () => {
  await initAuth();

  // Populate Add Medicine Modal Selects
  state.categories = await api.getCategories();
  state.manufacturers = await api.getManufacturers();

  const catSelect = $('#new-med-cat');
  if (catSelect) {
    catSelect.innerHTML = state.categories.map((c) => `<option value="${c.category_id}">${c.name}</option>`).join('');
  }

  const manSelect = $('#new-med-man');
  if (manSelect) {
    manSelect.innerHTML = state.manufacturers.map((m) => `<option value="${m.manufacturer_id}">${m.name}</option>`).join('');
  }

  show('Dashboard');
});
