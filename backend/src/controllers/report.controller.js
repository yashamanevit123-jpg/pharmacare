const reportRepo = require('../repositories/report.repository');

async function getLowStock(req, res, next) {
  try {
    const data = await reportRepo.getLowStockReport();
    res.json({ success: true, count: data.length, data });
  } catch (err) {
    next(err);
  }
}

async function getExpiringBatches(req, res, next) {
  try {
    const data = await reportRepo.getExpiringBatchesReport();
    res.json({ success: true, count: data.length, data });
  } catch (err) {
    next(err);
  }
}

async function getMonthlySales(req, res, next) {
  try {
    const data = await reportRepo.getMonthlySalesReport();
    res.json({ success: true, count: data.length, data });
  } catch (err) {
    next(err);
  }
}

async function getSupplierSummary(req, res, next) {
  try {
    const data = await reportRepo.getSupplierPurchaseSummary();
    res.json({ success: true, count: data.length, data });
  } catch (err) {
    next(err);
  }
}

async function getAuditHistory(req, res, next) {
  try {
    const { limit, offset, batchId } = req.query;
    const data = await reportRepo.getAuditHistory({ limit, offset, batchId });
    res.json({ success: true, count: data.length, data });
  } catch (err) {
    next(err);
  }
}

module.exports = {
  getLowStock,
  getExpiringBatches,
  getMonthlySales,
  getSupplierSummary,
  getAuditHistory,
};
