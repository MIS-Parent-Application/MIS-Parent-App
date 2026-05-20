package com.mis.parentapp.features.me

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mis.parentapp.R
import com.mis.parentapp.data.AppDatabase
import com.mis.parentapp.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val apiService = RetrofitInstance.api
    var fullName by mutableStateOf("Nathaniel B. McClure")
    var email by mutableStateOf("nathaniel.mcclure@example.com")
    var phoneNumber by mutableStateOf("+63 912 345 6789")
    var isPrimaryGuardian by mutableStateOf(true)
    
    var profileImageRes by mutableStateOf(R.drawable.parent_pic)
    var profileBitmap by mutableStateOf<ImageBitmap?>(null)
    var currentUsername: String? = null

    // Data Safety states
    var twoFactorEnabled by mutableStateOf(false)
    var loginAlertsEnabled by mutableStateOf(false)

    private val _twoFAState = MutableStateFlow<TwoFAState>(TwoFAState.Idle)
    val twoFAState: StateFlow<TwoFAState> = _twoFAState

    sealed class TwoFAState {
        object Idle : TwoFAState()
        object SendingCode : TwoFAState()
        object CodeSent : TwoFAState()
        object Verifying : TwoFAState()
        object Success : TwoFAState()
        data class Error(val message: String) : TwoFAState()
    }

    fun sendTwoFACode(userId: String, email: String) {
        viewModelScope.launch {
            _twoFAState.value = TwoFAState.SendingCode
            try {
                val response = apiService.send2FACode(mapOf("userId" to userId, "email" to email))
                if (response.isSuccessful) {
                    _twoFAState.value = TwoFAState.CodeSent
                } else {
                    _twoFAState.value = TwoFAState.Error("Failed to send code")
                }
            } catch (e: Exception) {
                _twoFAState.value = TwoFAState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun verifyAndToggleTwoFA(userId: String, code: String, enable: Boolean) {
        viewModelScope.launch {
            _twoFAState.value = TwoFAState.Verifying
            try {
                val verifyResponse = apiService.verify2FACode(
                    mapOf(
                        "userId" to userId,
                        "code" to code
                    )
                )
                if (verifyResponse.isSuccessful) {
                    // Code verified, now toggle 2FA
                    val toggleResponse = apiService.toggle2FA(
                        mapOf(
                            "userId" to userId,
                            "enable" to enable
                        )
                    )
                    if (toggleResponse.isSuccessful) {
                        // Update UI switch state
                        twoFactorEnabled = enable
                        _twoFAState.value = TwoFAState.Success
                    } else {
                        _twoFAState.value =
                            TwoFAState.Error("Failed to update 2FA")
                    }

                } else {
                    _twoFAState.value =
                        TwoFAState.Error("Invalid or expired code")
                }
            } catch (e: Exception) {
                _twoFAState.value =
                    TwoFAState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun reset2FAState() {
        _twoFAState.value = TwoFAState.Idle
    }
//

    init {
        loadProfileData()
    }

    fun toggleTwoFactor(enabled: Boolean) {
        twoFactorEnabled = enabled
    }

    fun toggleLoginAlerts(enabled: Boolean) {
        loginAlertsEnabled = enabled
    }

    fun requestDataExport() {
        // Logic for requesting data export
        // For now, just a mock action
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            try {
                // 1. Load from DB first
                val dbUser = userDao.getCurrentUser()
                dbUser?.let {
                    currentUsername = it.username
                    if (it.fullName != null) fullName = it.fullName
                    if (it.email != null) email = it.email
                    if (it.phoneNumber != null) phoneNumber = it.phoneNumber
                    if (it.profileImageBlob != null) {
                        val bitmap = BitmapFactory.decodeByteArray(it.profileImageBlob, 0, it.profileImageBlob.size)
                        profileBitmap = bitmap?.asImageBitmap()
                    } else if (it.profileImageUri != null) {
                        loadBitmapFromUri(Uri.parse(it.profileImageUri))
                    }
                }

                // 2. Load from API to update
                val dashboard = RetrofitInstance.api.getDashboard()
                // Update fields if they are null in DB or keep API as source of truth for name
                if (dbUser?.fullName == null) fullName = dashboard.parent.name
                if (dbUser?.email == null) email = dashboard.parent.email
                if (dbUser?.phoneNumber == null) phoneNumber = dashboard.parent.phone
                
                isPrimaryGuardian = dashboard.parent.children.isNotEmpty()
                
                // Save basic info to DB if not present
                if (dbUser == null) {
                    currentUsername = dashboard.parent.id.toString()
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun loadBitmapFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                profileBitmap = bitmap?.asImageBitmap()
            } catch (e: Exception) {
            }
        }
    }

    fun updateProfile(newName: String, newEmail: String, newPhone: String) {
        fullName = newName
        email = newEmail
        phoneNumber = newPhone
        
        viewModelScope.launch {
            currentUsername?.let {
                userDao.updateProfile(it, newName, newEmail, newPhone)
            }
        }
    }
    
    fun updateProfileImage(inputStream: InputStream?, uri: Uri?) {
        viewModelScope.launch {
            try {
                val bytes = inputStream?.readBytes()
                val bitmap = if (bytes != null) BitmapFactory.decodeByteArray(bytes, 0, bytes.size) else null
                profileBitmap = bitmap?.asImageBitmap()
                
                currentUsername?.let {
                    userDao.updateProfileImage(it, uri?.toString(), bytes)
                }
            } catch (e: Exception) {
            }
        }
    }
}
