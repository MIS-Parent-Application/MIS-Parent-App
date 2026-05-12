package com.mis.parentapp.core

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mis.parentapp.features.home.CalendarScreen
import com.mis.parentapp.features.home.HomeScreen
import com.mis.parentapp.features.home.NotificationScreen
import com.mis.parentapp.features.me.MeScreen
import com.mis.parentapp.features.services.ServicesScreen
import com.mis.parentapp.features.student.StudentScreen
import com.mis.parentapp.features.student.StudyLoadScreen
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.ui.theme.ParentAppTheme

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val studentSharedViewModel: StudentSharedViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
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
                HomeScreen(
                    studentVM = studentSharedViewModel,
                    onNotificationClick = { navController.navigate("notifications") },
                    onCalendarClick = { navController.navigate("calendar") }
                )
            }
            composable("student") {
                StudentScreen(
                    studentVM = studentSharedViewModel,
                    onNotificationClick = { navController.navigate("notifications") },
                    onCalendarClick = { navController.navigate("calendar") },
                    onStudyLoadClick = { navController.navigate("studyload") }
                )
            }
            composable("services") {
                ServicesScreen(
                    onNotificationClick = { navController.navigate("notifications") },
                    onCalendarClick = { navController.navigate("calendar") }
                )
            }
            composable("me") {
                MeScreen()
            }
            composable("notifications") {
                NotificationScreen(
                    studentVM = studentSharedViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("calendar") {
                CalendarScreen(
                    studentVM = studentSharedViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("studyload") {
                StudyLoadScreen(
                    studentVM = studentSharedViewModel,
                    onBackClick = { navController.popBackStack() }
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
