package com.mis.parentapp.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mis.parentapp.features.home.NotificationScreen
import com.mis.parentapp.features.home.menu.RecentActivitiesScreen
import com.mis.parentapp.features.home.menu.UpcomingEventsScreen
import com.mis.parentapp.features.student.StudyLoadScreen
import com.mis.parentapp.features.student.menu.MonitorAcademicScreen
import com.mis.parentapp.features.student.menu.TrackAttendanceContent
import com.mis.parentapp.features.services.menu.DocumentsScreens
import com.mis.parentapp.features.services.menu.FormsAndRequestScreen
import com.mis.parentapp.features.services.menu.FAQsScreen
import com.mis.parentapp.features.services.menu.PaymentOptionsScreen
import com.mis.parentapp.features.widgets.AcademicCalendarScreen
import com.mis.parentapp.features.widgets.NotificationsWidget
import com.mis.parentapp.features.me.essentials.AnnouncementsScreen
import com.mis.parentapp.features.me.essentials.FeedbacksScreen
import com.mis.parentapp.features.me.essentials.MeetingScreen
import com.mis.parentapp.features.me.essentials.MessagesScreen
import com.mis.parentapp.features.me.settings.DataSafetyScreen
import com.mis.parentapp.features.me.settings.EditProfileScreen
import com.mis.parentapp.features.me.settings.PreferenceScreen
import com.mis.parentapp.navigation.Calendar
import com.mis.parentapp.navigation.Notification
import com.mis.parentapp.navigation.RecentActivities
import com.mis.parentapp.navigation.StudyLoad
import com.mis.parentapp.navigation.UpcomingEvents
import com.mis.parentapp.navigation.MonitorAcademic
import com.mis.parentapp.navigation.TrackAttendance
import com.mis.parentapp.navigation.Documents
import com.mis.parentapp.navigation.FormsAndRequest
import com.mis.parentapp.navigation.FAQs
import com.mis.parentapp.navigation.PaymentOptions
import com.mis.parentapp.navigation.Announcements
import com.mis.parentapp.navigation.Feedbacks
import com.mis.parentapp.navigation.Meeting
import com.mis.parentapp.navigation.Messages
import com.mis.parentapp.navigation.DataSafety
import com.mis.parentapp.navigation.EditProfile
import com.mis.parentapp.navigation.Preference
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.ui.theme.AppTypes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScreen(
    startDestination: Any,
    studentVM: StudentSharedViewModel,
    onBack: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val title = when {
        currentDestination?.hasRoute(Notification::class) == true -> "Notifications"
        currentDestination?.hasRoute(UpcomingEvents::class) == true -> "Upcoming events"
        currentDestination?.hasRoute(RecentActivities::class) == true -> "Recent activities"
        currentDestination?.hasRoute(Calendar::class) == true -> "Calendar"
        currentDestination?.hasRoute(StudyLoad::class) == true -> "Study Load"
        currentDestination?.hasRoute(MonitorAcademic::class) == true -> "Academic"
        currentDestination?.hasRoute(TrackAttendance::class) == true -> "Attendance"
        currentDestination?.hasRoute(Documents::class) == true -> "Documents"
        currentDestination?.hasRoute(FormsAndRequest::class) == true -> "Forms & Request"
        currentDestination?.hasRoute(FAQs::class) == true -> "FAQs"
        currentDestination?.hasRoute(PaymentOptions::class) == true -> "Payment Options"
        currentDestination?.hasRoute(Announcements::class) == true -> "Announcements"
        currentDestination?.hasRoute(Feedbacks::class) == true -> "Feedbacks"
        currentDestination?.hasRoute(Meeting::class) == true -> "Meeting"
        currentDestination?.hasRoute(Messages::class) == true -> "Messages"
        currentDestination?.hasRoute(DataSafety::class) == true -> "Data Safety"
        currentDestination?.hasRoute(EditProfile::class) == true -> "Edit Profile"
        currentDestination?.hasRoute(Preference::class) == true -> "Preference"
        else -> ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, style = AppTypes.type_H1, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = {
                    fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    )
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300)
                    )
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300)
                    )
                }
            ) {
                composable<Notification> {
                    NotificationScreen(
                        studentVM = studentVM,
                        onBackClick = {
                            if (navController.previousBackStackEntry != null) navController.popBackStack() else onBack()
                        }
                    )
                }
                composable<UpcomingEvents> {
                    UpcomingEventsScreen(
                        onBackClick = {
                            if (navController.previousBackStackEntry != null) navController.popBackStack() else onBack()
                        }
                    )
                }
                composable<RecentActivities> {
                    RecentActivitiesScreen(
                        onBackClick = {
                            if (navController.previousBackStackEntry != null) navController.popBackStack() else onBack()
                        }
                    )
                }
                composable<Calendar> {
                    AcademicCalendarScreen()
                }
                composable<StudyLoad> {
                    StudyLoadScreen(
                        studentVM = studentVM,
                        onBackClick = {
                            if (navController.previousBackStackEntry != null) navController.popBackStack() else onBack()
                        }
                    )
                }
                composable<MonitorAcademic> {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val database = com.mis.parentapp.data.AppDatabase.getDatabase(context)
                    val dao = database.studentMonitoringDao()

                    val academicVM: com.mis.parentapp.features.student.StudentViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.mis.parentapp.features.student.StudentViewModel.provideFactory(dao)
                    )

                    MonitorAcademicScreen(
                        viewModel = academicVM,
                        onBackClick = {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                onBack()
                            }
                        },
                        onMonitorAcademicClick = {
                            navController.navigate(MonitorAcademic) { launchSingleTop = true }
                        },
                        onTrackAttendanceClick = {
                            navController.navigate(TrackAttendance) { launchSingleTop = true }
                        }
                    )
                }
                composable<TrackAttendance> {
                    TrackAttendanceContent(
                        attendanceList = emptyList(),
                        onBackClick = {
                            if (navController.previousBackStackEntry != null) navController.popBackStack() else onBack()
                        },
                        onMonitorAcademicClick = {
                            navController.navigate(MonitorAcademic) { launchSingleTop = true }
                        },
                        onTrackAttendanceClick = {
                            navController.navigate(TrackAttendance) { launchSingleTop = true }
                        }
                    )
                }
                composable<Documents> {
                    DocumentsScreens()
                }
                composable<FormsAndRequest> {
                    FormsAndRequestScreen()
                }
                composable<FAQs> {
                    FAQsScreen()
                }
                composable<PaymentOptions> {
                    PaymentOptionsScreen()
                }
                composable<Announcements> {
                    AnnouncementsScreen()
                }
                composable<Feedbacks> {
                    FeedbacksScreen()
                }
                composable<Meeting> {
                    MeetingScreen()
                }
                composable<Messages> {
                    MessagesScreen()
                }
                composable<DataSafety> {
                    DataSafetyScreen()
                }
                composable<EditProfile> {
                    EditProfileScreen()
                }
                composable<Preference> {
                    PreferenceScreen()
                }
            }
        }
    }
}