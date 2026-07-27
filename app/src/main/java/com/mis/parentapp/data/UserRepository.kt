package com.mis.parentapp.data

import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.network.SupabaseInstance
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class LoginResult {
    data class Success(val user: UserEntity) : LoginResult()
    data class RequiresOtp(val otpToken: String, val email: String) : LoginResult()
}

class UserRepository(private val userDao: UserDAO) {
    
    suspend fun login(username: String, pass: String): Result<LoginResult> = withContext(Dispatchers.IO) {
        try {
            // Use Supabase Auth for sign in
            SupabaseInstance.client.auth.signInWith(Email) {
                email = username
                password = pass
            }
            
            val session = SupabaseInstance.client.auth.currentSessionOrNull()
            if (session != null) {
                val user = session.user
                val userEntity = saveLoggedInUser(
                    username = username,
                    pass = pass,
                    token = session.accessToken,
                    email = user?.email,
                    fullName = user?.userMetadata?.get("full_name")?.toString()
                )
                Result.success(LoginResult.Success(userEntity))
            } else {
                Result.failure(Exception("Login failed: No session established"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(username: String, pass: String, otpToken: String, code: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        // Implementation for OTP verification using Supabase can be added here
        // Supabase Auth supports OTP, but the current UI flow might need adjustments
        Result.failure(Exception("OTP verification via Supabase not yet implemented"))
    }

    suspend fun resendOtp(otpToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        // Implementation for resending OTP via Supabase
        Result.failure(Exception("Resend OTP via Supabase not yet implemented"))
    }

    private suspend fun saveLoggedInUser(
        username: String,
        pass: String,
        token: String,
        email: String?,
        fullName: String?
    ): UserEntity {
        val currentTime = System.currentTimeMillis()
        
        // Notify Networking layer of the new token
        RetrofitInstance.setAuthToken(token)
        
        val newUser = UserEntity(
            username = username,
            password = pass,
            fullName = fullName,
            email = email,
            phoneNumber = null, // Can be fetched from user metadata if available
            lastLoginTime = currentTime,
            sessionToken = token
        )
        
        userDao.clearUsers() // Clear previous sessions
        userDao.registerUser(newUser)
        return newUser
    }

    suspend fun isUserLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        // Check if there's a valid Supabase session
        val session = SupabaseInstance.client.auth.currentSessionOrNull()
        if (session != null) {
            RetrofitInstance.setAuthToken(session.accessToken)
            return@withContext true
        }

        // Fallback to local database if needed, but Supabase session is preferred
        val user = userDao.getCurrentUser() ?: return@withContext false
        
        if (user.sessionToken.isNullOrBlank()) {
            userDao.clearUsers()
            return@withContext false
        }
        
        RetrofitInstance.setAuthToken(user.sessionToken)
        true
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            SupabaseInstance.client.auth.signOut()
        } catch (_: Exception) {}
        
        RetrofitInstance.setAuthToken(null)
        userDao.clearUsers()
    }
}
