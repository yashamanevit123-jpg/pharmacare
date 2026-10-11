const express = require('express');
const router = express.Router();
const returnController = require('../controllers/return.controller');
const { authenticateToken } = require('../middleware/auth');
const { authorizeRoles } = require('../middleware/rbac');

// Returns require Pharmacist or Admin authorization
router.post(
  '/',
  authenticateToken,
  authorizeRoles('Administrator', 'Pharmacist', 'Inventory Manager'),
  returnController.createReturn
);

router.get('/', authenticateToken, returnController.getAllReturns);

module.exports = router;
