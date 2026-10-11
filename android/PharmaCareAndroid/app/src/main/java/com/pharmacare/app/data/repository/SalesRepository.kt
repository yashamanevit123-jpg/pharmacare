package com.pharmacare.app.data.repository

import com.pharmacare.app.data.api.RetrofitClient
import com.pharmacare.app.data.model.CreateSaleRequest
import com.pharmacare.app.data.model.CreateSaleResponse
import com.pharmacare.app.data.model.CustomerItem
import com.pharmacare.app.data.model.SaleHistoryItem

class SalesRepository {
    suspend fun getCustomers(): Result<List<CustomerItem>> {
        return try {
            val response = RetrofitClient.apiService.getCustomers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val err = response.errorBody()?.string() ?: "Failed to fetch customers"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSale(request: CreateSaleRequest): Result<CreateSaleResponse> {
        return try {
            val response = RetrofitClient.apiService.createSale(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body.message ?: body.error ?: "Sale creation failed"))
                }
            } else {
                val err = response.errorBody()?.string() ?: "Sale checkout failed (${response.code()})"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSales(limit: Int = 50): Result<List<SaleHistoryItem>> {
        return try {
            val response = RetrofitClient.apiService.getSales(limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val err = response.errorBody()?.string() ?: "Failed to fetch sales history"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
