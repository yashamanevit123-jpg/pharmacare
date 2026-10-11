/**
 * PharmaCare Web API Client (api.js)
 * Wraps Fetch API requests to Express backend with JWT handling & error normalization.
 */

const API_BASE = '/api';

const api = {
  getToken() {
    return localStorage.getItem('pharmacare_token') || '';
  },

  setAuth(token, user) {
    localStorage.setItem('pharmacare_token', token);
    localStorage.setItem('pharmacare_user', JSON.stringify(user));
  },

  getUser() {
    try {
      return JSON.parse(localStorage.getItem('pharmacare_user')) || null;
    } catch {
      return null;
    }
  },

  clearAuth() {
    localStorage.removeItem('pharmacare_token');
    localStorage.removeItem('pharmacare_user');
  },

  async request(endpoint, options = {}) {
    const token = this.getToken();
    const headers = {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
      ...(options.headers || {}),
    };

    const res = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers,
    });

    const data = await res.json().catch(() => null);

    if (!res.ok) {
      const errorMsg = (data && data.error) ? data.error : `HTTP ${res.status}: Request failed`;
      const err = new Error(errorMsg);
      err.status = res.status;
      err.data = data;
      throw err;
    }

    return data;
  },

  // Auth
  async login(username, password) {
    const res = await this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
    if (res.token && res.user) {
      this.setAuth(res.token, res.user);
    }
    return res;
  },

  async getMe() {
    return await this.request('/auth/me');
  },

  // Dashboard
  async getDashboard() {
    const res = await this.request('/dashboard');
    return res.data;
  },

  // Medicines
  async getMedicines(params = {}) {
    const query = new URLSearchParams(params).toString();
    const res = await this.request(`/medicines${query ? `?${query}` : ''}`);
    return res.data;
  },

  async createMedicine(data) {
    const res = await this.request('/medicines', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    return res.data;
  },

  async deleteMedicine(id) {
    return await this.request(`/medicines/${id}`, { method: 'DELETE' });
  },

  async getCategories() {
    const res = await this.request('/medicines/categories');
    return res.data;
  },

  async getManufacturers() {
    const res = await this.request('/medicines/manufacturers');
    return res.data;
  },

  // Batches
  async getBatches(params = {}) {
    const query = new URLSearchParams(params).toString();
    const res = await this.request(`/batches${query ? `?${query}` : ''}`);
    return res.data;
  },

  async getAvailableBatches(medId) {
    const res = await this.request(`/batches/medicine/${medId}/available`);
    return res.data;
  },

  // Sales
  async getSales(params = {}) {
    const query = new URLSearchParams(params).toString();
    const res = await this.request(`/sales${query ? `?${query}` : ''}`);
    return res.data;
  },

  async getSale(id) {
    const res = await this.request(`/sales/${id}`);
    return res.data;
  },

  async createSale(payload) {
    return await this.request('/sales', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  // Purchases
  async getPurchases(params = {}) {
    const query = new URLSearchParams(params).toString();
    const res = await this.request(`/purchases${query ? `?${query}` : ''}`);
    return res.data;
  },

  async createPurchase(payload) {
    return await this.request('/purchases', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  async getSuppliers() {
    const res = await this.request('/purchases/suppliers');
    return res.data;
  },

  // Returns
  async getReturns(params = {}) {
    const query = new URLSearchParams(params).toString();
    const res = await this.request(`/returns${query ? `?${query}` : ''}`);
    return res.data;
  },

  async createReturn(payload) {
    return await this.request('/returns', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  // Customers & Doctors & Prescriptions
  async getCustomers(params = {}) {
    const query = new URLSearchParams(params).toString();
    const res = await this.request(`/customers${query ? `?${query}` : ''}`);
    return res.data;
  },

  async getDoctors() {
    const res = await this.request('/customers/doctors');
    return res.data;
  },

  async getPrescriptions(params = {}) {
    const query = new URLSearchParams(params).toString();
    const res = await this.request(`/customers/prescriptions${query ? `?${query}` : ''}`);
    return res.data;
  },

  async getPrescriptionDetails(id) {
    const res = await this.request(`/customers/prescriptions/${id}`);
    return res.data;
  },

  // Reports
  async getLowStock() {
    const res = await this.request('/reports/low-stock');
    return res.data;
  },

  async getExpiringBatches() {
    const res = await this.request('/reports/expiring-batches');
    return res.data;
  },

  async getMonthlySales() {
    const res = await this.request('/reports/monthly-sales');
    return res.data;
  },

  async getAudit(params = {}) {
    const query = new URLSearchParams(params).toString();
    const res = await this.request(`/reports/audit${query ? `?${query}` : ''}`);
    return res.data;
  },
};
