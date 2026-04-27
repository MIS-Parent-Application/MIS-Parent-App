package com.mis.parentapp.core

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mis.parentapp.DebugMenuScreen
import com.mis.parentapp.features.auth.AuthViewModel
import com.mis.parentapp.features.home.HomeScreen
import com.mis.parentapp.features.me.MeScreen
import com.mis.parentapp.features.services.ServicesScreen
import com.mis.parentapp.features.auth.UsernameSignInScreen
import com.mis.parentapp.features.student.StudentScreen
import com.mis.parentapp.navigation.DebugMenu
import com.mis.parentapp.navigation.Home
import com.mis.parentapp.navigation.Me
import com.mis.parentapp.navigation.Services
import com.mis.parentapp.navigation.SignIn
import com.mis.parentapp.navigation.SignUp
import com.mis.parentapp.navigation.Student
import com.mis.parentapp.ui.theme.ParentAppTheme

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = androidx.compose.ui.platform.LocalContext.current

    //init database
    val database = remember { com.mis.parentapp.data.AppDatabase.getDatabase(context) }
    val authViewModel = remember { AuthViewModel(database.userDao()) }

    val bottomTabs = listOf(
        BottomTab("Home", Home, Icons.Filled.Home, Icons.Outlined.Home),
        BottomTab("Student", Student, Icons.Filled.School, Icons.Outlined.School),
        BottomTab("Services", Services, Icons.Filled.Settings, Icons.Outlined.Settings),
        BottomTab("Me", Me, Icons.Filled.Person, Icons.Outlined.Person)
    )

    val showBottomBar = bottomTabs.any { tab ->
        currentDestination?.hasRoute(tab.route::class) == true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.hasRoute(tab.route::class)
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            //replace this later when testing for actual app
//            startDestination = DebugMenu,
            startDestination = Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<DebugMenu> {
                DebugMenuScreen(
                    onNavigateToSignIn = { bgId -> navController.navigate(SignIn(bgId)) },
                    onNavigateToSignUp = { bgId -> navController.navigate(SignUp(bgId)) }
                )
            }

            composable<SignIn> { backStackEntry ->
                val args = backStackEntry.toRoute<SignIn>()
                UsernameSignInScreen(
                    backgroundResId = args.backgroundResId,
                    onBack = { navController.popBackStack() },
                    viewModel = authViewModel,
                    onSignInSuccess = {
                        navController.navigate(Home) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(SignUp(backgroundResId = args.backgroundResId))
                    }
                )
            }
            composable<Services> { ServicesScreen() }
            composable<Me> { MeScreen() }
            composable<Home> { HomeScreen() }
            composable<Student> { StudentScreen() }
        }
    }
}

data class BottomTab(
    val label: String,
    val route: Any,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ParentAppTheme {
        MainScreen()
    }
}
