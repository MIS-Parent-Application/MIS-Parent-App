package com.mis.parentapp.data

import com.mis.parentapp.network.LoginRequest
import com.mis.parentapp.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDAO) {
    suspend fun login(username: String, pass: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            RetrofitInstance.api.login(
                LoginRequest(username = username, password = pass)
            )

            val currentTime = System.currentTimeMillis()
            val existingUser = userDao.getCurrentUser()
            
            val user = if (existingUser?.username == username) {
                userDao.updateLoginTime(username, currentTime)
                existingUser.copy(lastLoginTime = currentTime)
            } else {
                val newUser = UserEntity(username, pass, lastLoginTime = currentTime)
                userDao.registerUser(newUser)
                newUser
            }
            
            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isUserLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.getCurrentUser() ?: return@withContext false
        val currentTime = System.currentTimeMillis()
        val seventyTwoHoursInMillis = 72 * 60 * 60 * 1000L

        if (currentTime - user.lastLoginTime > seventyTwoHoursInMillis) {
            userDao.clearUsers()
            false
        } else {
            true
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        userDao.clearUsers()
    }
}
