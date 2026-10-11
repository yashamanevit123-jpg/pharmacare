const { withConnection, query } = require('../config/db');

async function recordPurchase({ supplierId, employeeId, invoiceNumber, purchaseDate, notes, items, username }) {
  return await withConnection(async (conn) => {
    if (username) {
      await conn.query('SET @app_user = ?', [username]);
    }

    const itemsJson = JSON.stringify(items);

    await conn.query(
      `CALL sp_record_purchase(?, ?, ?, ?, ?, ?, @p_purchase_id, @p_total_amount)`,
      [
        supplierId,
        employeeId,
        invoiceNumber,
        purchaseDate || null,
        notes || null,
        itemsJson,
      ]
    );

    const [results] = await conn.query(
      `SELECT @p_purchase_id AS purchaseId, @p_total_amount AS totalAmount`
    );

    if (username) {
      await conn.query('SET @app_user = NULL').catch(() => {});
    }

    return results[0];
  });
}

async function findAll({ limit = 50, offset = 0, supplierId } = {}) {
  let sql = `
    SELECT 
      p.purchase_id,
      p.invoice_number,
      p.supplier_id,
      s.name AS supplier_name,
      s.contact_person,
      p.employee_id,
      e.full_name AS received_by,
      p.purchase_date,
      p.total_amount,
      p.payment_status,
      p.notes,
      COUNT(pi.purchase_item_id) AS total_items
    FROM purchase p
    JOIN supplier s ON p.supplier_id = s.supplier_id
    JOIN employee e ON p.employee_id = e.employee_id
    LEFT JOIN purchase_item pi ON p.purchase_id = pi.purchase_id
    WHERE 1=1
  `;
  const params = [];

  if (supplierId) {
    sql += ` AND p.supplier_id = ?`;
    params.push(parseInt(supplierId, 10));
  }

  sql += ` GROUP BY p.purchase_id, p.invoice_number, p.supplier_id, s.name, s.contact_person, 
                    p.employee_id, e.full_name, p.purchase_date, p.total_amount, p.payment_status, p.notes
           ORDER BY p.purchase_date DESC
           LIMIT ? OFFSET ?`;
  params.push(parseInt(limit, 10), parseInt(offset, 10));

  return await query(sql, params);
}

async function findById(purchaseId) {
  const rows = await query(
    `SELECT 
      p.purchase_id,
      p.invoice_number,
      p.supplier_id,
      s.name AS supplier_name,
      s.phone AS supplier_phone,
      p.employee_id,
      e.full_name AS received_by,
      p.purchase_date,
      p.total_amount,
      p.payment_status,
      p.notes
     FROM purchase p
     JOIN supplier s ON p.supplier_id = s.supplier_id
     JOIN employee e ON p.employee_id = e.employee_id
     WHERE p.purchase_id = ?`,
    [purchaseId]
  );
  return rows[0] || null;
}

async function findItemsByPurchaseId(purchaseId) {
  return await query(
    `SELECT 
      pi.purchase_item_id,
      pi.medicine_id,
      m.name AS medicine_name,
      pi.batch_number,
      pi.expiry_date,
      pi.quantity,
      pi.unit_cost,
      pi.total_cost
     FROM purchase_item pi
     JOIN medicine m ON pi.medicine_id = m.medicine_id
     WHERE pi.purchase_id = ?`,
    [purchaseId]
  );
}

async function getSuppliers() {
  return await query(
    `SELECT supplier_id, name, contact_person, phone, email, address, gst_number 
     FROM supplier 
     WHERE is_active = TRUE 
     ORDER BY name ASC`
  );
}

module.exports = {
  recordPurchase,
  findAll,
  findById,
  findItemsByPurchaseId,
  getSuppliers,
};
