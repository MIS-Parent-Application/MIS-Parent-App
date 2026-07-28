package com.mis.parentapp.data

import android.util.Log
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.network.SupabaseInstance
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

sealed class LoginResult {
    data class Success(val user: UserEntity) : LoginResult()
    data class RequiresOtp(val otpToken: String, val email: String) : LoginResult()
}

class UserRepository(private val userDao: UserDAO) {
    
    suspend fun login(username: String, pass: String): Result<LoginResult> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Attempting login for $username")
            // 1. Initial login attempt with email and password
            withTimeout(20.seconds) {
                SupabaseInstance.client.auth.signInWith(Email) {
                    email = username
                    password = pass
                }
            }
            Log.d("UserRepository", "Password login successful")
            
            val session = SupabaseInstance.client.auth.currentSessionOrNull()
            val user = session?.user ?: throw Exception("Login failed: No session established")
            
            // 2. Fetch user security settings to check if 2FA is enabled
            Log.d("UserRepository", "Fetching security settings for ${user.id}")
            val securitySettings = try {
                withTimeout(10.seconds) {
                    RetrofitInstance.api.getParentSecurity(idFilter = "eq.${user.id}").firstOrNull()
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Security check failed/timed out", e)
                null
            }
            
            val is2FAEnabled = securitySettings?.twoFactorEnabled ?: false
            Log.d("UserRepository", "2FA Enabled: $is2FAEnabled")
            
            if (is2FAEnabled) {
                Log.d("UserRepository", "Triggering OTP for ${user.email}")
                
                // Thoroughly reset local auth state to ensure no session headers are sent
                SupabaseInstance.client.auth.signOut(SignOutScope.LOCAL)
                delay(500) // Small delay to allow state processing
                
                // 3. If 2FA is enabled, trigger an OTP to the user's email
                // We use a timeout here to prevent a total hang if Supabase/SMTP is slow
                try {
                    val targetEmail = user.email ?: username
                    Log.d("UserRepository", "Triggering OTP for $targetEmail with 30s timeout")
                    withTimeout(30.seconds) {
                        SupabaseInstance.client.auth.signInWith(OTP) {
                            email = targetEmail
                            createUser = false // Ensure we don't trigger signup flow
                        }
                    }
                } catch (e: Exception) {
                    Log.e("UserRepository", "OTP trigger failed", e)
                    val errorMessage = when (e) {
                        is TimeoutCancellationException -> "Verification code request timed out. Please try again."
                        is AuthRestException -> {
                            val detail = e.description ?: e.error ?: "Internal error"
                            "Code request failed: $detail (Code: ${e.errorCode ?: "500"})"
                        }
                        else -> "Could not send verification code: ${e.localizedMessage ?: "Unknown error"}"
                    }
                    throw Exception(errorMessage)
                }
                
                Log.d("UserRepository", "OTP triggered successfully")
                Result.success(LoginResult.RequiresOtp(otpToken = "active_session", email = user.email ?: username))
            } else {
                // 4. Regular login success
                Log.d("UserRepository", "Proceeding with regular login")
                val userEntity = saveLoggedInUser(
                    username = username,
                    pass = pass,
                    token = session.accessToken,
                    email = user.email,
                    fullName = user.userMetadata?.get("full_name")?.toString() ?: user.email,
                    twoFactorEnabled = is2FAEnabled,
                    loginAlertsEnabled = false // Default
                )
                Result.success(LoginResult.Success(userEntity))
            }

        } catch (e: Exception) {
            Log.e("UserRepository", "Login process failed", e)
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(username: String, pass: String, otpToken: String, code: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val emailToVerify = username.lowercase().trim()
        val codeToVerify = code.trim()
        try {
            Log.d("UserRepository", "Verifying OTP for $emailToVerify")
            // Verify the OTP code sent to the email
            // Use EMAIL as the catch-all type for 6-digit codes
            SupabaseInstance.client.auth.verifyEmailOtp(
                type = OtpType.Email.MAGIC_LINK,
                email = emailToVerify,
                token = codeToVerify
            )
            
            val session = SupabaseInstance.client.auth.currentSessionOrNull()
            val user = session?.user ?: throw Exception("Verification failed: Session not established")
            
            val userEntity = saveLoggedInUser(
                username = emailToVerify,
                pass = pass,
                token = session.accessToken,
                email = user.email,
                fullName = user.userMetadata?.get("full_name")?.toString() ?: user.email,
                twoFactorEnabled = true, // We are here because OTP was required
                loginAlertsEnabled = false // Default
            )
            Result.success(userEntity)
        } catch (e: Exception) {
            Log.e("UserRepository", "OTP verification failed for $emailToVerify", e)
            Result.failure(e)
        }
    }

    suspend fun resendOtp(username: String): Result<Unit> = withContext(Dispatchers.IO) {
        val emailToResend = username.lowercase().trim()
        try {
            // For login OTP resend, we should call signInWith(OTP) again
            Log.d("UserRepository", "Resending OTP to $emailToResend")
            
            // Clear local session state if any exists
            SupabaseInstance.client.auth.signOut(SignOutScope.LOCAL)
            
            withTimeout(30.seconds) {
                SupabaseInstance.client.auth.signInWith(OTP) {
                    email = emailToResend
                    createUser = false
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "OTP resend failed for $emailToResend", e)
            val errorMessage = when (e) {
                is AuthRestException -> e.description ?: e.error ?: "Internal error"
                else -> e.localizedMessage ?: "Unknown error"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    private suspend fun saveLoggedInUser(
        username: String,
        pass: String,
        token: String,
        email: String?,
        fullName: String?,
        twoFactorEnabled: Boolean = false,
        loginAlertsEnabled: Boolean = false
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
            sessionToken = token,
            twoFactorEnabled = twoFactorEnabled,
            loginAlertsEnabled = loginAlertsEnabled
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
