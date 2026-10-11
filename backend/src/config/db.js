const mysql = require('mysql2/promise');
const config = require('./env');

const pool = mysql.createPool({
  host: config.db.host,
  port: config.db.port,
  user: config.db.user,
  password: config.db.password,
  database: config.db.database,
  waitForConnections: true,
  connectionLimit: config.db.connectionLimit,
  queueLimit: 0,
  decimalNumbers: true,
  dateStrings: true,
});

/**
 * Execute a query using a pooled connection with optional user context for triggers
 * @param {string} sql 
 * @param {Array} params 
 * @param {string} [username] 
 */
async function query(sql, params = [], username = null) {
  let conn;
  try {
    conn = await pool.getConnection();
    if (username) {
      await conn.query('SET @app_user = ?', [username]);
    }
    const [rows, fields] = await conn.query(sql, params);
    return rows;
  } finally {
    if (conn) {
      if (username) {
        await conn.query('SET @app_user = NULL').catch(() => {});
      }
      conn.release();
    }
  }
}

/**
 * Helper to execute inside a dedicated connection
 * @param {Function} callback 
 */
async function withConnection(callback) {
  const conn = await pool.getConnection();
  try {
    return await callback(conn);
  } finally {
    conn.release();
  }
}

module.exports = {
  pool,
  query,
  withConnection,
};
