package com.pharmacare.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreatePurchaseRequest(
    val supplierId: Int,
    val invoiceNumber: String,
    val purchaseDate: String? = null,
    val notes: String? = null,
    val items: List<PurchaseItemPayload>
)

@Serializable
data class PurchaseItemPayload(
    val medicineId: Int,
    val batchNumber: String,
    val expiryDate: String,
    val quantity: Int,
    val unitCost: Double,
    val sellingPrice: Double
)

@Serializable
data class CreatePurchaseResponse(
    val success: Boolean,
    val message: String? = null,
    val data: PurchaseResultData? = null,
    val error: String? = null
)

@Serializable
data class PurchaseResultData(
    val purchaseId: Int,
    val totalAmount: Double
)

@Serializable
data class SupplierItem(
    val supplier_id: Int,
    val name: String,
    val contact_person: String? = null,
    val phone: String,
    val gst_number: String? = null
)

@Serializable
data class SupplierListResponse(
    val success: Boolean,
    val data: List<SupplierItem> = emptyList()
)

@Serializable
data class PurchaseHistoryResponse(
    val success: Boolean,
    val count: Int = 0,
    val data: List<PurchaseHistoryItem> = emptyList()
)

@Serializable
data class PurchaseHistoryItem(
    val purchase_id: Int,
    val invoice_number: String,
    val supplier_id: Int,
    val supplier_name: String,
    val purchase_date: String,
    val total_amount: Double,
    val payment_status: String
)
