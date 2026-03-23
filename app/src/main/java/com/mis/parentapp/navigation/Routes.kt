package com.mis.parentapp.navigation

import kotlinx.serialization.Serializable

@Serializable object OnBoarding
@Serializable data class SignUp(val backgroundResId: Int)
@Serializable data class SignIn(val backgroundResId: Int)
@Serializable object Services
@Serializable object Me
@Serializable object Home
@Serializable object Student
@Serializable object MainContainer
