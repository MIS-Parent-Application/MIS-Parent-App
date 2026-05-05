package com.mis.parentapp.data

import kotlinx.coroutines.flow.Flow

class StudentsRepo(private val studentDao: StudentMonitoringDao) {
    fun getChildrenForParent(parentId: String): Flow<List<StudentWithSchedules>> {
        return studentDao.getStudentsForParent(parentId)
    }

    suspend fun seedDummyStudents(parentId: String) {
        val child1 = StudentEntity(
            studentId = "STU_001",
            parentId = parentId,
            name = "Alex Smith",
            course = "BS Computer Science",
            year = "3rd Year",
            attendanceScore = 0.98,
            gpa = 1.25,
            pendingPayment = 0.00,
            notificationCount = 2,
            profileImageRes = com.mis.parentapp.R.drawable.student_image
        )
        val schedule1 = listOf(
            SubjectScheduleEntity(studentId = "STU_001", subject = "Mobile Computing", room = "Lab 3", day = "Mon/Wed", time = "08:00 AM"),
            SubjectScheduleEntity(studentId = "STU_001", subject = "Ethics", room = "Room 102", day = "Fri", time = "10:00 AM")
        )

        val child2 = StudentEntity(
            studentId = "STU_002",
            parentId = parentId,
            name = "Jordan Smith",
            course = "BS Information Technology",
            year = "1st Year",
            attendanceScore = 0.92,
            gpa = 1.75,
            pendingPayment = 2500.50,
            notificationCount = 5,
            profileImageRes = com.mis.parentapp.R.drawable.student_image
        )
        val schedule2 = listOf(
            SubjectScheduleEntity(studentId = "STU_002", subject = "Intro to IT", room = "Lab 1", day = "Tue/Thu", time = "01:00 PM")
        )

        val child3 = StudentEntity(
            studentId = "STU_003",
            parentId = parentId,
            name = "Taylor Smith",
            course = "BS Sports Science",
            year = "2nd Year",
            attendanceScore = 0.85,
            gpa = 2.25,
            pendingPayment = 120.00,
            notificationCount = 1,
            profileImageRes = com.mis.parentapp.R.drawable.student_image
        )
        val schedule3 = listOf(
            SubjectScheduleEntity(studentId = "STU_003", subject = "Anatomy", room = "Gym 2", day = "Mon/Wed/Fri", time = "03:00 PM")
        )

        studentDao.insertStudent(child1)
        studentDao.insertSchedules(schedule1)

        studentDao.insertStudent(child2)
        studentDao.insertSchedules(schedule2)

        studentDao.insertStudent(child3)
        studentDao.insertSchedules(schedule3)
    }
}