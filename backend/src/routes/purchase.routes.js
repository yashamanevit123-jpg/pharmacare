const express = require('express');
const router = express.Router();
const purchaseController = require('../controllers/purchase.controller');
const { authenticateToken } = require('../middleware/auth');
const { authorizeRoles } = require('../middleware/rbac');

// Inventory Manager, Pharmacist, and Admin can enter supplier purchases
router.post(
  '/',
  authenticateToken,
  authorizeRoles('Administrator', 'Inventory Manager', 'Pharmacist'),
  purchaseController.createPurchase
);

router.get('/', authenticateToken, purchaseController.getAllPurchases);
router.get('/suppliers', authenticateToken, purchaseController.getSuppliers);
router.get('/:id', authenticateToken, purchaseController.getPurchaseById);

module.exports = router;
