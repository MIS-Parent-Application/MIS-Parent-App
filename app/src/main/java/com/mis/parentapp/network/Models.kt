package com.mis.parentapp.network

data class ParentDashboard(
    val parent: Parent,
    val children: List<Child>,
    val unreadAnnouncements: Int,
    val upcomingEvents: List<String>
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
    val grade: String,
    val attendance: String,
    val gpa: Double,
    val pendingPayments: Int
)