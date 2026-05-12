package com.mis.parentapp.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mis.parentapp.data.UserDAO
import com.mis.parentapp.network.LoginRequest
import com.mis.parentapp.network.LoginResponse
import com.mis.parentapp.network.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.HttpException

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
            } catch (apiError: HttpException) {
                if (apiError.code() == 401) {
                    onError("Invalid username or password")
                } else {
                    onError("Login failed. Server returned ${apiError.code()}")
                }
            } catch (apiError: Exception) {
                onError("Cannot reach login server. Start the backend, then try again.")
            }
        }
    }

}
