package com.mis.parentapp.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mis.parentapp.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun signIn(username: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.login(username.trim(), pass)
            _isLoading.value = false

            result.onSuccess {
                onSuccess()
            }.onFailure { error ->
                onError(error.message ?: "Login failed")
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
