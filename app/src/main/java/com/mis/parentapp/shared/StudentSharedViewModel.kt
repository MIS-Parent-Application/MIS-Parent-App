package com.mis.parentapp.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mis.parentapp.network.Child

class StudentSharedViewModel : ViewModel() {
    var students by mutableStateOf<List<Child>>(emptyList())
        private set

    var selectedStudent by mutableStateOf<Child?>(null)
        private set

    var unreadAnnouncements by mutableStateOf(0)
        private set

    private val clearedStudentIds = mutableSetOf<Int>()

    fun updateStudents(children: List<Child>, unread: Int = 0) {
        students = children.map { child ->
            if (clearedStudentIds.contains(child.id)) {
                child.copy(notificationCount = 0)
            } else {
                child
            }
        }

        selectedStudent = selectedStudent?.let { current ->
            students.find { it.id == current.id }
        } ?: students.firstOrNull()

        unreadAnnouncements = selectedStudent?.notificationCount ?: unread
    }

    fun selectStudent(student: Child) {
        selectedStudent = student
        unreadAnnouncements = student.notificationCount
    }

    fun clearNotifications() {
        val currentId = selectedStudent?.id ?: return
        clearedStudentIds.add(currentId)
        students = students.map {
            if (it.id == currentId) it.copy(notificationCount = 0) else it
        }
        selectedStudent = students.find { it.id == currentId }
        unreadAnnouncements = 0
    }
}
