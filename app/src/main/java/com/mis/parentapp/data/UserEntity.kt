package com.mis.parentapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val password: String,
    val note: String = "+",
    val fullName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val profileImageUri: String? = null
)