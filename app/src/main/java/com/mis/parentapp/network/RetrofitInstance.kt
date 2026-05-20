package com.mis.parentapp.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    //for wi-fi
//private const val BASE_URL = "http://192.168.1.248:3000/"

    //if using phone via usb
    //then run adb reverse tcp:3000 tcp:3000 in terminal
    const val BASE_URL = "http://127.0.0.1:3000/"

    fun resolveMediaUrl(url: String?): String? {
        val cleanUrl = url?.trim().orEmpty()
        if (cleanUrl.isBlank()) return null
        if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) return cleanUrl
        return BASE_URL.trimEnd('/') + "/" + cleanUrl.trimStart('/')
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
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
