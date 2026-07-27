package com.mis.parentapp.network

import android.content.Context
import com.mis.parentapp.BuildConfig
import com.mis.parentapp.data.AppDatabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    val BASE_URL = BuildConfig.API_BASE_URL
    
    // Static state to hold the current session token
    private var sessionToken: String? = null
    private var onUnauthorized: (() -> Unit)? = null

    fun setAuthToken(token: String?) {
        sessionToken = token
    }

    fun setUnauthorizedCallback(callback: () -> Unit) {
        onUnauthorized = callback
    }

    fun resolveMediaUrl(url: String?): String? {
        val cleanUrl = url?.trim().orEmpty()
        if (cleanUrl.isBlank()) return null
        if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) return cleanUrl
        return BASE_URL.trimEnd('/') + "/" + cleanUrl.trimStart('/')
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(90, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                
                // Automatically attach Bearer token if available
                // Priority: Local sessionToken, then Supabase session
                val tokenToUse = sessionToken ?: try {
                    SupabaseInstance.client.auth.currentSessionOrNull()?.accessToken
                } catch (e: Exception) {
                    null
                }

                tokenToUse?.let {
                    requestBuilder.header("Authorization", "Bearer $it")
                }
                
                val response = chain.proceed(requestBuilder.build())
                
                // If server returns 401, trigger the unauthorized callback
                if (response.code == 401) {
                    onUnauthorized?.invoke()
                }
                
                response
            }
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
