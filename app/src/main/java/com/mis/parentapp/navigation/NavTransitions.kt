package com.mis.parentapp.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry

object NavTransitions {
    private val springSpec = spring<androidx.compose.ui.unit.IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    
    private val fadeSpec = spring<Float>(
        stiffness = Spring.StiffnessMediumLow
    )

    val enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = springSpec
        ) + fadeIn(animationSpec = fadeSpec)
    }

    val exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = springSpec
        ) + fadeOut(animationSpec = fadeSpec)
    }

    val popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = springSpec
        ) + fadeIn(animationSpec = fadeSpec)
    }

    val popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = springSpec
        ) + fadeOut(animationSpec = fadeSpec)
    }
}
