const purchaseRepo = require('../repositories/purchase.repository');

async function createPurchase(req, res, next) {
  try {
    const { supplierId, invoiceNumber, purchaseDate, notes, items } = req.body;

    if (!supplierId || !invoiceNumber) {
      return res.status(400).json({
        success: false,
        error: 'Validation Error: supplierId and invoiceNumber are required.',
      });
    }

    if (!Array.isArray(items) || items.length === 0) {
      return res.status(400).json({
        success: false,
        error: 'Validation Error: Purchase order must contain at least one line item.',
      });
    }

    for (const it of items) {
      if (!it.medicineId && !it.medicine_id) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error: Each item must specify medicineId.',
        });
      }
      if (!it.batchNumber && !it.batch_number) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error: Each item must specify batchNumber.',
        });
      }
      if (!it.expiryDate && !it.expiry_date) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error: Each item must specify expiryDate.',
        });
      }
      if (!it.quantity || it.quantity <= 0) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error: Quantity must be greater than zero.',
        });
      }
    }

    const formattedItems = items.map(i => ({
      medicine_id: parseInt(i.medicineId || i.medicine_id, 10),
      batch_number: (i.batchNumber || i.batch_number).trim(),
      expiry_date: i.expiryDate || i.expiry_date,
      quantity: parseInt(i.quantity, 10),
      unit_cost: parseFloat(i.unitCost || i.unit_cost || 0),
      selling_price: parseFloat(i.sellingPrice || i.selling_price || 0),
    }));

    const result = await purchaseRepo.recordPurchase({
      supplierId: parseInt(supplierId, 10),
      employeeId: req.user.employeeId,
      invoiceNumber: invoiceNumber.trim(),
      purchaseDate,
      notes,
      items: formattedItems,
      username: req.user.username,
    });

    res.status(201).json({
      success: true,
      message: 'Purchase recorded and stock added atomically.',
      data: {
        purchaseId: result.purchaseId,
        totalAmount: result.totalAmount,
      },
    });
  } catch (err) {
    next(err);
  }
}

async function getAllPurchases(req, res, next) {
  try {
    const { limit, offset, supplierId } = req.query;
    const purchases = await purchaseRepo.findAll({ limit, offset, supplierId });
    res.json({ success: true, count: purchases.length, data: purchases });
  } catch (err) {
    next(err);
  }
}

async function getPurchaseById(req, res, next) {
  try {
    const purchase = await purchaseRepo.findById(req.params.id);
    if (!purchase) {
      return res.status(404).json({ success: false, error: 'Purchase record not found.' });
    }

    const items = await purchaseRepo.findItemsByPurchaseId(req.params.id);
    res.json({
      success: true,
      data: {
        ...purchase,
        items,
      },
    });
  } catch (err) {
    next(err);
  }
}

async function getSuppliers(req, res, next) {
  try {
    const suppliers = await purchaseRepo.getSuppliers();
    res.json({ success: true, data: suppliers });
  } catch (err) {
    next(err);
  }
}

module.exports = {
  createPurchase,
  getAllPurchases,
  getPurchaseById,
  getSuppliers,
};
