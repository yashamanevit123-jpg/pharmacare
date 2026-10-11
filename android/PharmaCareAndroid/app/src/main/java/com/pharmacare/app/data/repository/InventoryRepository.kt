package com.pharmacare.app.data.repository

import com.pharmacare.app.data.api.RetrofitClient
import com.pharmacare.app.data.model.BatchItem
import com.pharmacare.app.data.model.CategoryItem
import com.pharmacare.app.data.model.MedicineItem

class InventoryRepository {
    private val api = RetrofitClient.apiService

    suspend fun getMedicines(
        search: String? = null,
        categoryId: Int? = null,
        status: String? = null
    ): Result<List<MedicineItem>> {
        return try {
            val response = RetrofitClient.apiService.getMedicines(search, categoryId, status)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val err = response.errorBody()?.string() ?: "Failed to fetch medicines (${response.code()})"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<CategoryItem>> {
        return try {
            val response = RetrofitClient.apiService.getCategories()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val err = response.errorBody()?.string() ?: "Failed to fetch categories"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailableBatches(medicineId: Int): Result<List<BatchItem>> {
        return try {
            val response = RetrofitClient.apiService.getAvailableBatches(medicineId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val err = response.errorBody()?.string() ?: "Failed to fetch batches"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
