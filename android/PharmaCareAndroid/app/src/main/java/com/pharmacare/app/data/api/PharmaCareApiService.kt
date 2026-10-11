package com.pharmacare.app.data.api

import com.pharmacare.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface PharmaCareApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("dashboard")
    suspend fun getDashboard(): Response<DashboardResponse>

    @GET("medicines")
    suspend fun getMedicines(
        @Query("search") search: String? = null,
        @Query("categoryId") categoryId: Int? = null,
        @Query("status") status: String? = null
    ): Response<MedicineListResponse>

    @GET("medicines/categories")
    suspend fun getCategories(): Response<CategoryListResponse>

    @GET("batches/medicine/{medicineId}/available")
    suspend fun getAvailableBatches(
        @Path("medicineId") medicineId: Int
    ): Response<BatchListResponse>

    @GET("customers")
    suspend fun getCustomers(): Response<CustomerListResponse>

    @POST("sales")
    suspend fun createSale(
        @Body request: CreateSaleRequest
    ): Response<CreateSaleResponse>

    @GET("sales")
    suspend fun getSales(
        @Query("limit") limit: Int = 50
    ): Response<SaleHistoryResponse>

    @GET("purchases/suppliers")
    suspend fun getSuppliers(): Response<SupplierListResponse>

    @POST("purchases")
    suspend fun createPurchase(
        @Body request: CreatePurchaseRequest
    ): Response<CreatePurchaseResponse>

    @GET("purchases")
    suspend fun getPurchases(
        @Query("limit") limit: Int = 50
    ): Response<PurchaseHistoryResponse>

    @GET("customers/prescriptions")
    suspend fun getPrescriptions(): Response<PrescriptionListResponse>

    @GET("reports/monthly-sales")
    suspend fun getMonthlySales(): Response<MonthlySalesResponse>
}
