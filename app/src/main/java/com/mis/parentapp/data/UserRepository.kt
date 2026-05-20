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

            val newUser = UserEntity(username, pass)
            userDao.registerUser(newUser)
            Result.success(newUser)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        userDao.clearUsers()
    }
}
