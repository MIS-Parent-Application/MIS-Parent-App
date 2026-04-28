package com.mis.parentapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: UserEntity)

    // Updated query to check username instead of email
    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    suspend fun loginUser(username: String, password: String): UserEntity?
}