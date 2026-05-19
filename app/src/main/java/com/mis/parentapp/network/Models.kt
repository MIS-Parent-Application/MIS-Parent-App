package com.mis.parentapp.network

data class ParentDashboard(
    val parent: Parent,
    val children: List<Child>,
    val unreadAnnouncements: Int,
    val upcomingEvents: List<String>
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val parent: Parent,
    val dashboard: ParentDashboard
)

data class Parent(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val children: List<Int>
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
    val schedules: List<ClassSchedule> = emptyList(),
    val studyLoad: List<StudyLoadSubject> = emptyList()
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
    val isNew: Boolean
)

data class CalendarEventDto(
    val id: Int,
    val studentId: Int?,
    val title: String,
    val category: String,
    val date: String,
    val time: String,
    val description: String,
    val status: String
)
