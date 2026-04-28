package com.mis.parentapp.data


data class SubjectSchedule(
    val subject: String,
    val room: String,
    val day: String,
    val time: String
)


data class Student (
    val studentId: String,
    val parentId: String? = null,
    val name: String,
    val course: String,
    val year: String,
    val schedules: List<SubjectSchedule>,
    val attendanceScore: Double, //0.95 for 95%
    val gpa: Double,
    val pendingPayment: Double,
    val notificationCount: Int,
    val upcomingEvents: List<String> = emptyList(),
    val recentActivities: List<String> = emptyList()
)