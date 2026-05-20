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
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()

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
