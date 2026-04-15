package com.mis.parentapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mis.parentapp.core.MainScreen
import com.mis.parentapp.features.auth.AuthViewModel
import com.mis.parentapp.features.auth.SignInScreen
import com.mis.parentapp.features.auth.SignUpScreen
import com.mis.parentapp.features.onboard.OnBoardingScreen
import com.mis.parentapp.navigation.DebugMenu
import com.mis.parentapp.navigation.MainContainer
import com.mis.parentapp.navigation.OnBoarding
import com.mis.parentapp.navigation.SignIn
import com.mis.parentapp.navigation.SignUp
import com.mis.parentapp.ui.theme.ParentAppTheme

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

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current

    //init database
    val database = remember { com.mis.parentapp.data.AppDatabase.getDatabase(context) }
    val authViewModel = remember { AuthViewModel(database.userDao()) }

    //original when testing actual app
    NavHost(navController = navController, startDestination = OnBoarding) {
    //debug menu to launch specific pages
//    NavHost(navController = navController, startDestination = DebugMenu) {
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
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSignInSuccess = {
                    navController.navigate(MainContainer) {
                        popUpTo<OnBoarding> { inclusive = true }
                    }
//                    navController.navigate(Home) {
//                        popUpTo(0) { inclusive = true }
//                        launchSingleTop = true
//                    }
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
                viewModel = authViewModel,
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
