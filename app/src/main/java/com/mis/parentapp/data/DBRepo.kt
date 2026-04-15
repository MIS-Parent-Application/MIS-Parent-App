package com.mis.parentapp.data


object MockDataRepository {
    val dummyStudents = listOf(
        Student(
            studentId = "STU001",
            name = "Alice Johnson",
            course = "BS Computer Science",
            year = "3rd Year",
            schedules = listOf(
                SubjectSchedule("Mobile Dev", "Lab 4", "Mon/Wed", "10:00 AM"),
                SubjectSchedule("Ethics", "Room 302", "Tue/Thu", "1:00 PM")
            ),
            attendanceScore = 98.5,
            gpa = 3.8,
            pendingPayment = 0.0,
            notificationCount = 2
        ),
        Student(
            studentId = "STU002",
            name = "Bob Smith",
            course = "BS Information Technology",
            year = "2nd Year",
            schedules = listOf(
                SubjectSchedule("Database Systems", "Lab 1", "Fri", "8:00 AM")
            ),
            attendanceScore = 85.0,
            gpa = 3.2,
            pendingPayment = 1500.0,
            notificationCount = 5
        ),
        Student(
            studentId = "STU003",
            name = "Charlie Brown",
            course = "BS Software Engineering",
            year = "1st Year",
            schedules = listOf(
                SubjectSchedule("Intro to Programming", "Lab 2", "Mon-Fri", "3:00 PM")
            ),
            attendanceScore = 92.0,
            gpa = 3.5,
            pendingPayment = 500.0,
            notificationCount = 0
        )
    )
}