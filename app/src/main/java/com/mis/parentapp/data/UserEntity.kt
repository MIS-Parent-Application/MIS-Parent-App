package com.mis.parentapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val password: String,
    val note: String = "+",
    val fullName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val profileImageUri: String? = null,
    val profileImageBlob: ByteArray? = null,
    val lastLoginTime: Long = 0L
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserEntity

        if (username != other.username) return false
        if (password != other.password) return false
        if (note != other.note) return false
        if (fullName != other.fullName) return false
        if (email != other.email) return false
        if (phoneNumber != other.phoneNumber) return false
        if (profileImageUri != other.profileImageUri) return false
        if (profileImageBlob != null) {
            if (other.profileImageBlob == null) return false
            if (!profileImageBlob.contentEquals(other.profileImageBlob)) return false
        } else if (other.profileImageBlob != null) return false
        if (lastLoginTime != other.lastLoginTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + note.hashCode()
        result = 31 * result + (fullName?.hashCode() ?: 0)
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (phoneNumber?.hashCode() ?: 0)
        result = 31 * result + (profileImageUri?.hashCode() ?: 0)
        result = 31 * result + (profileImageBlob?.contentHashCode() ?: 0)
        result = 31 * result + lastLoginTime.hashCode()
        return result
    }
}
