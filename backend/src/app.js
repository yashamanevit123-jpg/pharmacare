const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const path = require('path');
const config = require('./config/env');
const { apiLimiter } = require('./middleware/rateLimiter');
const { errorHandler } = require('./middleware/errorHandler');

// Route imports
const authRoutes = require('./routes/auth.routes');
const medicineRoutes = require('./routes/medicine.routes');
const batchRoutes = require('./routes/batch.routes');
const saleRoutes = require('./routes/sale.routes');
const purchaseRoutes = require('./routes/purchase.routes');
const returnRoutes = require('./routes/return.routes');
const dashboardRoutes = require('./routes/dashboard.routes');
const reportRoutes = require('./routes/report.routes');
const customerRoutes = require('./routes/customer.routes');

const app = express();

// Security and utility middleware
app.use(helmet({
  contentSecurityPolicy: false, // Permit fonts and styles from Google CDN
}));
app.use(cors({ origin: config.corsOrigin }));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Apply rate limiting to all REST API endpoints
app.use('/api', apiLimiter);

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({
    status: 'UP',
    timestamp: new Date().toISOString(),
    service: 'PharmaCare Unified REST API',
    engine: 'MySQL 8.0 LTS + Express.js',
  });
});

// REST API routes
app.use('/api/auth', authRoutes);
app.use('/api/medicines', medicineRoutes);
app.use('/api/batches', batchRoutes);
app.use('/api/sales', saleRoutes);
app.use('/api/purchases', purchaseRoutes);
app.use('/api/returns', returnRoutes);
app.use('/api/dashboard', dashboardRoutes);
app.use('/api/reports', reportRoutes);
app.use('/api/customers', customerRoutes);

// Conveniences & Aliases matching PLAN.md endpoint specification
app.use('/api/categories', (req, res, next) => {
  req.url = '/categories';
  medicineRoutes(req, res, next);
});
app.use('/api/suppliers', (req, res, next) => {
  req.url = '/suppliers';
  purchaseRoutes(req, res, next);
});
app.use('/api/prescriptions', (req, res, next) => {
  req.url = '/prescriptions' + (req.url === '/' ? '' : req.url);
  customerRoutes(req, res, next);
});
app.use('/api/audit', (req, res, next) => {
  req.url = '/audit' + (req.url === '/' ? '' : req.url);
  reportRoutes(req, res, next);
});

// Static web files serving: serves the PharmaCare web dashboard locally
const webPath = path.resolve(__dirname, '../../web');
app.use(express.static(webPath));

// Fallback to index.html for root or client navigation
app.get('/', (req, res) => {
  res.sendFile(path.join(webPath, 'index.html'));
});

// 404 Handler for undefined API routes
app.use('/api/*', (req, res) => {
  res.status(404).json({
    success: false,
    error: `Endpoint '${req.method} ${req.baseUrl}' not found.`,
  });
});

// Centralized error handling middleware
app.use(errorHandler);

module.exports = app;
