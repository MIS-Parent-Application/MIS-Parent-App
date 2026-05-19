package com.mis.parentapp.navigation

import kotlinx.serialization.Serializable

//for dev purposes only
@Serializable
object DebugMenu

@Serializable object OnBoarding
@Serializable data class SignIn(val backgroundResId: Int)
@Serializable data class PasswordSignIn(val backgroundResId: Int, val email: String)
@Serializable object Services
@Serializable object Me
@Serializable object Home
@Serializable object Student
@Serializable object MainContainer

@Serializable
object Notification

@Serializable
object UpcomingEvents

@Serializable
object RecentActivities

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

// Me Settings
@Serializable object DataSafety
@Serializable object EditProfile
@Serializable object Preference

@Serializable
object GetStarted