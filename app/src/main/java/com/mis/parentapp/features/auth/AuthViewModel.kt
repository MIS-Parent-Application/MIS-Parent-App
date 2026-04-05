package com.mis.parentapp.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mis.parentapp.data.UserDAO
import kotlinx.coroutines.launch

class AuthViewModel(private val userDao: UserDAO) : ViewModel() {

    fun signIn(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val user = userDao.loginUser(email, pass)
            if (user != null) {
                onSuccess()
            } else {
                onError("Invalid email or password")
            }
        }
    }


    fun signUp(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val newUser = com.mis.parentapp.data.UserEntity(email, pass)
                userDao.registerUser(newUser)
                onSuccess()
            } catch (e: Exception) {
                onError("Account already exists or database error.")
            }
        }
    }
}