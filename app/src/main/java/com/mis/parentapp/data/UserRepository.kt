package com.mis.parentapp.data

import com.mis.parentapp.network.LoginRequest
import com.mis.parentapp.network.LoginResponse
import com.mis.parentapp.network.ResendOtpRequest
import com.mis.parentapp.network.ResendOtpResponse
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.network.VerifyOtpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class LoginResult {
    data class Success(val user: UserEntity) : LoginResult()
    data class RequiresOtp(val otpToken: String, val email: String) : LoginResult()
}

class UserRepository(private val userDao: UserDAO) {
    suspend fun login(username: String, pass: String): Result<LoginResult> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitInstance.api.login(
                LoginRequest(username = username, password = pass)
            )
            if (response.requiresTwoFactor) {
                return@withContext Result.success(
                    LoginResult.RequiresOtp(
                        otpToken = response.otpToken.orEmpty(),
                        email = response.email.orEmpty()
                    )
                )
            }

            Result.success(LoginResult.Success(saveLoggedInUser(username, pass, response)))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(username: String, pass: String, otpToken: String, code: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitInstance.api.verifyOtp(
                VerifyOtpRequest(otpToken = otpToken, code = code)
            )
            Result.success(saveLoggedInUser(username, pass, response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendOtp(otpToken: String): Result<ResendOtpResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(RetrofitInstance.api.resendOtp(ResendOtpRequest(otpToken = otpToken)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveLoggedInUser(username: String, pass: String, response: LoginResponse): UserEntity {
        val currentTime = System.currentTimeMillis()
        val existingUser = userDao.getCurrentUser()
        return if (existingUser?.username == username) {
            userDao.updateLoginTime(username, currentTime)
            existingUser.copy(lastLoginTime = currentTime)
        } else {
            val parent = response.parent
            val newUser = UserEntity(
                username = username,
                password = pass,
                fullName = parent?.name,
                email = parent?.email,
                phoneNumber = parent?.phone,
                lastLoginTime = currentTime
            )
            userDao.registerUser(newUser)
            newUser
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
