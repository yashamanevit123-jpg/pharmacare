const { query } = require('../config/db');

async function getLowStockReport() {
  return await query(`SELECT * FROM vw_low_stock ORDER BY batch_quantity ASC`);
}

async function getExpiringBatchesReport() {
  return await query(`SELECT * FROM vw_expiring_batches ORDER BY expiry_date ASC`);
}

async function getMonthlySalesReport() {
  return await query(`SELECT * FROM vw_monthly_sales ORDER BY sales_year DESC, sales_month DESC`);
}

async function getSupplierPurchaseSummary() {
  return await query(`SELECT * FROM vw_supplier_purchase_summary ORDER BY total_amount_spent DESC`);
}

async function getAuditHistory({ limit = 100, offset = 0, batchId } = {}) {
  let sql = `
    SELECT 
      a.audit_id,
      a.batch_id,
      b.batch_number,
      a.medicine_id,
      m.name AS medicine_name,
      a.change_type,
      a.old_quantity,
      a.new_quantity,
      a.quantity_change,
      a.changed_by,
      a.changed_at,
      a.notes
    FROM inventory_audit a
    JOIN batch b ON a.batch_id = b.batch_id
    JOIN medicine m ON a.medicine_id = m.medicine_id
    WHERE 1=1
  `;
  const params = [];

  if (batchId) {
    sql += ` AND a.batch_id = ?`;
    params.push(parseInt(batchId, 10));
  }

  sql += ` ORDER BY a.changed_at DESC, a.audit_id DESC LIMIT ? OFFSET ?`;
  params.push(parseInt(limit, 10), parseInt(offset, 10));

  return await query(sql, params);
}

module.exports = {
  getLowStockReport,
  getExpiringBatchesReport,
  getMonthlySalesReport,
  getSupplierPurchaseSummary,
  getAuditHistory,
};
