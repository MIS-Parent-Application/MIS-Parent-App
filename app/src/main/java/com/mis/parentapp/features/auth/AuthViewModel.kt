package com.mis.parentapp.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mis.parentapp.data.UserDAO
import com.mis.parentapp.network.LoginRequest
import com.mis.parentapp.network.LoginResponse
import com.mis.parentapp.network.RetrofitInstance
import kotlinx.coroutines.launch

class AuthViewModel(private val userDao: UserDAO) : ViewModel() {
    var currentSession: LoginResponse? = null
        private set

    fun signIn(
        username: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                currentSession = RetrofitInstance.api.login(
                    LoginRequest(
                        username = username.trim(),
                        password = pass
                    )
                )
                onSuccess()
            } catch (apiError: Exception) {
                val localUser = userDao.loginUser(username.trim(), pass)
                if (localUser != null) {
                    onSuccess()
                } else {
                    onError("Invalid login or server unavailable")
                }
            }
        }
    }

}
