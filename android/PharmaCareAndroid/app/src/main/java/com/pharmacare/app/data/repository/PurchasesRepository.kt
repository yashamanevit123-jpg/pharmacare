package com.pharmacare.app.data.repository

import com.pharmacare.app.data.api.RetrofitClient
import com.pharmacare.app.data.model.CreatePurchaseRequest
import com.pharmacare.app.data.model.CreatePurchaseResponse
import com.pharmacare.app.data.model.PurchaseHistoryItem
import com.pharmacare.app.data.model.SupplierItem

class PurchasesRepository {
    suspend fun getSuppliers(): Result<List<SupplierItem>> {
        return try {
            val response = RetrofitClient.apiService.getSuppliers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val err = response.errorBody()?.string() ?: "Failed to fetch suppliers"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPurchase(request: CreatePurchaseRequest): Result<CreatePurchaseResponse> {
        return try {
            val response = RetrofitClient.apiService.createPurchase(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body.message ?: body.error ?: "Purchase record failed"))
                }
            } else {
                val err = response.errorBody()?.string() ?: "Purchase creation failed (${response.code()})"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPurchases(limit: Int = 50): Result<List<PurchaseHistoryItem>> {
        return try {
            val response = RetrofitClient.apiService.getPurchases(limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val err = response.errorBody()?.string() ?: "Failed to fetch purchase orders"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
