const { query } = require('../config/db');

async function findAll({ search } = {}) {
  let sql = `SELECT customer_id, name, phone, email, address, age, gender, created_at FROM customer WHERE 1=1`;
  const params = [];
  if (search) {
    sql += ` AND (name LIKE ? OR phone LIKE ? OR address LIKE ?)`;
    const term = `%${search}%`;
    params.push(term, term, term);
  }
  sql += ` ORDER BY name ASC`;
  return await query(sql, params);
}

async function findById(customerId) {
  const rows = await query(`SELECT * FROM customer WHERE customer_id = ?`, [customerId]);
  return rows[0] || null;
}

async function create(data) {
  const result = await query(
    `INSERT INTO customer (name, phone, email, address, age, gender) VALUES (?, ?, ?, ?, ?, ?)`,
    [data.name, data.phone, data.email || null, data.address || null, data.age || null, data.gender || null]
  );
  return result.insertId;
}

async function getDoctors() {
  return await query(`SELECT doctor_id, name, specialization, hospital_clinic, phone, email FROM doctor ORDER BY name ASC`);
}

async function getPrescriptions({ customerId } = {}) {
  let sql = `
    SELECT 
      p.prescription_id,
      p.customer_id,
      c.name AS customer_name,
      p.doctor_id,
      d.name AS doctor_name,
      d.specialization,
      d.hospital_clinic,
      p.prescription_date,
      p.diagnosis_notes,
      COUNT(pi.prescription_item_id) AS total_items
    FROM prescription p
    JOIN customer c ON p.customer_id = c.customer_id
    JOIN doctor d ON p.doctor_id = d.doctor_id
    LEFT JOIN prescription_item pi ON p.prescription_id = pi.prescription_id
    WHERE 1=1
  `;
  const params = [];
  if (customerId) {
    sql += ` AND p.customer_id = ?`;
    params.push(parseInt(customerId, 10));
  }
  sql += ` GROUP BY p.prescription_id, p.customer_id, c.name, p.doctor_id, d.name, 
                    d.specialization, d.hospital_clinic, p.prescription_date, p.diagnosis_notes
           ORDER BY p.prescription_date DESC`;
  return await query(sql, params);
}

async function getPrescriptionItems(prescriptionId) {
  return await query(
    `SELECT 
      pi.prescription_item_id,
      pi.medicine_id,
      m.name AS medicine_name,
      pi.dosage_instruction,
      pi.duration,
      pi.quantity_prescribed
     FROM prescription_item pi
     JOIN medicine m ON pi.medicine_id = m.medicine_id
     WHERE pi.prescription_id = ?`,
    [prescriptionId]
  );
}

module.exports = {
  findAll,
  findById,
  create,
  getDoctors,
  getPrescriptions,
  getPrescriptionItems,
};
