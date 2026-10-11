const { withConnection, query } = require('../config/db');

async function processReturn({ saleId, employeeId, reason, items, username }) {
  return await withConnection(async (conn) => {
    if (username) {
      await conn.query('SET @app_user = ?', [username]);
    }

    const itemsJson = JSON.stringify(items);

    await conn.query(
      `CALL sp_process_return(?, ?, ?, ?, @p_return_id, @p_refund_amount)`,
      [
        saleId,
        employeeId,
        reason || 'Customer Return',
        itemsJson,
      ]
    );

    const [results] = await conn.query(
      `SELECT @p_return_id AS returnId, @p_refund_amount AS refundAmount`
    );

    if (username) {
      await conn.query('SET @app_user = NULL').catch(() => {});
    }

    return results[0];
  });
}

async function findAll({ limit = 50, offset = 0 } = {}) {
  return await query(
    `SELECT 
      r.return_id,
      r.sale_id,
      s.invoice_number,
      r.customer_id,
      c.name AS customer_name,
      r.employee_id,
      e.full_name AS processed_by,
      r.return_date,
      r.refund_amount,
      r.reason,
      r.status
     FROM sales_return r
     JOIN sale s ON r.sale_id = s.sale_id
     JOIN customer c ON r.customer_id = c.customer_id
     JOIN employee e ON r.employee_id = e.employee_id
     ORDER BY r.return_date DESC
     LIMIT ? OFFSET ?`,
    [parseInt(limit, 10), parseInt(offset, 10)]
  );
}

module.exports = {
  processReturn,
  findAll,
};
