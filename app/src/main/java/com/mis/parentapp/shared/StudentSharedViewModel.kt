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

    fun updateStudents(children: List<Child>) {
        students = children
        if (selectedStudent == null || children.none { it.id == selectedStudent?.id }) {
            selectedStudent = children.firstOrNull()
        }
    }

    fun selectStudent(student: Child) {
        selectedStudent = student
    }
}
