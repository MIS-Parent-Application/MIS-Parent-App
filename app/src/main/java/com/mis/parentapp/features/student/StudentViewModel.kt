package com.mis.parentapp.features.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mis.parentapp.data.AttendanceRecord
import com.mis.parentapp.data.CourseGrade
import com.mis.parentapp.data.StudentMonitoringDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudentViewModel(private val dao: StudentMonitoringDao) : ViewModel() {

    // 1. Automatically fetch and hold the latest grades
    val grades: StateFlow<List<CourseGrade>> = dao.getAllGrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Automatically fetch and hold attendance records
    val attendance: StateFlow<List<AttendanceRecord>> = dao.getAllAttendance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. Dynamically calculate the GPA (Total Points / Total Units)
    val gpa: StateFlow<Double> = dao.getAllGrades().map { gradesList ->
        if (gradesList.isEmpty()) return@map 0.0
        val totalUnits = gradesList.sumOf { it.units }
        if (totalUnits == 0) return@map 0.0

        val totalPoints = gradesList.sumOf { it.grade * it.units }
        // Rounds to 2 decimal places
        Math.round((totalPoints / totalUnits) * 100.0) / 100.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // 4. Functions for the UI to add new data
    fun addGrade(subject: String, units: Int, grade: Double) {
        viewModelScope.launch {
            dao.insertGrade(CourseGrade(subjectName = subject, units = units, grade = grade))
        }
    }

    fun addAttendance(date: String, status: String, reason: String? = null) {
        viewModelScope.launch {
            dao.insertAttendance(AttendanceRecord(date = date, status = status, reason = reason))
        }
    }
}

// 5. A Factory to help Compose build this ViewModel with the Room Database
class StudentViewModelFactory(private val dao: StudentMonitoringDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}