package com.mis.parentapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mis.parentapp.core.MainScreen
import com.mis.parentapp.data.AppDatabase
import com.mis.parentapp.features.auth.AuthViewModel
import com.mis.parentapp.features.auth.GetStartedScreen
import com.mis.parentapp.features.auth.SignInScreen
import com.mis.parentapp.navigation.MainContainer
import com.mis.parentapp.navigation.OnBoarding
import com.mis.parentapp.navigation.SignIn
import com.mis.parentapp.ui.theme.ParentAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ParentAppTheme {
                MainScreen()
            }
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
            SignInScreen(
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
