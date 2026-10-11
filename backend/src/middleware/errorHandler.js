const config = require('../config/env');

function errorHandler(err, req, res, next) {
  // Log error server-side
  console.error(`[Error] ${req.method} ${req.url}:`, err.message || err);

  // MySQL Specific Error Handling
  if (err.errno || err.sqlState) {
    // Custom SIGNAL SQLSTATE '45000' (Business Rule or Rollback from stored procedures/triggers)
    if (err.sqlState === '45000' || err.errno === 1644) {
      return res.status(400).json({
        success: false,
        error: err.sqlMessage || err.message || 'Database business rule violation.',
      });
    }

    // Duplicate unique key violation (ER_DUP_ENTRY)
    if (err.errno === 1062) {
      return res.status(409).json({
        success: false,
        error: 'Conflict: A record with duplicate unique identifier already exists.',
        details: config.nodeEnv === 'development' ? err.sqlMessage : undefined,
      });
    }

    // Foreign key violation on insert/update (ER_NO_REFERENCED_ROW_2)
    if (err.errno === 1452) {
      return res.status(400).json({
        success: false,
        error: 'Foreign Key Violation: Referenced entity does not exist.',
      });
    }

    // Foreign key constraint on delete (ER_ROW_IS_REFERENCED_2)
    if (err.errno === 1451) {
      return res.status(409).json({
        success: false,
        error: 'Cannot delete record: It is actively referenced by existing transactions.',
      });
    }

    // Check constraint violation (ER_CHECK_CONSTRAINT_VIOLATED)
    if (err.errno === 3819) {
      return res.status(400).json({
        success: false,
        error: 'Integrity Constraint Error: Data violates database check constraints (e.g. negative stock or price).',
      });
    }
  }

  // Handle explicitly structured HTTP errors
  const statusCode = err.statusCode || 500;
  const message = err.message || 'An unexpected internal server error occurred.';

  res.status(statusCode).json({
    success: false,
    error: message,
    ...(config.nodeEnv === 'development' && { stack: err.stack }),
  });
}

module.exports = {
  errorHandler,
};
