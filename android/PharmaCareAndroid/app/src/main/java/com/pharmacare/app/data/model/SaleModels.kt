package com.pharmacare.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateSaleRequest(
    val customerId: Int,
    val paymentMethod: String = "Cash",
    val discountAmount: Double = 0.0,
    val items: List<SaleItemPayload>
)

@Serializable
data class SaleItemPayload(
    val batchId: Int,
    val quantity: Int
)

@Serializable
data class CreateSaleResponse(
    val success: Boolean,
    val message: String? = null,
    val data: SaleResultData? = null,
    val error: String? = null
)

@Serializable
data class SaleResultData(
    val saleId: Int,
    val invoiceNumber: String,
    val totalAmount: Double
)

@Serializable
data class SaleHistoryResponse(
    val success: Boolean,
    val count: Int = 0,
    val data: List<SaleHistoryItem> = emptyList()
)

@Serializable
data class SaleHistoryItem(
    val sale_id: Int,
    val invoice_number: String,
    val customer_id: Int,
    val customer_name: String,
    val customer_phone: String? = null,
    val sale_date: String,
    val payment_method: String,
    val total_amount: Double,
    val status: String,
    val total_items: Int = 0
)

@Serializable
data class CustomerItem(
    val customer_id: Int,
    val name: String,
    val phone: String,
    val email: String? = null,
    val address: String? = null
)

@Serializable
data class CustomerListResponse(
    val success: Boolean,
    val count: Int = 0,
    val data: List<CustomerItem> = emptyList()
)
