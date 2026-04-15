package com.mis.parentapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// CHANGE 1: Added CourseGrade and AttendanceRecord, changed version to 2
@Database(entities = [UserEntity::class, CourseGrade::class, AttendanceRecord::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDAO

    // CHANGE 2: Added the Student Monitoring DAO
    abstract fun studentMonitoringDao(): StudentMonitoringDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "parent_app_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}