package com.pharmacare.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null,
    val user: UserInfo? = null,
    val error: String? = null
)

@Serializable
data class UserInfo(
    val employeeId: Int,
    val username: String,
    val fullName: String,
    val roleId: Int,
    val roleName: String
)
