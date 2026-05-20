package com.mis.parentapp.features.me

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mis.parentapp.R

class UserProfileViewModel : ViewModel() {
    var fullName by mutableStateOf("Nathaniel B. McClure")
    var email by mutableStateOf("nathaniel.mcclure@example.com")
    var phoneNumber by mutableStateOf("+63 912 345 6789")
    var profileImageRes by mutableStateOf(R.drawable.parent_pic)

    fun updateProfile(newName: String, newEmail: String, newPhone: String) {
        fullName = newName
        email = newEmail
        phoneNumber = newPhone
    }
    
    fun updateProfileImage(resId: Int) {
        profileImageRes = resId
    }
}
