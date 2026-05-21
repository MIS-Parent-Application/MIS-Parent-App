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
                onError(error.message ?: "Login failed")
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
                onError(error.message ?: "Verification failed")
            }
        }
    }

    fun resendOtp(
        otpToken: String,
        onSuccess: (String, Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.resendOtp(otpToken)
            _isLoading.value = false

            result.onSuccess { response ->
                onSuccess(response.otpToken, response.retryAfterSeconds)
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
