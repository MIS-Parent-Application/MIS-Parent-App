package com.mis.parentapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registerUser(user: UserEntity)

    // Updated query to check username instead of email
    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    suspend fun loginUser(username: String, password: String): UserEntity?

    @Query("UPDATE users SET note = :newNote WHERE username = :username")
    suspend fun updateUserNote(username: String, newNote: String)

    @Query("UPDATE users SET fullName = :name, email = :email, phoneNumber = :phone WHERE username = :username")
    suspend fun updateProfile(username: String, name: String, email: String, phone: String)

    @Query("UPDATE users SET profileImageUri = :uri WHERE username = :username")
    suspend fun updateProfileImage(username: String, uri: String?)

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("UPDATE users SET lastLoginTime = :time WHERE username = :username")
    suspend fun updateLoginTime(username: String, time: Long)

    @Query("DELETE FROM users")
    suspend fun clearUsers()

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun getUserFlow(username: String): kotlinx.coroutines.flow.Flow<UserEntity?>
}
