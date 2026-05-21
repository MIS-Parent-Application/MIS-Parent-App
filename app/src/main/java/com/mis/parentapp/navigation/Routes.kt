package com.mis.parentapp.navigation

import kotlinx.serialization.Serializable

//for dev purposes only
@Serializable
object DebugMenu

@Serializable object OnBoarding
@Serializable data class SignIn(val backgroundResId: Int)
@Serializable data class PasswordSignIn(val backgroundResId: Int, val email: String)
@Serializable data class OtpSignIn(val backgroundResId: Int, val username: String, val password: String, val otpToken: String, val email: String)
@Serializable object Services
@Serializable object Me
@Serializable object Home
@Serializable object Student
@Serializable object MainContainer

@Serializable
object Notification

@Serializable
data class UpcomingEvents(
    val autoSelectEventId: Int? = null
)

@Serializable
data class RecentActivities(
    val autoSelectEventId: Int? = null
)

@Serializable
object Analytics

@Serializable
object Calendar

@Serializable
object StudyLoad

@Serializable
object MonitorAcademic

@Serializable
object TrackAttendance

@Serializable
object Documents

@Serializable
object FormsAndRequest

@Serializable
object FAQs

@Serializable
object PaymentOptions

// Me Essentials
@Serializable object Announcements
@Serializable object Feedbacks
@Serializable object Meeting
@Serializable object Messages
@Serializable data class Chat(val id: String, val senderName: String, val imageRes: Int)

// Me Settings
@Serializable object DataSafety
@Serializable object EditProfile
@Serializable object Preference
@Serializable object About  // 👈 ADDED THIS LINE