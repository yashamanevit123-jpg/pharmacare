package com.pharmacare.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PrescriptionListResponse(
    val success: Boolean,
    val count: Int = 0,
    val data: List<PrescriptionItemData> = emptyList()
)

@Serializable
data class PrescriptionItemData(
    val prescription_id: Int,
    val customer_id: Int,
    val customer_name: String,
    val doctor_id: Int,
    val doctor_name: String,
    val specialization: String,
    val hospital_clinic: String,
    val prescription_date: String,
    val diagnosis_notes: String? = null,
    val total_items: Int = 0
)

@Serializable
data class MonthlySalesResponse(
    val success: Boolean,
    val count: Int = 0,
    val data: List<MonthlySaleItem> = emptyList()
)

@Serializable
data class MonthlySaleItem(
    val sales_year: Int,
    val month_label: String,
    val total_invoices: Int,
    val gross_subtotal: Double,
    val total_discounts: Double,
    val net_revenue: Double,
    val average_invoice_value: Double
)
