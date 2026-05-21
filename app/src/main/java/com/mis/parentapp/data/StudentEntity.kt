package com.mis.parentapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["username"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StudentEntity(
    @PrimaryKey val studentId: String,
    val parentId: String,
    val name: String,
    val course: String,
    val year: String,
    val attendanceScore: Double, //0.98 for 98%
    val gpa: Double,             //1.5
    val pendingPayment: Double,  //250.00
    val performanceScore: Int = 0, //0 to 100
    val notificationCount: Int,
    val profileImageRes: Int,
    val isPresent: Boolean = false
)