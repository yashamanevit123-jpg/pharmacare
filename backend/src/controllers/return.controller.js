const returnRepo = require('../repositories/return.repository');

async function createReturn(req, res, next) {
  try {
    const { saleId, reason, items } = req.body;

    if (!saleId || !Array.isArray(items) || items.length === 0) {
      return res.status(400).json({
        success: false,
        error: 'Validation Error: saleId and items array are required.',
      });
    }

    const formattedItems = items.map(i => ({
      sale_item_id: parseInt(i.saleItemId || i.sale_item_id, 10),
      quantity: parseInt(i.quantity, 10),
      restock: i.restock !== undefined ? i.restock : true,
    }));

    const result = await returnRepo.processReturn({
      saleId: parseInt(saleId, 10),
      employeeId: req.user.employeeId,
      reason,
      items: formattedItems,
      username: req.user.username,
    });

    res.status(201).json({
      success: true,
      message: 'Sales return processed and eligible stock replenished.',
      data: {
        returnId: result.returnId,
        refundAmount: result.refundAmount,
      },
    });
  } catch (err) {
    next(err);
  }
}

async function getAllReturns(req, res, next) {
  try {
    const { limit, offset } = req.query;
    const returns = await returnRepo.findAll({ limit, offset });
    res.json({ success: true, count: returns.length, data: returns });
  } catch (err) {
    next(err);
  }
}

module.exports = {
  createReturn,
  getAllReturns,
};
