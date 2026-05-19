package com.mis.parentapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentMonitoringDao {

    // -- Grades --
    @Query("SELECT * FROM grades_table")
    fun getAllGrades(): Flow<List<CourseGrade>>

    @Insert
    suspend fun insertGrade(grade: CourseGrade)

    // -- Attendance --
    @Query("SELECT * FROM attendance_table ORDER BY date DESC")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Insert
    suspend fun insertAttendance(record: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<SubjectScheduleEntity>)

    @Transaction
    @Query("SELECT * FROM students WHERE parentId = :parentId")
    fun getStudentsForParent(parentId: String): Flow<List<StudentWithSchedules>>

    @Transaction
    @Query("SELECT * FROM students WHERE studentId = :studentId")
    suspend fun getStudentById(studentId: String): StudentWithSchedules?

    // Helper to clear and re-seed if necessary
    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()
}