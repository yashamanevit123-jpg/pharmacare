package com.pharmacare.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DashboardResponse(
    val success: Boolean,
    val data: DashboardData? = null
)

@Serializable
data class DashboardData(
    val metrics: DashboardMetrics,
    val lowStockAlerts: List<LowStockAlertItem> = emptyList(),
    val expiringAlerts: List<ExpiringAlertItem> = emptyList()
)

@Serializable
data class DashboardMetrics(
    val totalMedicines: Int = 0,
    val totalSellableStock: Int = 0,
    val lowStockCount: Int = 0,
    val expiringBatchesCount: Int = 0,
    val todaySalesRevenue: Double = 0.0,
    val todaySalesCount: Int = 0,
    val monthlySalesRevenue: Double = 0.0,
    val supplierCount: Int = 0,
    val totalInventoryValue: Double = 0.0
)

@Serializable
data class LowStockAlertItem(
    val medicine_id: Int,
    val medicine_name: String,
    val category_name: String,
    val batch_quantity: Int,
    val rack_location: String
)

@Serializable
data class ExpiringAlertItem(
    val medicine_id: Int,
    val medicine_name: String,
    val batch_number: String,
    val quantity: Int,
    val expiry_date: String,
    val days_remaining: Int? = null,
    val expiry_status: String
)
