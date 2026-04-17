package com.mis.parentapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mis.parentapp.core.MainScreen
import com.mis.parentapp.features.auth.SignInScreen
import com.mis.parentapp.features.auth.SignUpScreen
import com.mis.parentapp.features.onboard.OnBoardingScreen
import com.mis.parentapp.navigation.MainContainer
import com.mis.parentapp.navigation.OnBoarding
import com.mis.parentapp.navigation.SignIn
import com.mis.parentapp.navigation.SignUp
import com.mis.parentapp.navigation.DebugMenu
import com.mis.parentapp.ui.theme.ParentAppTheme
import androidx.navigation.NavOptionsBuilder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParentAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    //original when testing actual app
//    NavHost(navController = navController, startDestination = OnBoarding) {

    //debug menu to launch specific pages
    NavHost(navController = navController, startDestination = DebugMenu) {
        //remove this after dev
        composable<DebugMenu> {
            DebugMenuScreen(
                onNavigateToSignIn = { bgId -> navController.navigate(SignIn(bgId)) },
                onNavigateToSignUp = { bgId -> navController.navigate(SignUp(bgId)) }
            )
        }
        composable<OnBoarding> {
            OnBoardingScreen(
                onSignInClick = { backgroundResId ->
                    navController.navigate(SignIn(backgroundResId))
                },
                onCreateAccountClick = { backgroundResId ->
                    navController.navigate(SignUp(backgroundResId))
                }
            )
        }
        composable<SignIn> { backStackEntry ->
            val args = backStackEntry.toRoute<SignIn>()
            SignInScreen(
                backgroundResId = args.backgroundResId,
                onBack = { navController.popBackStack() },
                onSignInSuccess = {
                    navController.navigate(MainContainer) {
                        popUpTo<OnBoarding> { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(SignUp(args.backgroundResId))
                }
            )
        }
        composable<SignUp> { backStackEntry ->
            val args = backStackEntry.toRoute<SignUp>()
            SignUpScreen(
                backgroundResId = args.backgroundResId,
                onBack = { navController.popBackStack() },
                onNavigateToSignIn = {
                    navController.navigate(SignIn(args.backgroundResId)) {
                        popUpTo<SignUp> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<MainContainer> {
            MainScreen()
        }
    }
}
