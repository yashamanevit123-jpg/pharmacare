const { query } = require('../config/db');

async function findAll({ search, categoryId, status } = {}) {
  let sql = `
    SELECT 
      m.medicine_id,
      m.name,
      m.generic_name,
      m.dosage_form,
      m.unit_pack,
      m.reorder_level,
      m.rack_location,
      m.is_prescription_required,
      m.is_active,
      c.category_id,
      c.name AS category_name,
      man.manufacturer_id,
      man.name AS manufacturer_name,
      COALESCE(SUM(CASE WHEN b.expiry_date >= CURRENT_DATE AND b.status = 'Available' THEN b.quantity ELSE 0 END), 0) AS total_stock,
      MIN(CASE WHEN b.expiry_date >= CURRENT_DATE AND b.status = 'Available' THEN b.selling_price END) AS min_price,
      MAX(CASE WHEN b.expiry_date >= CURRENT_DATE AND b.status = 'Available' THEN b.selling_price END) AS max_price,
      COUNT(b.batch_id) AS batch_count
    FROM medicine m
    JOIN category c ON m.category_id = c.category_id
    JOIN manufacturer man ON m.manufacturer_id = man.manufacturer_id
    LEFT JOIN batch b ON m.medicine_id = b.medicine_id
    WHERE m.is_active = TRUE
  `;
  const params = [];

  if (search) {
    sql += ` AND (m.name LIKE ? OR m.generic_name LIKE ? OR man.name LIKE ?)`;
    const term = `%${search}%`;
    params.push(term, term, term);
  }

  if (categoryId && parseInt(categoryId, 10) > 0) {
    sql += ` AND m.category_id = ?`;
    params.push(parseInt(categoryId, 10));
  }

  sql += ` GROUP BY m.medicine_id, m.name, m.generic_name, m.dosage_form, m.unit_pack, 
                    m.reorder_level, m.rack_location, m.is_prescription_required, m.is_active, 
                    c.category_id, c.name, man.manufacturer_id, man.name`;

  if (status === 'low') {
    sql += ` HAVING total_stock <= m.reorder_level`;
  }

  sql += ` ORDER BY m.name ASC`;

  return await query(sql, params);
}

async function findById(medicineId) {
  const rows = await query(
    `SELECT 
      m.medicine_id,
      m.name,
      m.generic_name,
      m.dosage_form,
      m.unit_pack,
      m.reorder_level,
      m.rack_location,
      m.is_prescription_required,
      m.is_active,
      m.category_id,
      c.name AS category_name,
      m.manufacturer_id,
      man.name AS manufacturer_name
     FROM medicine m
     JOIN category c ON m.category_id = c.category_id
     JOIN manufacturer man ON m.manufacturer_id = man.manufacturer_id
     WHERE m.medicine_id = ?`,
    [medicineId]
  );
  return rows[0] || null;
}

async function create(data, username) {
  const result = await query(
    `INSERT INTO medicine (
      category_id, manufacturer_id, name, generic_name, dosage_form, 
      unit_pack, reorder_level, rack_location, is_prescription_required
     ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
    [
      data.categoryId,
      data.manufacturerId,
      data.name,
      data.genericName || null,
      data.dosageForm,
      data.unitPack || '1 Strip',
      data.reorderLevel || 10,
      data.rackLocation || 'R1-A1',
      data.isPrescriptionRequired ? 1 : 0,
    ],
    username
  );
  return result.insertId;
}

async function update(medicineId, data, username) {
  await query(
    `UPDATE medicine SET 
      category_id = COALESCE(?, category_id),
      manufacturer_id = COALESCE(?, manufacturer_id),
      name = COALESCE(?, name),
      generic_name = COALESCE(?, generic_name),
      dosage_form = COALESCE(?, dosage_form),
      unit_pack = COALESCE(?, unit_pack),
      reorder_level = COALESCE(?, reorder_level),
      rack_location = COALESCE(?, rack_location),
      is_prescription_required = COALESCE(?, is_prescription_required)
     WHERE medicine_id = ?`,
    [
      data.categoryId,
      data.manufacturerId,
      data.name,
      data.genericName,
      data.dosageForm,
      data.unitPack,
      data.reorderLevel,
      data.rackLocation,
      data.isPrescriptionRequired !== undefined ? (data.isPrescriptionRequired ? 1 : 0) : null,
      medicineId,
    ],
    username
  );
  return true;
}

async function deleteById(medicineId, username) {
  // Respect referential integrity. MySQL will throw ER_ROW_IS_REFERENCED_2 if historical sales exist.
  await query(`DELETE FROM medicine WHERE medicine_id = ?`, [medicineId], username);
  return true;
}

async function getCategories() {
  return await query(`SELECT category_id, name, description FROM category ORDER BY name ASC`);
}

async function getManufacturers() {
  return await query(`SELECT manufacturer_id, name, contact_email, phone, country FROM manufacturer ORDER BY name ASC`);
}

module.exports = {
  findAll,
  findById,
  create,
  update,
  deleteById,
  getCategories,
  getManufacturers,
};
