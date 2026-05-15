package com.mis.parentapp.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mis.parentapp.data.UserDAO
import com.mis.parentapp.data.UserRepository
import com.mis.parentapp.network.LoginRequest
import com.mis.parentapp.network.LoginResponse
import com.mis.parentapp.network.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel(private val repository: UserRepository) : ViewModel() {
    fun signIn(username: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.login(username.trim(), pass)

            result.onSuccess {
                onSuccess()
            }.onFailure { error ->
                onError(error.message ?: "Login failed")
            }
        }
    }
}
