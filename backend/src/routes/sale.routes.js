const express = require('express');
const router = express.Router();
const saleController = require('../controllers/sale.controller');
const { authenticateToken } = require('../middleware/auth');
const { authorizeRoles } = require('../middleware/rbac');

// Cashier, Pharmacist, and Admin can create POS sales
router.post(
  '/',
  authenticateToken,
  authorizeRoles('Administrator', 'Pharmacist', 'Cashier'),
  saleController.createSale
);

router.get('/', authenticateToken, saleController.getAllSales);
router.get('/:id', authenticateToken, saleController.getSaleById);

module.exports = router;
