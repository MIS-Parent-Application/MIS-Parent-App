package com.mis.parentapp.network

import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/parent-login")
    suspend fun parentChatLogin(@Body request: ParentChatLoginRequest): ParentChatLoginResponse

    @GET("api/parent/dashboard")
    suspend fun getDashboard(): ParentDashboard

    @GET("api/notifications")
    suspend fun getNotifications(@Query("studentId") studentId: Int? = null): List<NotificationDto>

    @GET("api/calendar")
    suspend fun getCalendarEvents(@Query("studentId") studentId: Int? = null): List<CalendarEventDto>

    @GET("api/student/{id}/studyload")
    suspend fun getStudyLoad(@Path("id") studentId: Int): List<StudyLoadSubject>

    @GET("api/student/{id}/grades")
    suspend fun getStudentGrades(@Path("id") studentId: Int): List<GradeDto>

    @GET("api/student/{id}/academic-performance")
    suspend fun getAcademicPerformance(@Path("id") studentId: Int): List<AcademicPerformanceDto>

    @GET("api/student/{id}/attendance")
    suspend fun getStudentAttendance(@Path("id") studentId: Int): List<AttendanceDto>

    @GET("api/student/{id}/payments")
    suspend fun getStudentPayments(@Path("id") studentId: Int): List<PaymentRecordDto>

    @POST("api/student/{id}/payments")
    suspend fun createStudentPayment(
        @Path("id") studentId: Int,
        @Body request: CreatePaymentRequest
    ): PaymentRecordDto

    @GET("api/faculty")
    suspend fun getFacultyContacts(): List<FacultyContactDto>

    @GET("api/chat/history/{facultyId}")
    suspend fun getChatHistory(
        @Path("facultyId") facultyId: String,
        @Query("parentId") parentId: String = "parent_1"
    ): List<ChatMessageDto>

    @POST("api/chat/send")
    suspend fun sendChatMessage(@Body request: SendChatMessageRequest): ChatMessageDto

    @GET("api/announcements")
    suspend fun getAnnouncements(): List<AnnouncementDto>
}
