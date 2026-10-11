const medicineRepo = require('../repositories/medicine.repository');

async function getAllMedicines(req, res, next) {
  try {
    const { search, categoryId, status } = req.query;
    const medicines = await medicineRepo.findAll({ search, categoryId, status });
    res.json({ success: true, count: medicines.length, data: medicines });
  } catch (err) {
    next(err);
  }
}

async function getMedicineById(req, res, next) {
  try {
    const medicine = await medicineRepo.findById(req.params.id);
    if (!medicine) {
      return res.status(404).json({ success: false, error: 'Medicine not found.' });
    }
    res.json({ success: true, data: medicine });
  } catch (err) {
    next(err);
  }
}

async function createMedicine(req, res, next) {
  try {
    const { categoryId, manufacturerId, name, dosageForm } = req.body;
    if (!categoryId || !manufacturerId || !name || !dosageForm) {
      return res.status(400).json({
        success: false,
        error: 'Validation Error: categoryId, manufacturerId, name, and dosageForm are required.',
      });
    }

    const insertId = await medicineRepo.create(req.body, req.user?.username);
    const created = await medicineRepo.findById(insertId);
    res.status(201).json({
      success: true,
      message: 'Medicine successfully created.',
      data: created,
    });
  } catch (err) {
    next(err);
  }
}

async function updateMedicine(req, res, next) {
  try {
    const existing = await medicineRepo.findById(req.params.id);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Medicine not found.' });
    }

    await medicineRepo.update(req.params.id, req.body, req.user?.username);
    const updated = await medicineRepo.findById(req.params.id);
    res.json({
      success: true,
      message: 'Medicine updated successfully.',
      data: updated,
    });
  } catch (err) {
    next(err);
  }
}

async function deleteMedicine(req, res, next) {
  try {
    const existing = await medicineRepo.findById(req.params.id);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Medicine not found.' });
    }

    await medicineRepo.deleteById(req.params.id, req.user?.username);
    res.json({
      success: true,
      message: 'Medicine record deleted successfully.',
    });
  } catch (err) {
    next(err);
  }
}

async function getCategories(req, res, next) {
  try {
    const categories = await medicineRepo.getCategories();
    res.json({ success: true, data: categories });
  } catch (err) {
    next(err);
  }
}

async function getManufacturers(req, res, next) {
  try {
    const manufacturers = await medicineRepo.getManufacturers();
    res.json({ success: true, data: manufacturers });
  } catch (err) {
    next(err);
  }
}

module.exports = {
  getAllMedicines,
  getMedicineById,
  createMedicine,
  updateMedicine,
  deleteMedicine,
  getCategories,
  getManufacturers,
};
