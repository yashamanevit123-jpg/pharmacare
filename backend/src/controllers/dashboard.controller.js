const dashboardRepo = require('../repositories/dashboard.repository');

async function getDashboard(req, res, next) {
  try {
    const data = await dashboardRepo.getStats();
    res.json({
      success: true,
      data,
    });
  } catch (err) {
    next(err);
  }
}

module.exports = {
  getDashboard,
};
