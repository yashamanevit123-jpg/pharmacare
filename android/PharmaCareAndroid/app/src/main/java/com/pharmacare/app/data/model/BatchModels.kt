package com.pharmacare.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BatchListResponse(
    val success: Boolean,
    val count: Int = 0,
    val data: List<BatchItem> = emptyList()
)

@Serializable
data class BatchItem(
    val batch_id: Int,
    val medicine_id: Int,
    val medicine_name: String? = null,
    val batch_number: String,
    val manufacturing_date: String? = null,
    val expiry_date: String,
    val purchase_price: Double = 0.0,
    val selling_price: Double = 0.0,
    val quantity: Int = 0,
    val status: String = "Available",
    val computed_status: String? = null,
    val days_remaining: Int? = null
)
