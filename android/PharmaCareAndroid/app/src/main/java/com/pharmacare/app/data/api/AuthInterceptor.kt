package com.pharmacare.app.data.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    @Volatile
    var token: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()

        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        requestBuilder.addHeader("Accept", "application/json")
        return chain.proceed(requestBuilder.build())
    }
}
