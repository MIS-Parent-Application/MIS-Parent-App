package com.mis.parentapp.features.me

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
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
import com.mis.parentapp.network.ParentProfileUpdateRequest
import com.mis.parentapp.network.UpdateParentSecurityRequest
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
    var profileImageUrl by mutableStateOf<String?>(null)
    var backgroundImageUrl by mutableStateOf<String?>(null)
    var currentUsername: String? = null

    // Data Safety states
    var twoFactorEnabled by mutableStateOf(false)
    var loginAlertsEnabled by mutableStateOf(false)

    init {
        loadProfileData()
    }

    fun toggleTwoFactor(enabled: Boolean) {
        val previous = twoFactorEnabled
        twoFactorEnabled = enabled
        viewModelScope.launch {
            runCatching {
                RetrofitInstance.api.updateParentSecurity(
                    UpdateParentSecurityRequest(twoFactorEnabled = enabled)
                )
            }.onSuccess {
                twoFactorEnabled = it.twoFactorEnabled
            }.onFailure {
                twoFactorEnabled = previous
            }
        }
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
            // 1. Load from DB first
            try {
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
            } catch (e: Exception) {
                e.printStackTrace()
                // If the blob is too big, we might want to clear it to avoid future crashes
                if (e.message?.contains("Row too big", ignoreCase = true) == true) {
                    currentUsername?.let {
                        userDao.updateProfileImage(it, null, null)
                    }
                }
            }

            // 2. Load from API to update
            try {
                val dashboard = RetrofitInstance.api.getDashboard()
                // Update fields if they are null in DB or keep API as source of truth for name
                fullName = dashboard.parent.name
                email = dashboard.parent.email
                phoneNumber = dashboard.parent.phone
                profileImageUrl = dashboard.parent.profileImageUrl
                backgroundImageUrl = dashboard.parent.backgroundImageUrl ?: dashboard.parent.profileImageUrl
                
                isPrimaryGuardian = dashboard.parent.children.isNotEmpty()
                loadSecuritySettings()
                
                // Save basic info to DB if not present
                val dbUser = userDao.getCurrentUser()
                if (dbUser == null) {
                    currentUsername = dashboard.parent.id.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

    private suspend fun loadSecuritySettings() {
        runCatching {
            RetrofitInstance.api.getParentSecurity()
        }.onSuccess {
            twoFactorEnabled = it.twoFactorEnabled
            email = it.email
            phoneNumber = it.phone
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
            runCatching {
                RetrofitInstance.api.updateParentProfile(
                    ParentProfileUpdateRequest(
                        email = newEmail,
                        phone = newPhone
                    )
                )
            }.onSuccess {
                fullName = it.name
                email = it.email
                phoneNumber = it.phone
                profileImageUrl = it.profileImageUrl
                backgroundImageUrl = it.backgroundImageUrl ?: it.profileImageUrl
            }
        }
    }
    
    fun updateProfileImage(inputStream: InputStream?, uri: Uri?) {
        viewModelScope.launch {
            try {
                val rawBytes = inputStream?.use { it.readBytes() } ?: return@launch
                
                // 1. Decode with inSampleSize to avoid OOM if image is huge
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, options)
                
                val maxSize = 800 // Max dimension for profile pic
                var inSampleSize = 1
                if (options.outHeight > maxSize || options.outWidth > maxSize) {
                    val halfHeight = options.outHeight / 2
                    val halfWidth = options.outWidth / 2
                    while (halfHeight / inSampleSize >= maxSize && halfWidth / inSampleSize >= maxSize) {
                        inSampleSize *= 2
                    }
                }
                
                val decodeOptions = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                }
                val decodedBitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOptions) ?: return@launch
                
                // 2. Further resize to be more efficient if needed
                val scaledBitmap = if (decodedBitmap.width > maxSize || decodedBitmap.height > maxSize) {
                    val ratio = decodedBitmap.width.toFloat() / decodedBitmap.height.toFloat()
                    val width: Int
                    val height: Int
                    if (ratio > 1) {
                        width = maxSize
                        height = (maxSize / ratio).toInt()
                    } else {
                        height = maxSize
                        width = (maxSize * ratio).toInt()
                    }
                    Bitmap.createScaledBitmap(decodedBitmap, width, height, true)
                } else {
                    decodedBitmap
                }

                // 3. Compress to JPEG (significant size reduction)
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val compressedBytes = outputStream.toByteArray()
                
                profileBitmap = scaledBitmap.asImageBitmap()
                
                currentUsername?.let {
                    userDao.updateProfileImage(it, uri?.toString(), compressedBytes)
                }
                
                val mimeType = "image/jpeg" // We forced JPEG compression
                val base64 = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
                
                runCatching {
                    RetrofitInstance.api.updateParentProfile(
                        ParentProfileUpdateRequest(
                            email = email,
                            phone = phoneNumber,
                            profileImageData = base64,
                            profileImageMimeType = mimeType
                        )
                    )
                }.onSuccess {
                    profileImageUrl = it.profileImageUrl
                    backgroundImageUrl = it.backgroundImageUrl ?: it.profileImageUrl
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteProfileImage() {
        viewModelScope.launch {
            try {
                profileBitmap = null
                profileImageUrl = null
                
                currentUsername?.let {
                    userDao.updateProfileImage(it, null, null)
                }
                
                runCatching {
                    RetrofitInstance.api.updateParentProfile(
                        ParentProfileUpdateRequest(
                            email = email,
                            phone = phoneNumber,
                            profileImageData = "", // Empty string to indicate removal
                            profileImageMimeType = ""
                        )
                    )
                }.onSuccess {
                    profileImageUrl = it.profileImageUrl
                    backgroundImageUrl = it.backgroundImageUrl ?: it.profileImageUrl
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
