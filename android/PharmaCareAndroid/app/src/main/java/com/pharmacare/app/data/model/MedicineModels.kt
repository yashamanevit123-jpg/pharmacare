package com.pharmacare.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MedicineListResponse(
    val success: Boolean,
    val count: Int = 0,
    val data: List<MedicineItem> = emptyList()
)

@Serializable
data class MedicineItem(
    val medicine_id: Int,
    val name: String,
    val generic_name: String? = null,
    val dosage_form: String,
    val unit_pack: String,
    val reorder_level: Int,
    val rack_location: String,
    val is_prescription_required: Boolean = false,
    val category_id: Int,
    val category_name: String,
    val manufacturer_id: Int,
    val manufacturer_name: String,
    val total_stock: Int = 0,
    val min_price: Double? = null,
    val max_price: Double? = null,
    val batch_count: Int = 0
)

@Serializable
data class CategoryItem(
    val category_id: Int,
    val name: String,
    val description: String? = null
)

@Serializable
data class CategoryListResponse(
    val success: Boolean,
    val data: List<CategoryItem> = emptyList()
)
