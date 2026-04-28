package com.mis.parentapp.network

import retrofit2.http.GET

interface ApiService {

    @GET("api/parent/dashboard")
    suspend fun getDashboard(): ParentDashboard
}