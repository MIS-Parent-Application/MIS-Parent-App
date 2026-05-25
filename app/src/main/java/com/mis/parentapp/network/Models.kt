package com.mis.parentapp.network

import com.google.gson.annotations.SerializedName

data class ParentDashboard(
    val parent: Parent,
    val children: List<Child>,
    val unreadAnnouncements: Int,
    val upcomingEvents: List<String>
)

data class AppVersionDto(
    @SerializedName("latestVersionCode")
    val versionCode: Int,

    @SerializedName("latestVersionName")
    val versionName: String,

    @SerializedName("remarks")
    val releaseNotes: String?,

    @SerializedName("downloadUrl")
    val apkUrl: String?
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String? = null,
    val parent: Parent? = null,
    val dashboard: ParentDashboard? = null,
    val requiresTwoFactor: Boolean = false,
    val otpToken: String? = null,
    val email: String? = null,
    val expiresAt: String? = null,
    val retryAfterSeconds: Int? = null
)

data class VerifyOtpRequest(
    val otpToken: String,
    val code: String
)

data class ResendOtpRequest(
    val otpToken: String
)

data class ResendOtpResponse(
    val otpToken: String,
    val email: String,
    val expiresAt: String,
    val retryAfterSeconds: Int
)

data class ParentSecuritySettingsDto(
    val parentId: Int,
    val email: String,
    val phone: String,
    val twoFactorEnabled: Boolean
)

data class UpdateParentSecurityRequest(
    val parentId: Int = 1,
    val twoFactorEnabled: Boolean
)

data class Parent(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val children: List<Int>,
    val profileImageUrl: String? = null,
    val backgroundImageUrl: String? = null
)

data class ParentProfileUpdateRequest(
    val parentId: Int = 1,
    val email: String? = null,
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val profileImageData: String? = null,
    val profileImageMimeType: String? = null
)

data class Child(
    val id: Int,
    val name: String,
    val rollNumber: String,
    val grade: String,
    val section: String,
    val program: String,
    val course: String,
    val year: String,
    val classTeacher: String,
    val attendance: String,
    val gpa: Double,
    val pendingPayments: Int,
    val notificationCount: Int = 0,
    val performancePercentage: Int = 0,
    val profileImageUrl: String? = null,
    val backgroundImageUrl: String? = null,
    val schedules: List<ClassSchedule> = emptyList(),
    val studyLoad: List<StudyLoadSubject> = emptyList()
)

data class StudentPhotoUpdateRequest(
    val profileImageUrl: String? = null,
    val backgroundImageUrl: String? = null,
    val profileImageData: String? = null,
    val profileImageMimeType: String? = null
)

data class ClassSchedule(
    val subject: String,
    val room: String,
    val instructor: String,
    val day: String,
    val startTime: String,
    val endTime: String
)

data class StudyLoadSubject(
    val code: String,
    val title: String,
    val units: Int,
    val instructor: String,
    val schedule: String,
    val room: String,
    val scheduleNumber: String = "",
    val courseNumber: String = code,
    val time: String = schedule,
    val days: String = "",
    val remarks: String = "",
    val semester: String = "2nd Sem.",
    val schoolYear: String = "S.Y. 2025-2026",
    val dateEnrolled: String = ""
)

data class NotificationDto(
    val id: Int,
    val studentId: Int?,
    val text: String,
    val type: String,
    val time: String,
    val category: String,
    val isNew: Boolean,
    val imageUrl: String? = null
)

data class CalendarEventDto(
    val id: Int,
    val title: String,
    val category: String,
    val date: String,
    val time: String,
    val description: String,
    val status: String,
    val imageUrl: String?
)

data class AnnouncementDto(
    val id: Int,
    val title: String,
    val content: String,
    val category: String,
    val urgent: Boolean,
    val imageUrl: String? = null
)

data class GradeDto(
    val id: Int,
    val studentId: Int,
    val subjectName: String,
    val units: Int,
    val grade: Double,
    val instructor: String,
    val remarks: String,
    val term: String
)

data class AcademicPerformanceDto(
    val id: Int,
    val studentId: Int,
    val type: String,
    val title: String,
    val subject: String,
    val teacher: String,
    val summary: String,
    val details: String,
    val criteria: String,
    val imageUrl: String?,
    val score: String?,
    val status: String,
    val assignedDate: String,
    val dueDate: String,
    val timeAgo: String,
    val isPositive: Boolean
)

data class AttendanceDto(
    val id: Int,
    val studentId: Int,
    val subjectName: String,
    val instructor: String,
    val presentDays: Int,
    val totalDays: Int,
    val lateDays: Int,
    val absentDays: Int
)

data class PaymentRecordDto(
    val id: Int,
    val studentId: Int,
    val invoiceNumber: String,
    val purchasedItem: String,
    val paymentOption: String,
    val paidDate: String,
    val totalAmount: Double,
    val pdfBreakdown: String,
    val status: String
)

data class CreatePaymentRequest(
    val invoiceNumber: String,
    val purchasedItem: String,
    val paymentOption: String,
    val paidDate: String,
    val totalAmount: Double,
    val pdfBreakdown: String,
    val status: String = "Paid"
)

data class FacultyContactDto(
    val facultyId: String,
    val name: String,
    val department: String,
    val email: String,
    val subject: String
)

data class ParentChatLoginRequest(
    val parentName: String
)

data class ParentChatLoginResponse(
    val status: String,
    val token: String,
    val parent_data: ParentChatData
)

data class ParentChatData(
    val userId: String,
    val parentName: String
)

data class ChatMessageDto(
    val id: Int? = null,
    val sender_id: String,
    val receiver_id: String,
    val message: String,
    val created_at: String? = null
)

data class ChatHistoryResponse(
    val status: String,
    val data: List<ChatMessageDto>
)

data class SendChatMessageRequest(
    val sender_id: String = "parent_1",
    val receiver_id: String,
    val message: String
)

data class FeedbackRequest(
    val userEmail: String?,
    val feedbackType: String,
    val message: String,
    val appVersion: String
)

data class FeedbackResponse(
    val success: Boolean,
    val message: String
)
