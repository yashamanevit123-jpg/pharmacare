package com.pharmacare.app.data.repository

import com.pharmacare.app.data.api.RetrofitClient
import com.pharmacare.app.data.model.LoginRequest
import com.pharmacare.app.data.model.LoginResponse
import com.pharmacare.app.data.model.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthRepository {
    private val apiService = RetrofitClient.apiService

    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = RetrofitClient.apiService.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.token != null) {
                    _token.value = body.token
                    _currentUser.value = body.user
                    RetrofitClient.setAuthToken(body.token)
                    Result.success(body)
                } else {
                    Result.failure(Exception(body.message ?: body.error ?: "Invalid credentials"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Login failed (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        _token.value = null
        _currentUser.value = null
        RetrofitClient.setAuthToken(null)
    }

    val isLoggedIn: Boolean
        get() = _token.value != null

    val userRole: String
        get() = _currentUser.value?.roleName ?: ""
}
