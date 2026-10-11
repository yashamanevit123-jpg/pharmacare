const { query } = require('../config/db');

async function findAll({ medicineId, status, availableOnly } = {}) {
  let sql = `
    SELECT 
      b.batch_id,
      b.medicine_id,
      m.name AS medicine_name,
      m.rack_location,
      c.name AS category_name,
      man.name AS manufacturer_name,
      b.supplier_id,
      sup.name AS supplier_name,
      b.batch_number,
      b.manufacturing_date,
      b.expiry_date,
      DATEDIFF(b.expiry_date, CURRENT_DATE) AS days_remaining,
      b.purchase_price,
      b.selling_price,
      b.quantity,
      b.status,
      fn_stock_status(b.batch_id) AS computed_status
    FROM batch b
    JOIN medicine m ON b.medicine_id = m.medicine_id
    JOIN category c ON m.category_id = c.category_id
    JOIN manufacturer man ON m.manufacturer_id = man.manufacturer_id
    JOIN supplier sup ON b.supplier_id = sup.supplier_id
    WHERE 1=1
  `;
  const params = [];

  if (medicineId) {
    sql += ` AND b.medicine_id = ?`;
    params.push(parseInt(medicineId, 10));
  }

  if (availableOnly) {
    sql += ` AND b.status = 'Available' AND b.quantity > 0 AND b.expiry_date >= CURRENT_DATE`;
  }

  if (status) {
    sql += ` AND b.status = ?`;
    params.push(status);
  }

  sql += ` ORDER BY b.expiry_date ASC, b.quantity DESC`;

  return await query(sql, params);
}

async function findById(batchId) {
  const rows = await query(
    `SELECT 
      b.batch_id,
      b.medicine_id,
      m.name AS medicine_name,
      b.batch_number,
      b.expiry_date,
      b.purchase_price,
      b.selling_price,
      b.quantity,
      b.status,
      fn_stock_status(b.batch_id) AS computed_status
     FROM batch b
     JOIN medicine m ON b.medicine_id = m.medicine_id
     WHERE b.batch_id = ?`,
    [batchId]
  );
  return rows[0] || null;
}

async function findAvailableByMedicine(medicineId) {
  return await query(
    `SELECT batch_id, batch_number, expiry_date, selling_price, quantity
     FROM batch
     WHERE medicine_id = ? 
       AND status = 'Available' 
       AND quantity > 0 
       AND expiry_date >= CURRENT_DATE
     ORDER BY expiry_date ASC`,
    [medicineId]
  );
}

module.exports = {
  findAll,
  findById,
  findAvailableByMedicine,
};
