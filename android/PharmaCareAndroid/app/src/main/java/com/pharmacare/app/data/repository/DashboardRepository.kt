package com.pharmacare.app.data.repository

import com.pharmacare.app.data.api.RetrofitClient
import com.pharmacare.app.data.model.DashboardData
import com.pharmacare.app.data.model.MonthlySaleItem
import com.pharmacare.app.data.model.PrescriptionItemData

class DashboardRepository {
    suspend fun getDashboard(): Result<DashboardData> {
        return try {
            val response = RetrofitClient.apiService.getDashboard()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Dashboard payload empty"))
                }
            } else {
                val err = response.errorBody()?.string() ?: "Failed to fetch dashboard metrics"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPrescriptions(): Result<List<PrescriptionItemData>> {
        return try {
            val response = RetrofitClient.apiService.getPrescriptions()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val err = response.errorBody()?.string() ?: "Failed to fetch prescriptions"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMonthlySales(): Result<List<MonthlySaleItem>> {
        return try {
            val response = RetrofitClient.apiService.getMonthlySales()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val err = response.errorBody()?.string() ?: "Failed to fetch monthly reports"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
