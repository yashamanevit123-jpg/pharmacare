const app = require('./app');
const config = require('./config/env');
const { pool } = require('./config/db');

async function startServer() {
  try {
    // Validate database connection
    const conn = await pool.getConnection();
    const [rows] = await conn.query('SELECT VERSION() AS version, DATABASE() AS database_name');
    conn.release();

    console.log(`[Database] Connected successfully to MySQL ${rows[0].version} (Database: ${rows[0].database_name})`);

    const server = app.listen(config.port, () => {
      console.log(`================================================================`);
      console.log(` PharmaCare REST API & Web Dashboard Server Running`);
      console.log(` Environment : ${config.nodeEnv}`);
      console.log(` API Endpoint: http://localhost:${config.port}/api`);
      console.log(` Web Frontend: http://localhost:${config.port}/`);
      console.log(`================================================================`);
    });

    // Graceful shutdown handling
    const shutdown = async () => {
      console.log('\n[Server] Shutting down gracefully...');
      server.close(async () => {
        await pool.end();
        console.log('[Database] Connection pool closed.');
        process.exit(0);
      });
    };

    process.on('SIGTERM', shutdown);
    process.on('SIGINT', shutdown);

    return server;
  } catch (err) {
    console.error('[Startup Error] Failed to start server:', err.message);
    process.exit(1);
  }
}

if (require.main === module) {
  startServer();
}

module.exports = {
  startServer,
};
