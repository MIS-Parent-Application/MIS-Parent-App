package com.mis.parentapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//added EventItem, incremented version to 3
@Database(
    entities = [
        UserEntity::class,
        CourseGrade::class,
        AttendanceRecord::class,
        EventItem::class,
        StudentEntity::class,
        SubjectScheduleEntity::class,
        FeedbackEntity::class
    ],
    version = 12, // Incremented version to include FeedbackEntity
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDAO
    abstract fun studentMonitoringDao(): StudentMonitoringDao
    abstract fun eventDao(): EventDao
    abstract fun feedbackDao(): FeedbackDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "parent_app_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
