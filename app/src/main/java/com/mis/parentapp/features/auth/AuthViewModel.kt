package com.mis.parentapp.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mis.parentapp.data.LoginResult
import com.mis.parentapp.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun signIn(
        username: String,
        pass: String,
        onSuccess: () -> Unit,
        onOtpRequired: (String, String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.login(username.trim(), pass)
            _isLoading.value = false

            result.onSuccess { loginResult ->
                when (loginResult) {
                    is LoginResult.Success -> onSuccess()
                    is LoginResult.RequiresOtp -> onOtpRequired(loginResult.otpToken, loginResult.email)
                }
            }.onFailure { error ->
                android.util.Log.e("AuthViewModel", "Sign in failed", error)
                val message = when {
                    error.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Connection timed out. Please check your internet or try again."
                    error.message?.contains("network", ignoreCase = true) == true ->
                        "Network error. Please check your connection."
                    else -> error.message ?: "Login failed"
                }
                onError(message)
            }
        }
    }

    fun verifyOtp(
        username: String,
        pass: String,
        otpToken: String,
        code: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.verifyOtp(username.trim(), pass, otpToken, code)
            _isLoading.value = false

            result.onSuccess {
                onSuccess()
            }.onFailure { error ->
                android.util.Log.e("AuthViewModel", "OTP verification failed", error)
                val message = if (error is io.github.jan.supabase.auth.exception.AuthRestException) {
                    when (error.error) {
                        "invalid_grant" -> "Invalid or expired code. Please try again."
                        "over_confirmation_rate_limit" -> "Too many attempts. Please wait a while."
                        else -> error.description ?: "Verification failed: ${error.error}"
                    }
                } else {
                    error.message ?: "Verification failed"
                }
                onError(message)
            }
        }
    }

    fun resendOtp(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.resendOtp(email)
            _isLoading.value = false

            result.onSuccess {
                onSuccess()
            }.onFailure { error ->
                onError(error.message ?: "Unable to resend verification code")
            }
        }
    }

    suspend fun isUserLoggedIn(): Boolean {
        return repository.isUserLoggedIn()
    }

    fun signOut(onSignOutComplete: () -> Unit) {
        viewModelScope.launch {
            repository.signOut()
            onSignOutComplete()
        }
    }
}
