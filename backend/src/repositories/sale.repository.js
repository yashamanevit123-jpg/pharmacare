const { withConnection, query } = require('../config/db');

/**
 * Execute atomic sale processing procedure sp_process_sale
 * Transaction management and inventory deductions are handled entirely inside MySQL.
 */
async function processSale({ customerId, employeeId, paymentMethod, discountAmount, items, username }) {
  return await withConnection(async (conn) => {
    if (username) {
      await conn.query('SET @app_user = ?', [username]);
    }

    const itemsJson = JSON.stringify(items);

    // Call stored procedure
    await conn.query(
      `CALL sp_process_sale(?, ?, ?, ?, ?, @p_sale_id, @p_invoice_number, @p_total_amount)`,
      [
        customerId,
        employeeId,
        paymentMethod || 'Cash',
        discountAmount || 0.00,
        itemsJson,
      ]
    );

    // Retrieve procedure OUT parameters
    const [results] = await conn.query(
      `SELECT @p_sale_id AS saleId, @p_invoice_number AS invoiceNumber, @p_total_amount AS totalAmount`
    );

    if (username) {
      await conn.query('SET @app_user = NULL').catch(() => {});
    }

    return results[0];
  });
}

async function findAll({ limit = 50, offset = 0, customerId } = {}) {
  let sql = `
    SELECT 
      s.sale_id,
      s.invoice_number,
      s.customer_id,
      c.name AS customer_name,
      c.phone AS customer_phone,
      s.employee_id,
      e.full_name AS cashier_name,
      s.sale_date,
      s.payment_method,
      s.subtotal,
      s.discount_amount,
      s.tax_amount,
      s.total_amount,
      s.status,
      COUNT(si.sale_item_id) AS total_items
    FROM sale s
    JOIN customer c ON s.customer_id = c.customer_id
    JOIN employee e ON s.employee_id = e.employee_id
    LEFT JOIN sale_item si ON s.sale_id = si.sale_id
    WHERE 1=1
  `;
  const params = [];

  if (customerId) {
    sql += ` AND s.customer_id = ?`;
    params.push(parseInt(customerId, 10));
  }

  sql += ` GROUP BY s.sale_id, s.invoice_number, s.customer_id, c.name, c.phone, 
                    s.employee_id, e.full_name, s.sale_date, s.payment_method, 
                    s.subtotal, s.discount_amount, s.tax_amount, s.total_amount, s.status
           ORDER BY s.sale_date DESC
           LIMIT ? OFFSET ?`;
  params.push(parseInt(limit, 10), parseInt(offset, 10));

  return await query(sql, params);
}

async function findById(saleId) {
  const rows = await query(
    `SELECT 
      s.sale_id,
      s.invoice_number,
      s.customer_id,
      c.name AS customer_name,
      c.phone AS customer_phone,
      c.address AS customer_address,
      s.employee_id,
      e.full_name AS cashier_name,
      s.sale_date,
      s.payment_method,
      s.subtotal,
      s.discount_amount,
      s.tax_amount,
      s.total_amount,
      s.status
     FROM sale s
     JOIN customer c ON s.customer_id = c.customer_id
     JOIN employee e ON s.employee_id = e.employee_id
     WHERE s.sale_id = ?`,
    [saleId]
  );
  return rows[0] || null;
}

async function findItemsBySaleId(saleId) {
  return await query(
    `SELECT 
      si.sale_item_id,
      si.batch_id,
      b.batch_number,
      b.expiry_date,
      si.medicine_id,
      m.name AS medicine_name,
      m.unit_pack,
      si.quantity,
      si.unit_price,
      si.subtotal
     FROM sale_item si
     JOIN batch b ON si.batch_id = b.batch_id
     JOIN medicine m ON si.medicine_id = m.medicine_id
     WHERE si.sale_id = ?`,
    [saleId]
  );
}

module.exports = {
  processSale,
  findAll,
  findById,
  findItemsBySaleId,
};
