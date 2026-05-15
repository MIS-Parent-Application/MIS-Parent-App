package com.mis.parentapp.data

import com.mis.parentapp.network.LoginRequest
import com.mis.parentapp.network.LoginResponse
import com.mis.parentapp.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDAO) {
    companion object {
        //sample user for testing
        private const val SAMPLE_USER = "user"
        private const val SAMPLE_PASS = "pass"
    }

    suspend fun login(username: String, pass: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            //Room
            val localUser = userDao.loginUser(username, pass)
            if (localUser != null) return@withContext Result.success(localUser)
            if (username == SAMPLE_USER && pass == SAMPLE_PASS) {
                val mockUser = UserEntity(SAMPLE_USER, SAMPLE_PASS)
                userDao.registerUser(mockUser)
                return@withContext Result.success(mockUser)
            }

            //Retrofit - if Room doesn't exist
            val response = RetrofitInstance.api.login(
                LoginRequest(username = username, password = pass)
            )

            val newUser = UserEntity(username, pass)
            userDao.registerUser(newUser)
            Result.success(newUser)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}