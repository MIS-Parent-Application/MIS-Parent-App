package com.mis.parentapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mis.parentapp.core.MainScreen
import com.mis.parentapp.data.AppDatabase
import com.mis.parentapp.features.auth.AuthViewModel
import com.mis.parentapp.features.auth.GetStartedScreen
import com.mis.parentapp.features.auth.UsernameSignInScreen
import com.mis.parentapp.features.auth.PasswordSignInScreen
import com.mis.parentapp.navigation.MainContainer
import com.mis.parentapp.navigation.OnBoarding
import com.mis.parentapp.navigation.SignIn
import com.mis.parentapp.navigation.PasswordSignIn
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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val authViewModel = remember { AuthViewModel(database.userDao()) }

    NavHost(navController = navController, startDestination = OnBoarding) {
        composable<OnBoarding> {
            GetStartedScreen(
                onNavigateToSignIn = { bgId ->
                    navController.navigate(SignIn(bgId))
                }
            )
        }

        composable<SignIn> { backStackEntry ->
            val args = backStackEntry.toRoute<SignIn>()
            UsernameSignInScreen(
                backgroundResId = args.backgroundResId,
                onBack = { navController.popBackStack() },
                onNavigateToPassword = { email ->
                    navController.navigate(PasswordSignIn(args.backgroundResId, email))
                }
            )
        }

        composable<PasswordSignIn> { backStackEntry ->
            val args = backStackEntry.toRoute<PasswordSignIn>()
            PasswordSignInScreen(
                username = args.email,
                backgroundResId = args.backgroundResId,
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSignInSuccess = {
                    navController.navigate(MainContainer) {
                        popUpTo(OnBoarding) { inclusive = true }
                    }
                }
            )
        }

        composable<MainContainer> {
            MainScreen()
        }
    }
}
