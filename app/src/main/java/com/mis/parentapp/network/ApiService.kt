package com.mis.parentapp.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/parent/dashboard")
    suspend fun getDashboard(): ParentDashboard

    @GET("api/notifications")
    suspend fun getNotifications(@Query("studentId") studentId: Int? = null): List<NotificationDto>

    @GET("api/calendar")
    suspend fun getCalendarEvents(@Query("studentId") studentId: Int? = null): List<CalendarEventDto>

    @GET("api/student/{id}/studyload")
    suspend fun getStudyLoad(@Path("id") studentId: Int): List<StudyLoadSubject>
}
