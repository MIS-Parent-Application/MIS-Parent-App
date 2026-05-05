package com.mis.parentapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<SubjectScheduleEntity>)

    // Inserts a student and their schedule in a single atomic transaction
    @Transaction
    suspend fun insertStudentWithSchedules(student: StudentEntity, schedules: List<SubjectScheduleEntity>) {
        insertStudent(student)
        insertSchedules(schedules)
    }

    // Fetches all students belonging to the specific parent with schedules
    @Transaction
    @Query("SELECT * FROM students WHERE parentId = :parentId")
    fun getStudentsForParent(parentId: String): Flow<List<StudentWithSchedules>>

    // Fetches a single student's quick stats
    @Transaction
    @Query("SELECT * FROM students WHERE studentId = :studentId")
    suspend fun getStudentById(studentId: String): StudentWithSchedules?
}