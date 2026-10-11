const express = require('express');
const router = express.Router();
const batchController = require('../controllers/batch.controller');
const { authenticateToken } = require('../middleware/auth');

router.get('/', authenticateToken, batchController.getAllBatches);
router.get('/:id', authenticateToken, batchController.getBatchById);
router.get('/medicine/:medicineId/available', authenticateToken, batchController.getAvailableBatches);

module.exports = router;
