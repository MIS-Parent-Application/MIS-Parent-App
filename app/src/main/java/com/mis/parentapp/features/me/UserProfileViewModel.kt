package com.mis.parentapp.features.me

import android.app.Application
import android.graphics.Bitmap
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
import com.mis.parentapp.network.ParentProfileUpdateRequest
import com.mis.parentapp.network.UpdateParentSecurityRequest
import com.mis.parentapp.network.SupabaseInstance
import io.github.jan.supabase.storage.storage
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

    // Status states
    var twoFactorEnabled by mutableStateOf(false)
    var loginAlertsEnabled by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isSavingProfile by mutableStateOf(false)

    // Pending data to be saved
    private var pendingImageBytes: ByteArray? = null
    private var pendingImageUri: String? = null

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
    }

    private fun loadProfileData() {
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
            }

            // 2. Fetch fresh data from API
            if (parentId == null || parentId == "null") {
                android.util.Log.d("UserProfileViewModel", "No valid Parent ID yet, skipping API load")
                return@launch
            }

            try {
                android.util.Log.d("UserProfileViewModel", "Fetching fresh profile for Parent ID: $parentId")
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
                    
                    // Sync with local DB
                    currentUsername?.let { username ->
                        viewModelScope.launch(Dispatchers.IO) {
                            userDao.updateProfile(username, parentRow.name, parentRow.email, parentRow.phone)
                            val currentUser = userDao.getCurrentUser()
                            userDao.updateProfileImage(username, parentRow.profileImageUrl, currentUser?.profileImageBlob)
                        }
                    }
                } else {
                    android.util.Log.w("UserProfileViewModel", "No profile record found in 'parents' table for UUID: $parentId")
                }
            } catch (e: Exception) {
                android.util.Log.e("UserProfileViewModel", "API Profile load failed", e)
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
                currentUsername?.let { username ->
                    viewModelScope.launch(Dispatchers.IO) {
                        userDao.updateSecuritySettings(username, it.twoFactorEnabled, loginAlertsEnabled)
                    }
                }
            }
        }
    }

    fun updateProfile(newName: String, newEmail: String, newPhone: String, onSuccess: () -> Unit = {}) {
        val parentId = getSafeParentId()
        if (parentId == null) {
            errorMessage = "No active session. Please re-login."
            return
        }

        isSavingProfile = true
        errorMessage = null

        viewModelScope.launch {
            try {
                var finalImageUrl = profileImageUrl

                // 1. If we have a pending image, upload it first
                if (pendingImageBytes != null) {
                    android.util.Log.d("UserProfileViewModel", "Uploading new profile picture...")
                    val fileName = "avatar_${parentId}.jpg"
                    val bucket = SupabaseInstance.client.storage.from("avatars")
                    
                    val publicUrl = withContext(Dispatchers.IO) {
                        try {
                            bucket.upload(fileName, pendingImageBytes!!) {
                                upsert = true
                            }
                            bucket.publicUrl(fileName)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    
                    if (publicUrl != null) {
                        finalImageUrl = publicUrl
                        android.util.Log.d("UserProfileViewModel", "Upload success. URL: $publicUrl")
                    } else {
                        throw Exception("Failed to upload image to Supabase Storage.")
                    }
                }

                // 2. Update the profile record in the database
                val responseList = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.updateParentProfile(
                        idFilter = "eq.$parentId",
                        request = ParentProfileUpdateRequest(
                            email = newEmail,
                            phone = newPhone,
                            profileImageUrl = finalImageUrl
                        )
                    )
                }

                val result = responseList.firstOrNull()
                if (result != null) {
                    fullName = result.name
                    email = result.email
                    phoneNumber = result.phone
                    profileImageUrl = result.profileImageUrl
                    backgroundImageUrl = result.backgroundImageUrl ?: result.profileImageUrl

                    // 3. Update local cache
                    currentUsername?.let { username ->
                        withContext(Dispatchers.IO) {
                            userDao.updateProfile(username, result.name, result.email, result.phone)
                            userDao.updateProfileImage(username, result.profileImageUrl, pendingImageBytes)
                        }
                    }

                    pendingImageBytes = null // Clear pending after success
                    onSuccess()
                    android.util.Log.d("UserProfileViewModel", "Profile saved successfully")
                } else {
                    throw Exception("Profile update failed: Record not found on server.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.localizedMessage ?: "Failed to save changes."
            } finally {
                isSavingProfile = false
            }
        }
    }
    
    fun updateProfileImage(inputStream: InputStream?, uri: Uri?) {
        viewModelScope.launch {
            try {
                val processed = withContext(Dispatchers.IO) {
                    val rawBytes = inputStream?.use { it.readBytes() } ?: return@withContext null
                    
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, options)
                    
                    val maxSize = 800
                    var inSampleSize = 1
                    if (options.outHeight > maxSize || options.outWidth > maxSize) {
                        val halfHeight = options.outHeight / 2
                        val halfWidth = options.outWidth / 2
                        while (halfHeight / inSampleSize >= maxSize && halfWidth / inSampleSize >= maxSize) {
                            inSampleSize *= 2
                        }
                    }
                    
                    val decodedBitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, BitmapFactory.Options().apply {
                        this.inSampleSize = inSampleSize
                    }) ?: return@withContext null
                    
                    val scaledBitmap = if (decodedBitmap.width > maxSize || decodedBitmap.height > maxSize) {
                        val ratio = decodedBitmap.width.toFloat() / decodedBitmap.height.toFloat()
                        val width: Int; val height: Int
                        if (ratio > 1) { width = maxSize; height = (maxSize / ratio).toInt() }
                        else { height = maxSize; width = (maxSize * ratio).toInt() }
                        Bitmap.createScaledBitmap(decodedBitmap, width, height, true)
                    } else decodedBitmap

                    val outputStream = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    Pair(scaledBitmap, outputStream.toByteArray())
                } ?: return@launch
                
                // Update local preview and set pending data
                profileBitmap = processed.first.asImageBitmap()
                pendingImageBytes = processed.second
                pendingImageUri = uri?.toString()
                
                android.util.Log.d("UserProfileViewModel", "New photo selected. Pending save.")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteProfileImage() {
        val parentId = getSafeParentId()
        if (parentId == null) return

        isSavingProfile = true
        viewModelScope.launch {
            try {
                // Update on server
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.updateParentProfile(
                        idFilter = "eq.$parentId",
                        request = ParentProfileUpdateRequest(
                            email = email,
                            phone = phoneNumber,
                            profileImageUrl = "" // Remove URL
                        )
                    )
                }

                if (response.isNotEmpty()) {
                    profileBitmap = null
                    profileImageUrl = null
                    pendingImageBytes = null
                    
                    currentUsername?.let { username ->
                        withContext(Dispatchers.IO) {
                            userDao.updateProfileImage(username, null, null)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Failed to remove image."
            } finally {
                isSavingProfile = false
            }
        }
    }
}
