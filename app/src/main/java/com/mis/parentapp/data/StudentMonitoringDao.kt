package com.mis.parentapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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
}