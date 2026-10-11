const customerRepo = require('../repositories/customer.repository');

async function getAllCustomers(req, res, next) {
  try {
    const { search } = req.query;
    const customers = await customerRepo.findAll({ search });
    res.json({ success: true, count: customers.length, data: customers });
  } catch (err) {
    next(err);
  }
}

async function getCustomerById(req, res, next) {
  try {
    const customer = await customerRepo.findById(req.params.id);
    if (!customer) {
      return res.status(404).json({ success: false, error: 'Customer not found.' });
    }
    res.json({ success: true, data: customer });
  } catch (err) {
    next(err);
  }
}

async function createCustomer(req, res, next) {
  try {
    const { name, phone, email, address, age, gender } = req.body;
    if (!name || !phone) {
      return res.status(400).json({
        success: false,
        error: 'Validation Error: Name and phone are required.',
      });
    }

    const insertId = await customerRepo.create({ name, phone, email, address, age, gender });
    const created = await customerRepo.findById(insertId);
    res.status(201).json({
      success: true,
      message: 'Customer registered successfully.',
      data: created,
    });
  } catch (err) {
    next(err);
  }
}

async function getDoctors(req, res, next) {
  try {
    const doctors = await customerRepo.getDoctors();
    res.json({ success: true, data: doctors });
  } catch (err) {
    next(err);
  }
}

async function getPrescriptions(req, res, next) {
  try {
    const { customerId } = req.query;
    const rx = await customerRepo.getPrescriptions({ customerId });
    res.json({ success: true, count: rx.length, data: rx });
  } catch (err) {
    next(err);
  }
}

async function getPrescriptionDetails(req, res, next) {
  try {
    const items = await customerRepo.getPrescriptionItems(req.params.id);
    res.json({ success: true, data: items });
  } catch (err) {
    next(err);
  }
}

module.exports = {
  getAllCustomers,
  getCustomerById,
  createCustomer,
  getDoctors,
  getPrescriptions,
  getPrescriptionDetails,
};
