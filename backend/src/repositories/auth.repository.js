const { query } = require('../config/db');

async function findByUsername(username) {
  const rows = await query(
    `SELECT e.employee_id, e.role_id, e.full_name, e.email, e.phone, e.username, 
            e.password_hash, e.is_active, r.role_name
     FROM employee e
     JOIN role r ON e.role_id = r.role_id
     WHERE e.username = ?`,
    [username]
  );
  return rows[0] || null;
}

async function findById(employeeId) {
  const rows = await query(
    `SELECT e.employee_id, e.role_id, e.full_name, e.email, e.phone, e.username, 
            e.is_active, r.role_name
     FROM employee e
     JOIN role r ON e.role_id = r.role_id
     WHERE e.employee_id = ?`,
    [employeeId]
  );
  return rows[0] || null;
}

module.exports = {
  findByUsername,
  findById,
};
