package com.mis.parentapp.core

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mis.parentapp.features.home.NotificationScreen
import com.mis.parentapp.features.home.menu.AnalyticsScreen
import com.mis.parentapp.features.home.menu.RecentActivitiesScreen
import com.mis.parentapp.features.home.menu.UpcomingEventsScreen
import com.mis.parentapp.features.me.UserProfileViewModel
import com.mis.parentapp.features.me.essentials.AnnouncementsScreen
import com.mis.parentapp.features.me.essentials.ChatViewModel
import com.mis.parentapp.features.me.essentials.FeedbacksScreen
import com.mis.parentapp.features.me.essentials.MeetingScreen
import com.mis.parentapp.features.me.essentials.MessageScreen
import com.mis.parentapp.features.me.essentials.MessagesScreen
import com.mis.parentapp.features.me.essentials.SharedFeedback
import com.mis.parentapp.features.me.settings.DataSafetyScreen
import com.mis.parentapp.features.me.settings.EditProfileScreen
import com.mis.parentapp.features.me.settings.PreferenceScreen
import com.mis.parentapp.features.services.menu.DocumentsScreens
import com.mis.parentapp.features.services.menu.FAQsScreen
import com.mis.parentapp.features.services.menu.FormsAndRequestScreen
import com.mis.parentapp.features.services.menu.PaymentOptionsScreen
import com.mis.parentapp.features.student.StudyLoadScreen
import com.mis.parentapp.features.student.menu.MonitorAcademicScreen
import com.mis.parentapp.features.student.menu.TrackAttendanceScreen
import com.mis.parentapp.features.home.CalendarScreen
import com.mis.parentapp.navigation.Analytics
import com.mis.parentapp.navigation.Announcements
import com.mis.parentapp.navigation.Calendar
import com.mis.parentapp.navigation.Chat
import com.mis.parentapp.navigation.DataSafety
import com.mis.parentapp.navigation.Documents
import com.mis.parentapp.navigation.EditProfile
import com.mis.parentapp.navigation.FAQs
import com.mis.parentapp.navigation.Feedbacks
import com.mis.parentapp.navigation.FormsAndRequest
import com.mis.parentapp.navigation.Meeting
import com.mis.parentapp.navigation.Messages
import com.mis.parentapp.navigation.MonitorAcademic
import com.mis.parentapp.navigation.Notification
import com.mis.parentapp.navigation.PaymentOptions
import com.mis.parentapp.navigation.Preference
import com.mis.parentapp.navigation.RecentActivities
import com.mis.parentapp.navigation.StudyLoad
import com.mis.parentapp.navigation.TrackAttendance
import com.mis.parentapp.navigation.UpcomingEvents
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.ui.theme.AppTypes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScreen(
    startDestination: Any,
    studentVM: StudentSharedViewModel,
    onBack: () -> Unit,
    onNavigate: ((Any) -> Unit)? = null,
    chatViewModel: ChatViewModel? = null,
    userProfileViewModel: UserProfileViewModel? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val title = when {
        currentDestination?.hasRoute(Notification::class) == true -> "Notifications"
        currentDestination?.hasRoute(UpcomingEvents::class) == true -> "Upcoming events"
        currentDestination?.hasRoute(RecentActivities::class) == true -> "Recent activities"
        currentDestination?.hasRoute(Analytics::class) == true -> "Analytics"
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
        currentDestination?.hasRoute(Meeting::class) == true -> "Meetings"
        currentDestination?.hasRoute(Messages::class) == true -> "Messages"
        currentDestination?.hasRoute(Chat::class) == true -> navBackStackEntry?.toRoute<Chat>()?.senderName ?: ""
        currentDestination?.hasRoute(DataSafety::class) == true -> "Data Safety"
        currentDestination?.hasRoute(EditProfile::class) == true -> "Edit Profile"
        currentDestination?.hasRoute(Preference::class) == true -> "Preference"
        else -> ""
    }

    val chatArgs = if (currentDestination?.hasRoute(Chat::class) == true) {
        navBackStackEntry?.toRoute<Chat>()
    } else null

    val selectedStudent = studentVM.selectedStudent
    val studentLabel = selectedStudent?.let { "${it.name} - ${it.section}" } ?: "No student selected"
    var academicDetailBackAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var academicDetailShareAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    var eventDetailBackAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var eventDetailShareAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val isEventDetailOpen = currentDestination?.hasRoute(UpcomingEvents::class) == true && eventDetailBackAction != null

    var recentDetailBackAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var recentDetailShareAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val isRecentDetailOpen = currentDestination?.hasRoute(RecentActivities::class) == true && recentDetailBackAction != null

    val isAnyDetailOpen = isEventDetailOpen || isRecentDetailOpen

    val isStudentMenu = currentDestination?.hasRoute(MonitorAcademic::class) == true ||
            currentDestination?.hasRoute(TrackAttendance::class) == true
    val isAcademicDetailOpen = currentDestination?.hasRoute(MonitorAcademic::class) == true &&
            academicDetailBackAction != null
    val isDarkTheme = isSystemInDarkTheme()
    val studentTopBarContainer = if (isDarkTheme) {
        androidx.compose.ui.graphics.Color(0xFF122D14)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val studentTopBarTitleColor = if (isDarkTheme) {
        androidx.compose.ui.graphics.Color(0xFFF3FFE9)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val studentTopBarSubtitleColor = if (isDarkTheme) {
        androidx.compose.ui.graphics.Color(0xFFC7EFBF)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Scaffold(
        topBar = {
            if (isStudentMenu) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = title,
                                style = AppTypes.type_H2,
                                color = studentTopBarTitleColor
                            )
                            Text(
                                text = studentLabel,
                                style = AppTypes.type_Caption,
                                color = studentTopBarSubtitleColor,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee(
                                    iterations = Int.MAX_VALUE,
                                    velocity = 40.dp
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isAcademicDetailOpen) {
                                academicDetailBackAction?.invoke()
                            } else if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                onBack()
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = studentTopBarTitleColor
                            )
                        }
                    },
                    actions = {
                        if (isAcademicDetailOpen) {
                            IconButton(onClick = { academicDetailShareAction?.invoke() }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share performance task",
                                    tint = studentTopBarTitleColor
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = studentTopBarContainer,
                        scrolledContainerColor = studentTopBarContainer
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (chatArgs != null && chatArgs.imageRes != 0) {
                                Image(
                                    painter = painterResource(id = chatArgs.imageRes),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Text(text = title, style = AppTypes.type_H1, fontSize = 20.sp)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isEventDetailOpen) {
                                eventDetailBackAction?.invoke()
                            } else if (isRecentDetailOpen) {
                                recentDetailBackAction?.invoke()
                            } else if (navController.previousBackStackEntry != null) {
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
                    actions = {
                        if (isAnyDetailOpen) {
                            IconButton(onClick = {
                                if (isEventDetailOpen) eventDetailShareAction?.invoke()
                                else recentDetailShareAction?.invoke()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share item details",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
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
                composable<UpcomingEvents> { backStackEntry ->
                    val routeArgs = backStackEntry.toRoute<UpcomingEvents>()

                    UpcomingEventsScreen(
                        studentId = selectedStudent?.id,
                        autoSelectEventId = routeArgs.autoSelectEventId,
                        onBackClick = {
                            if (navController.previousBackStackEntry != null) navController.popBackStack() else onBack()
                        },
                        onDetailTopBarChange = { isDetailOpen, detailBackAction, detailShareAction ->
                            eventDetailBackAction = if (isDetailOpen) detailBackAction else null
                            eventDetailShareAction = if (isDetailOpen) detailShareAction else null
                        }
                    )
                }
                composable<RecentActivities> { backStackEntry ->
                    val routeArgs = backStackEntry.toRoute<RecentActivities>()

                    RecentActivitiesScreen(
                        studentId = selectedStudent?.id,
                        autoSelectEventId = routeArgs.autoSelectEventId,
                        onBackClick = {
                            if (navController.previousBackStackEntry != null) navController.popBackStack() else onBack()
                        },
                        onDetailTopBarChange = { isDetailOpen, detailBackAction, detailShareAction ->
                            recentDetailBackAction = if (isDetailOpen) detailBackAction else null
                            recentDetailShareAction = if (isDetailOpen) detailShareAction else null
                        }
                    )
                }
                composable<Analytics> {
                    AnalyticsScreen(
                        onBackClick = {
                            if (navController.previousBackStackEntry != null) navController.popBackStack() else onBack()
                        }
                    )
                }
                composable<Calendar> {
                    CalendarScreen(
                        studentVM = studentVM,
                        onBackClick = {
                            if (navController.previousBackStackEntry != null) navController.popBackStack() else onBack()
                        }
                    )
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
                    MonitorAcademicScreen(
                        studentVM = studentVM,
                        onBackClick = {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                onBack()
                            }
                        },
                        onDetailTopBarChange = { isDetailOpen, detailBackAction, detailShareAction ->
                            academicDetailBackAction = if (isDetailOpen) detailBackAction else null
                            academicDetailShareAction = if (isDetailOpen) detailShareAction else null
                        }
                    )
                }
                composable<TrackAttendance> {
                    TrackAttendanceScreen(
                        studentVM = studentVM,
                        onBackClick = {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                onBack()
                            }
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
                    FeedbacksScreen(
                        onOpenTeacherMessages = {
                            navController.navigate(Messages)
                        }
                    )
                }
                composable<Meeting> {
                    MeetingScreen()
                }
                composable<Messages> {
                    MessagesScreen(
                        onMessageClick = { message ->
                            val route = Chat(message.id, message.senderName, message.imageRes ?: 0)
                            if (onNavigate != null) {
                                onNavigate(route)
                            } else {
                                navController.navigate(route) {
                                    if (SharedFeedback.message != null) {
                                        popUpTo(Feedbacks) { inclusive = false }
                                    }
                                }
                            }
                        }
                    )
                }
                composable<Chat> { 
                    val args = it.toRoute<Chat>()
                    val vm: ChatViewModel = chatViewModel ?: viewModel(it)
                    MessageScreen(
                        contactId = args.id,
                        senderName = args.senderName,
                        onBack = {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                onBack()
                            }
                        },
                        viewModel = vm,
                        onFeedbackSent = {
                            // Pop all the way back to the FeedbacksScreen instantly
                            navController.popBackStack(Feedbacks, inclusive = false)
                        }
                    )
                }
                composable<DataSafety> {
                    DataSafetyScreen(userProfileViewModel = userProfileViewModel ?: viewModel())
                }
                composable<EditProfile> {
                    EditProfileScreen(
                        userProfileViewModel = userProfileViewModel ?: viewModel(),
                        onSaveSuccess = {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                onBack()
                            }
                        }
                    )
                }
                composable<Preference> {
                    PreferenceScreen()
                }
            }
        }
    }
}
