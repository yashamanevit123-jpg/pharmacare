const batchRepo = require('../repositories/batch.repository');

async function getAllBatches(req, res, next) {
  try {
    const { medicineId, status, availableOnly } = req.query;
    const batches = await batchRepo.findAll({
      medicineId,
      status,
      availableOnly: availableOnly === 'true',
    });
    res.json({ success: true, count: batches.length, data: batches });
  } catch (err) {
    next(err);
  }
}

async function getBatchById(req, res, next) {
  try {
    const batch = await batchRepo.findById(req.params.id);
    if (!batch) {
      return res.status(404).json({ success: false, error: 'Batch not found.' });
    }
    res.json({ success: true, data: batch });
  } catch (err) {
    next(err);
  }
}

async function getAvailableBatches(req, res, next) {
  try {
    const batches = await batchRepo.findAvailableByMedicine(req.params.medicineId);
    res.json({ success: true, count: batches.length, data: batches });
  } catch (err) {
    next(err);
  }
}

module.exports = {
  getAllBatches,
  getBatchById,
  getAvailableBatches,
};
