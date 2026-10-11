const express = require('express');
const router = express.Router();
const medicineController = require('../controllers/medicine.controller');
const { authenticateToken } = require('../middleware/auth');
const { authorizeRoles } = require('../middleware/rbac');

// All authenticated roles can browse medicines
router.get('/', authenticateToken, medicineController.getAllMedicines);
router.get('/categories', authenticateToken, medicineController.getCategories);
router.get('/manufacturers', authenticateToken, medicineController.getManufacturers);
router.get('/:id', authenticateToken, medicineController.getMedicineById);

// Creation, mutation, and deletion restricted to Admin and Pharmacist
router.post(
  '/',
  authenticateToken,
  authorizeRoles('Administrator', 'Pharmacist'),
  medicineController.createMedicine
);

router.put(
  '/:id',
  authenticateToken,
  authorizeRoles('Administrator', 'Pharmacist'),
  medicineController.updateMedicine
);

router.delete(
  '/:id',
  authenticateToken,
  authorizeRoles('Administrator'),
  medicineController.deleteMedicine
);

module.exports = router;
