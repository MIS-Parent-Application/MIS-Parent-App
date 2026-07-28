package com.mis.parentapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mis.parentapp.core.MainScreen
import com.mis.parentapp.data.AppDatabase
import com.mis.parentapp.data.UserRepository
import com.mis.parentapp.features.auth.AuthViewModel
import com.mis.parentapp.features.auth.GetStartedScreen
import com.mis.parentapp.features.auth.OtpSignInScreen
import com.mis.parentapp.features.auth.PasswordSignInScreen
import com.mis.parentapp.features.auth.UsernameSignInScreen
import com.mis.parentapp.navigation.MainContainer
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.navigation.OnBoarding
import com.mis.parentapp.navigation.OtpSignIn
import com.mis.parentapp.navigation.PasswordSignIn
import com.mis.parentapp.navigation.SignIn
import com.mis.parentapp.shared.AppSettingsViewModel
import kotlinx.coroutines.launch
import com.mis.parentapp.shared.ThemeMode
import com.mis.parentapp.ui.theme.ParentAppTheme

class MainActivity : ComponentActivity() {
    private var isCheckingAuth by mutableStateOf(true)
    private var initialDestination: Any by mutableStateOf(OnBoarding)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { isCheckingAuth }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val userRepository = UserRepository(database.userDao())

        lifecycleScope.launch {
            try {
                if (userRepository.isUserLoggedIn()) {
                    initialDestination = MainContainer
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isCheckingAuth = false
            }
        }

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val settingsViewModel: AppSettingsViewModel = viewModel()
            val darkTheme = when (settingsViewModel.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            ParentAppTheme(darkTheme = darkTheme) {
                if (!isCheckingAuth) {
                    AppNavigation(windowSizeClass, initialDestination)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    windowSizeClass: androidx.compose.material3.windowsizeclass.WindowSizeClass,
    initialDestination: Any
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val authViewModel = remember { AuthViewModel(userRepository) }

    // --- INITIALIZE SERVER-SIDE SESSION LISTENER ---
    androidx.compose.runtime.LaunchedEffect(Unit) {
        RetrofitInstance.setUnauthorizedCallback {
            // Ensure navigation happens on the Main Thread
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                authViewModel.signOut {
                    navController.navigate(OnBoarding) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }


    NavHost(
        navController = navController,
        startDestination = initialDestination,
        enterTransition = com.mis.parentapp.navigation.NavTransitions.enterTransition,
        exitTransition = com.mis.parentapp.navigation.NavTransitions.exitTransition,
        popEnterTransition = com.mis.parentapp.navigation.NavTransitions.popEnterTransition,
        popExitTransition = com.mis.parentapp.navigation.NavTransitions.popExitTransition
    ) {
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
                },
                onOtpRequired = { password, otpToken, email ->
                    navController.navigate(
                        OtpSignIn(
                            backgroundResId = args.backgroundResId,
                            username = args.email,
                            password = password,
                            otpToken = otpToken,
                            email = email
                        )
                    )
                }
            )
        }

        composable<OtpSignIn> { backStackEntry ->
            val args = backStackEntry.toRoute<OtpSignIn>()
            OtpSignInScreen(
                username = args.username,
                password = args.password,
                otpToken = args.otpToken,
                email = args.email,
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
            MainScreen(
                windowSizeClass = windowSizeClass,
                onSignOut = {
                    navController.navigate(OnBoarding) {
                        popUpTo(MainContainer) { inclusive = true }
                    }
                }
            )
        }
    }
}
