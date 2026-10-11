const { query } = require('../config/db');

async function getStats() {
  // Aggregate live metrics in a single coordinated query
  const statsRows = await query(`
    SELECT
      (SELECT COUNT(*) FROM medicine WHERE is_active = TRUE) AS totalMedicines,
      (SELECT COALESCE(SUM(quantity), 0) FROM batch WHERE status = 'Available' AND expiry_date >= CURRENT_DATE) AS totalSellableStock,
      (SELECT COUNT(DISTINCT medicine_id) FROM vw_low_stock) AS lowStockCount,
      (SELECT COUNT(*) FROM vw_expiring_batches) AS expiringBatchesCount,
      (SELECT COALESCE(SUM(total_amount), 0.00) FROM sale WHERE DATE(sale_date) = CURRENT_DATE AND status IN ('Completed', 'Partially_Refunded')) AS todaySalesRevenue,
      (SELECT COUNT(*) FROM sale WHERE DATE(sale_date) = CURRENT_DATE AND status IN ('Completed', 'Partially_Refunded')) AS todaySalesCount,
      (SELECT COALESCE(SUM(total_amount), 0.00) FROM sale WHERE YEAR(sale_date) = YEAR(CURRENT_DATE) AND MONTH(sale_date) = MONTH(CURRENT_DATE) AND status IN ('Completed', 'Partially_Refunded')) AS monthlySalesRevenue,
      (SELECT COUNT(*) FROM supplier WHERE is_active = TRUE) AS supplierCount,
      (SELECT fn_inventory_value(NULL)) AS totalInventoryValue
  `);

  const lowStockAlerts = await query(`SELECT * FROM vw_low_stock LIMIT 6`);
  const expiringAlerts = await query(`SELECT * FROM vw_expiring_batches ORDER BY expiry_date ASC LIMIT 6`);
  const recentSales = await query(`
    SELECT s.sale_id, s.invoice_number, c.name AS customer_name, s.sale_date, s.payment_method, s.total_amount
    FROM sale s
    JOIN customer c ON s.customer_id = c.customer_id
    ORDER BY s.sale_date DESC
    LIMIT 5
  `);
  const recentPurchases = await query(`
    SELECT p.purchase_id, p.invoice_number, sup.name AS supplier_name, p.purchase_date, p.total_amount
    FROM purchase p
    JOIN supplier sup ON p.supplier_id = sup.supplier_id
    ORDER BY p.purchase_date DESC
    LIMIT 5
  `);
  const topSelling = await query(`SELECT * FROM vw_top_selling_medicines ORDER BY total_quantity_sold DESC LIMIT 5`);

  return {
    metrics: statsRows[0],
    lowStockAlerts,
    expiringAlerts,
    recentSales,
    recentPurchases,
    topSelling,
  };
}

module.exports = {
  getStats,
};
