package com.mis.parentapp.data

import androidx.room.Embedded
import androidx.room.Relation

data class StudentWithSchedules(
    @Embedded val student: StudentEntity,
    @Relation(
        parentColumn = "studentId",
        entityColumn = "studentId"
    )
    val schedules: List<SubjectScheduleEntity>
)