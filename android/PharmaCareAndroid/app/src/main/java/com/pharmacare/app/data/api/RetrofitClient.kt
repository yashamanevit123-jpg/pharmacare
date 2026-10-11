package com.pharmacare.app.data.api

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // 10.0.2.2 is the default Android emulator alias for host machine localhost
    var baseUrl: String = "http://10.0.2.2:3000/api/"
        set(value) {
            val formatted = if (value.endsWith("/")) value else "$value/"
            field = formatted
            rebuildService(formatted)
        }

    val authInterceptor = AuthInterceptor()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    @Volatile
    var apiService: PharmaCareApiService = createService(baseUrl)
        private set

    private fun createService(url: String): PharmaCareApiService {
        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
        return retrofit.create(PharmaCareApiService::class.java)
    }

    private fun rebuildService(url: String) {
        apiService = createService(url)
    }

    fun setAuthToken(token: String?) {
        authInterceptor.token = token
    }
}
