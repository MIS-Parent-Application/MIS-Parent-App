package com.mis.parentapp.network

import com.mis.parentapp.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    const val BASE_URL = BuildConfig.API_BASE_URL

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
                val newRequest = originalRequest.newBuilder()
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .build()
                chain.proceed(newRequest)
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
