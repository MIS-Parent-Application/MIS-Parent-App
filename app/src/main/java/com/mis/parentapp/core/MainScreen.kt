package com.mis.parentapp.core

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.*
import com.mis.parentapp.features.auth.AuthViewModel
import com.mis.parentapp.features.home.HomeScreen
import com.mis.parentapp.features.me.MeScreen
import com.mis.parentapp.features.services.ServicesScreen
import com.mis.parentapp.features.student.StudentScreen
import com.mis.parentapp.features.auth.SignInScreen
import com.mis.parentapp.features.student.StudentScreen
import com.mis.parentapp.navigation.DebugMenu
import com.mis.parentapp.navigation.Home
import com.mis.parentapp.navigation.Me
import com.mis.parentapp.navigation.Services
import com.mis.parentapp.navigation.SignIn
import com.mis.parentapp.navigation.Student
import com.mis.parentapp.ui.theme.ParentAppTheme
import androidx.compose.ui.tooling.preview.Preview

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current

    val database = remember { com.mis.parentapp.data.AppDatabase.getDatabase(context) }
    val authViewModel = remember { AuthViewModel(database.userDao()) }

    val tabs = listOf(
        BottomTab("home", "Home", Icons.Filled.Home),
        BottomTab("student", "Student", Icons.Filled.School),
        BottomTab("services", "Services", Icons.Filled.Settings),
        BottomTab("me", "Me", Icons.Filled.Person)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            navController.navigate(tab.route)
                        },
                        icon = {
                            Icon(tab.icon, contentDescription = tab.label)
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("home") {
                Text("HOME SCREEN WORKING")
                HomeScreen()
            }

            composable("student") {
                Text("STUDENT SCREEN WORKING")
                StudentScreen()
            }

            composable("services") {
                Text("SERVICES SCREEN WORKING")
                ServicesScreen()
            }

            composable("me") {
                Text("ME SCREEN WORKING")
                MeScreen()
            composable<DebugMenu> {
                DebugMenuScreen(
                    onNavigateToSignIn = { bgId -> navController.navigate(SignIn(bgId)) }
                )
            }

            composable<SignIn> { backStackEntry ->
                val args = backStackEntry.toRoute<SignIn>()
                SignInScreen(
                    backgroundResId = args.backgroundResId,
                    onBack = { navController.popBackStack() },
                    viewModel = authViewModel,
                    onSignInSuccess = {
                        navController.navigate(Home) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

data class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    ParentAppTheme {
        MainScreen()
    }
}