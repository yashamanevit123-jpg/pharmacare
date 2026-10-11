const jwt = require('jsonwebtoken');
const config = require('../config/env');
const { query } = require('../config/db');

async function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({
      success: false,
      error: 'Authentication token required.',
    });
  }

  try {
    const decoded = jwt.verify(token, config.jwt.secret);
    
    // Verify user is still active in database
    const users = await query(
      `SELECT e.employee_id, e.username, e.full_name, e.is_active, r.role_id, r.role_name
       FROM employee e
       JOIN role r ON e.role_id = r.role_id
       WHERE e.employee_id = ?`,
      [decoded.employeeId]
    );

    if (!users || users.length === 0 || !users[0].is_active) {
      return res.status(401).json({
        success: false,
        error: 'Invalid or inactive user account.',
      });
    }

    const u = users[0];
    req.user = {
      employeeId: u.employee_id,
      username: u.username,
      fullName: u.full_name,
      roleId: u.role_id,
      roleName: u.role_name,
    };

    next();
  } catch (err) {
    return res.status(401).json({
      success: false,
      error: 'Invalid or expired authentication token.',
    });
  }
}

module.exports = {
  authenticateToken,
};
