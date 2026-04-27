package com.mis.parentapp.navigation

import kotlinx.serialization.Serializable

//for dev purposes only
@Serializable
object DebugMenu

@Serializable object OnBoarding
@Serializable data class SignIn(val backgroundResId: Int)
@Serializable data class PasswordSignIn(val backgroundResId: Int, val email: String)
@Serializable data class SignUp(val backgroundResId: Int)
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