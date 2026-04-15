package com.mis.parentapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grades_table")
data class CourseGrade(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectName: String,
    val units: Int,
    val grade: Double // e.g., 1.0, 1.5, or 95.0 depending on your school's system
)

@Entity(tableName = "attendance_table")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // e.g., "2026-04-16"
    val status: String, // e.g., "Present", "Absent", "Late"
    val reason: String? = null // Optional excuse note for absences
)