const saleRepo = require('../repositories/sale.repository');

async function createSale(req, res, next) {
  try {
    const { customerId, paymentMethod, discountAmount, items } = req.body;

    if (!customerId) {
      return res.status(400).json({
        success: false,
        error: 'Validation Error: customerId is required.',
      });
    }

    if (!Array.isArray(items) || items.length === 0) {
      return res.status(400).json({
        success: false,
        error: 'Validation Error: Invoice must contain at least one item.',
      });
    }

    for (const it of items) {
      if (!it.batchId && !it.batch_id) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error: Each item must specify a valid batchId.',
        });
      }
      if (!it.quantity || it.quantity <= 0) {
        return res.status(400).json({
          success: false,
          error: 'Validation Error: Item quantity must be greater than zero.',
        });
      }
    }

    // Format items payload for sp_process_sale
    const formattedItems = items.map(i => ({
      batch_id: parseInt(i.batchId || i.batch_id, 10),
      quantity: parseInt(i.quantity, 10),
    }));

    // Delegate transaction, locking, inventory adjustments, and triggers to MySQL
    const result = await saleRepo.processSale({
      customerId: parseInt(customerId, 10),
      employeeId: req.user.employeeId,
      paymentMethod: paymentMethod || 'Cash',
      discountAmount: parseFloat(discountAmount) || 0.00,
      items: formattedItems,
      username: req.user.username,
    });

    res.status(201).json({
      success: true,
      message: 'Sale successfully processed and inventory updated.',
      data: {
        saleId: result.saleId,
        invoiceNumber: result.invoiceNumber,
        totalAmount: result.totalAmount,
      },
    });
  } catch (err) {
    next(err);
  }
}

async function getAllSales(req, res, next) {
  try {
    const { limit, offset, customerId } = req.query;
    const sales = await saleRepo.findAll({ limit, offset, customerId });
    res.json({ success: true, count: sales.length, data: sales });
  } catch (err) {
    next(err);
  }
}

async function getSaleById(req, res, next) {
  try {
    const sale = await saleRepo.findById(req.params.id);
    if (!sale) {
      return res.status(404).json({ success: false, error: 'Sale invoice not found.' });
    }

    const items = await saleRepo.findItemsBySaleId(req.params.id);
    res.json({
      success: true,
      data: {
        ...sale,
        items,
      },
    });
  } catch (err) {
    next(err);
  }
}

module.exports = {
  createSale,
  getAllSales,
  getSaleById,
};
