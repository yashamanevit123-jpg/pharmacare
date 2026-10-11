const express = require('express');
const router = express.Router();
const reportController = require('../controllers/report.controller');
const { authenticateToken } = require('../middleware/auth');
const { authorizeRoles } = require('../middleware/rbac');

router.get('/low-stock', authenticateToken, reportController.getLowStock);
router.get('/expiring-batches', authenticateToken, reportController.getExpiringBatches);
router.get('/monthly-sales', authenticateToken, reportController.getMonthlySales);
router.get('/supplier-summary', authenticateToken, reportController.getSupplierSummary);

// Inventory audit trail requires Admin or Inventory Manager
router.get(
  '/audit',
  authenticateToken,
  authorizeRoles('Administrator', 'Inventory Manager'),
  reportController.getAuditHistory
);

module.exports = router;
