const dotenv = require('dotenv');
const path = require('path');

dotenv.config({ path: path.resolve(__dirname, '../../.env') });

module.exports = {
  port: parseInt(process.env.PORT, 10) || 3000,
  nodeEnv: process.env.NODE_ENV || 'development',
  db: {
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT, 10) || 3306,
    user: process.env.DB_USER || 'pharmacare_user',
    password: process.env.DB_PASSWORD || 'PharmaCare@2026!',
    database: process.env.DB_NAME || 'pharmacare_db',
    connectionLimit: parseInt(process.env.DB_CONNECTION_LIMIT, 10) || 10,
  },
  jwt: {
    secret: process.env.JWT_SECRET || 'pharmacare_fallback_secret_key_2026',
    expiresIn: process.env.JWT_EXPIRES_IN || '8h',
  },
  corsOrigin: process.env.CLIENT_ORIGIN || '*',
};
