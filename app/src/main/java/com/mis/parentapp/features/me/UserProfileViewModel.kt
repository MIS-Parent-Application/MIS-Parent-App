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
import com.mis.parentapp.network.SupabaseInstance
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    var actualParentId: String = ""
        private set

    // Data Safety states
    var twoFactorEnabled by mutableStateOf(false)
    var loginAlertsEnabled by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private fun getSafeParentId(): String? {
        if (actualParentId.isNotBlank() && actualParentId != "server" && actualParentId != "null") return actualParentId
        val id = RetrofitInstance.tryGetSessionId()
        if (id != null && id.isNotBlank()) {
            actualParentId = id
            return id
        }
        return null
    }

    init {
        getSafeParentId()
        loadProfileData()
    }

    fun toggleTwoFactor(enabled: Boolean) {
        val parentId = getSafeParentId()
        if (parentId == null || parentId.isBlank() || parentId == "server" || parentId == "null") {
            errorMessage = "Update failed: No active session found. Please re-login."
            return
        }
        
        // Basic UUID format check
        if (!parentId.contains("-")) {
            errorMessage = "Update failed: Invalid user identifier."
            return
        }

        val previous = twoFactorEnabled
        twoFactorEnabled = enabled
        errorMessage = null
        viewModelScope.launch {
            try {
                val responseList = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.updateParentSecurity(
                        idFilter = "eq.$parentId",
                        request = UpdateParentSecurityRequest(
                            twoFactorEnabled = enabled
                        )
                    )
                }
                
                val result = responseList.firstOrNull()
                if (result != null) {
                    twoFactorEnabled = result.twoFactorEnabled
                    // Update local DB
                    currentUsername?.let { username ->
                        viewModelScope.launch(Dispatchers.IO) {
                            userDao.updateSecuritySettings(username, twoFactorEnabled, loginAlertsEnabled)
                        }
                    }
                } else {
                    twoFactorEnabled = previous
                    errorMessage = "Update failed: Profile record not found in database. Please contact support or run backfill script."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val serverError = if (e is retrofit2.HttpException) {
                    try {
                        val errorBody = e.response()?.errorBody()?.string()
                        val json = com.google.gson.Gson().fromJson(errorBody, com.google.gson.JsonObject::class.java)
                        // Postgrest errors usually have 'message', 'details', or 'hint'
                        json.get("message")?.asString 
                            ?: json.get("details")?.asString 
                            ?: json.get("error")?.asString 
                            ?: "HTTP ${e.code()}"
                    } catch (_: Exception) {
                        "Server error ${e.code()}"
                    }
                } else {
                    e.localizedMessage ?: "Connection error"
                }
                errorMessage = "Failed to update 2FA: $serverError"
                twoFactorEnabled = previous
            }
        }
    }

    fun toggleLoginAlerts(enabled: Boolean) {
        loginAlertsEnabled = enabled
        viewModelScope.launch(Dispatchers.IO) {
            currentUsername?.let {
                userDao.updateSecuritySettings(it, twoFactorEnabled, loginAlertsEnabled)
            }
        }
    }

    fun refresh() {
        actualParentId = ""
        getSafeParentId()
        loadProfileData()
    }

    fun requestDataExport() {
        // Logic for requesting data export
        // For now, just a mock action
    }

    private fun loadProfileData() {
        if (currentUsername != null && actualParentId.isNotBlank()) return
        
        val parentId = getSafeParentId()
        
        viewModelScope.launch {
            // 1. Load from DB first
            try {
                val dbUser = withContext(Dispatchers.IO) { userDao.getCurrentUser() }
                dbUser?.let {
                    currentUsername = it.username
                    twoFactorEnabled = it.twoFactorEnabled
                    loginAlertsEnabled = it.loginAlertsEnabled
                    if (it.fullName != null) fullName = it.fullName
                    if (it.email != null) email = it.email
                    if (it.phoneNumber != null) phoneNumber = it.phoneNumber
                    if (it.profileImageBlob != null) {
                        val bitmap = withContext(Dispatchers.IO) {
                            BitmapFactory.decodeByteArray(it.profileImageBlob, 0, it.profileImageBlob.size)
                        }
                        profileBitmap = bitmap?.asImageBitmap()
                    } else if (it.profileImageUri != null) {
                        loadBitmapFromUri(Uri.parse(it.profileImageUri))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // If the blob is too big, we might want to clear it to avoid future crashes
                if (e.message?.contains("Row too big", ignoreCase = true) == true) {
                    currentUsername?.let { username ->
                        viewModelScope.launch(Dispatchers.IO) {
                            userDao.updateProfileImage(username, null, null)
                        }
                    }
                }
            }

            if (parentId == null) return@launch

            // 2. Load from API to update
            try {
                val parents = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getParentProfile(idFilter = "eq.$parentId")
                }
                val parentRow = parents.firstOrNull()
                
                if (parentRow != null) {
                    fullName = parentRow.name
                    email = parentRow.email
                    phoneNumber = parentRow.phone
                    profileImageUrl = parentRow.profileImageUrl
                    backgroundImageUrl = parentRow.backgroundImageUrl ?: parentRow.profileImageUrl
                    
                    isPrimaryGuardian = parentRow.children.isNotEmpty()
                    loadSecuritySettings()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadBitmapFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                    BitmapFactory.decodeStream(inputStream)
                }
                profileBitmap = bitmap?.asImageBitmap()
            } catch (e: Exception) {
            }
        }
    }

    private suspend fun loadSecuritySettings() {
        val parentId = getSafeParentId() ?: return
        runCatching {
            withContext(Dispatchers.IO) {
                RetrofitInstance.api.getParentSecurity(idFilter = "eq.$parentId")
            }
        }.onSuccess { list ->
            list.firstOrNull()?.let {
                twoFactorEnabled = it.twoFactorEnabled
                email = it.email
                phoneNumber = it.phone
                // Keep local DB in sync
                currentUsername?.let { username ->
                    viewModelScope.launch(Dispatchers.IO) {
                        userDao.updateSecuritySettings(username, it.twoFactorEnabled, loginAlertsEnabled)
                    }
                }
            }
        }
    }

    fun updateProfile(newName: String, newEmail: String, newPhone: String) {
        val parentId = getSafeParentId()
        fullName = newName
        email = newEmail
        phoneNumber = newPhone
        
        viewModelScope.launch {
            currentUsername?.let { username ->
                viewModelScope.launch(Dispatchers.IO) {
                    userDao.updateProfile(username, newName, newEmail, newPhone)
                }
            }
            if (parentId == null) return@launch
            
            runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitInstance.api.updateParentProfile(
                        idFilter = "eq.$parentId",
                        request = ParentProfileUpdateRequest(
                            email = newEmail,
                            phone = newPhone
                        )
                    )
                }
            }.onSuccess { list ->
                list.firstOrNull()?.let {
                    fullName = it.name
                    email = it.email
                    phoneNumber = it.phone
                    profileImageUrl = it.profileImageUrl
                    backgroundImageUrl = it.backgroundImageUrl ?: it.profileImageUrl
                }
            }
        }
    }
    
    fun updateProfileImage(inputStream: InputStream?, uri: Uri?) {
        val parentId = getSafeParentId()
        viewModelScope.launch {
            try {
                val compressedBytes = withContext(Dispatchers.IO) {
                    val rawBytes = inputStream?.use { it.readBytes() } ?: return@withContext null
                    
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
                    val decodedBitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOptions) ?: return@withContext null
                    
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
                    outputStream.toByteArray()
                } ?: return@launch
                
                val decodedBitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
                }
                profileBitmap = decodedBitmap?.asImageBitmap()
                
                currentUsername?.let { username ->
                    viewModelScope.launch(Dispatchers.IO) {
                        userDao.updateProfileImage(username, uri?.toString(), compressedBytes)
                    }
                }
                
                if (parentId == null) return@launch

                val mimeType = "image/jpeg" // We forced JPEG compression
                val base64 = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
                
                runCatching {
                    withContext(Dispatchers.IO) {
                        RetrofitInstance.api.updateParentProfile(
                            idFilter = "eq.$parentId",
                            request = ParentProfileUpdateRequest(
                                email = email,
                                phone = phoneNumber,
                                profileImageData = base64,
                                profileImageMimeType = mimeType
                            )
                        )
                    }
                }.onSuccess { list ->
                    list.firstOrNull()?.let {
                        profileImageUrl = it.profileImageUrl
                        backgroundImageUrl = it.backgroundImageUrl ?: it.profileImageUrl
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteProfileImage() {
        val parentId = getSafeParentId()
        viewModelScope.launch {
            try {
                profileBitmap = null
                profileImageUrl = null
                
                currentUsername?.let { username ->
                    viewModelScope.launch(Dispatchers.IO) {
                        userDao.updateProfileImage(username, null, null)
                    }
                }
                
                if (parentId == null) return@launch

                runCatching {
                    withContext(Dispatchers.IO) {
                        RetrofitInstance.api.updateParentProfile(
                            idFilter = "eq.$parentId",
                            request = ParentProfileUpdateRequest(
                                email = email,
                                phone = phoneNumber,
                                profileImageData = "", // Empty string to indicate removal
                                profileImageMimeType = ""
                            )
                        )
                    }
                }.onSuccess { list ->
                    list.firstOrNull()?.let {
                        profileImageUrl = it.profileImageUrl
                        backgroundImageUrl = it.backgroundImageUrl ?: it.profileImageUrl
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
