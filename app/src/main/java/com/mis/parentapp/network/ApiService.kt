package com.mis.parentapp.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Header

interface ApiService {

    @GET("rest/v1/app_versions")
    suspend fun getAppVersion(
        @Query("select") columns: String = "*"
    ): List<AppVersionDto>

    @GET("rest/v1/faculty")
    suspend fun getFacultyContacts(
        @Query("select") columns: String = "*"
    ): List<FacultyContactDto>

    @GET("rest/v1/payments")
    suspend fun getStudentPayments(
        @Query("student_id") idFilter: String,
        @Query("select") columns: String = "*"
    ): List<PaymentRecordDto>

    @POST("rest/v1/payments")
    suspend fun createStudentPayment(
        @Body request: CreatePaymentRequest,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<PaymentRecordDto>

    @GET("rest/v1/parents")
    suspend fun getParentProfile(
        @Query("id") idFilter: String,
        @Query("select") columns: String = "*"
    ): List<Parent>

    @GET("rest/v1/parents")
    suspend fun getParentSecurity(
        @Query("id") idFilter: String,
        @Query("select") columns: String = "id,email,phone,two_factor_enabled"
    ): List<ParentSecuritySettingsDto>

    @PATCH("rest/v1/parents")
    suspend fun updateParentSecurity(
        @Query("id") idFilter: String,
        @Body request: UpdateParentSecurityRequest,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<ParentSecuritySettingsDto>

    @PATCH("rest/v1/parents")
    suspend fun updateParentProfile(
        @Query("id") idFilter: String,
        @Body request: ParentProfileUpdateRequest,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<Parent>

    @GET("rest/v1/notifications")
    suspend fun getNotifications(
        @Query("student_id") idFilter: String? = null,
        @Query("select") columns: String = "*"
    ): List<NotificationDto>

    @GET("rest/v1/calendar_events")
    suspend fun getCalendarEvents(
        @Query("student_id") idFilter: String? = null,
        @Query("or") orFilter: String? = null,
        @Query("select") columns: String = "*"
    ): List<CalendarEventDto>

    @GET("rest/v1/study_load_subjects")
    suspend fun getStudyLoad(
        @Query("student_id") idFilter: String,
        @Query("select") columns: String = "*"
    ): List<StudyLoadSubject>

    @GET("rest/v1/academic_grades")
    suspend fun getStudentGrades(
        @Query("student_id") idFilter: String,
        @Query("select") columns: String = "*"
    ): List<GradeDto>

    @GET("rest/v1/academic_performance")
    suspend fun getAcademicPerformance(
        @Query("student_id") idFilter: String? = null,
        @Query("select") columns: String = "*"
    ): List<AcademicPerformanceDto>

    @GET("rest/v1/attendance_subjects")
    suspend fun getStudentAttendance(
        @Query("student_id") idFilter: String,
        @Query("select") columns: String = "*"
    ): List<AttendanceDto>

    @GET("rest/v1/announcements")
    suspend fun getAnnouncements(
        @Query("select") columns: String = "*"
    ): List<AnnouncementDto>

    @POST("rest/v1/parent_app_feedback")
    suspend fun submitFeedback(
        @Body request: FeedbackRequest,
        @Header("Prefer") prefer: String = "return=minimal"
    ): Response<Unit>

    // Support for children fetching
    @GET("rest/v1/parent_students")
    suspend fun getParentStudents(
        @Query("parent_id") parentIdFilter: String,
        @Query("select") columns: String = "students(*,class_schedules(*),study_load_subjects(*))"
    ): List<ParentStudentJunctionResponse>
}

data class ParentStudentJunctionResponse(
    val students: Child
)
