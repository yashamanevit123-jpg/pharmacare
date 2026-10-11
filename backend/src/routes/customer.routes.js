const express = require('express');
const router = express.Router();
const customerController = require('../controllers/customer.controller');
const { authenticateToken } = require('../middleware/auth');

router.get('/', authenticateToken, customerController.getAllCustomers);
router.post('/', authenticateToken, customerController.createCustomer);
router.get('/doctors', authenticateToken, customerController.getDoctors);
router.get('/prescriptions', authenticateToken, customerController.getPrescriptions);
router.get('/prescriptions/:id', authenticateToken, customerController.getPrescriptionDetails);
router.get('/:id', authenticateToken, customerController.getCustomerById);

module.exports = router;
