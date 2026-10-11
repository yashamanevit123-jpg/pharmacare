const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const config = require('../config/env');
const authRepo = require('../repositories/auth.repository');

async function login(req, res, next) {
  try {
    const { username, password } = req.body;

    if (!username || !password) {
      return res.status(400).json({
        success: false,
        error: 'Username and password are required.',
      });
    }

    const employee = await authRepo.findByUsername(username.trim());

    if (!employee || !employee.is_active) {
      return res.status(401).json({
        success: false,
        error: 'Invalid credentials or inactive account.',
      });
    }

    const isMatch = await bcrypt.compare(password, employee.password_hash);
    if (!isMatch) {
      return res.status(401).json({
        success: false,
        error: 'Invalid credentials or inactive account.',
      });
    }

    const payload = {
      employeeId: employee.employee_id,
      username: employee.username,
      fullName: employee.full_name,
      roleId: employee.role_id,
      roleName: employee.role_name,
    };

    const token = jwt.sign(payload, config.jwt.secret, {
      expiresIn: config.jwt.expiresIn,
    });

    res.json({
      success: true,
      message: 'Login successful.',
      token,
      user: payload,
    });
  } catch (err) {
    next(err);
  }
}

async function getProfile(req, res, next) {
  try {
    const employee = await authRepo.findById(req.user.employeeId);
    if (!employee) {
      return res.status(404).json({ success: false, error: 'User profile not found.' });
    }
    res.json({
      success: true,
      user: {
        employeeId: employee.employee_id,
        fullName: employee.full_name,
        email: employee.email,
        phone: employee.phone,
        username: employee.username,
        roleId: employee.role_id,
        roleName: employee.role_name,
      },
    });
  } catch (err) {
    next(err);
  }
}

module.exports = {
  login,
  getProfile,
};
