package com.mis.parentapp.core

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mis.parentapp.DebugMenuScreen
import com.mis.parentapp.R
import com.mis.parentapp.data.AppDatabase
import com.mis.parentapp.data.UserRepository
import com.mis.parentapp.features.auth.AuthViewModel
import com.mis.parentapp.features.auth.PasswordSignInScreen
import com.mis.parentapp.features.auth.UsernameSignInScreen
import com.mis.parentapp.features.home.HomeScreen
import com.mis.parentapp.features.me.MeScreen
import com.mis.parentapp.features.me.UserProfileViewModel
import com.mis.parentapp.features.student.StudentScreen
import com.mis.parentapp.navigation.Calendar
import com.mis.parentapp.navigation.DebugMenu
import com.mis.parentapp.navigation.Documents
import com.mis.parentapp.navigation.FAQs
import com.mis.parentapp.navigation.FormsAndRequest
import com.mis.parentapp.navigation.Home
import com.mis.parentapp.navigation.Me
import com.mis.parentapp.navigation.MonitorAcademic
import com.mis.parentapp.navigation.Notification
import com.mis.parentapp.navigation.PasswordSignIn
import com.mis.parentapp.navigation.PaymentOptions
import com.mis.parentapp.navigation.RecentActivities
import com.mis.parentapp.navigation.SignIn
import com.mis.parentapp.navigation.Student
import com.mis.parentapp.navigation.StudyLoad
import com.mis.parentapp.navigation.TrackAttendance
import com.mis.parentapp.navigation.UpcomingEvents
import com.mis.parentapp.navigation.Announcements
import com.mis.parentapp.navigation.Feedbacks
import com.mis.parentapp.navigation.Meeting
import com.mis.parentapp.navigation.Messages
import com.mis.parentapp.navigation.DataSafety
import com.mis.parentapp.navigation.EditProfile
import com.mis.parentapp.navigation.Preference
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.draw.clip
import com.mis.parentapp.navigation.Chat
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.features.me.essentials.ChatViewModel
import com.mis.parentapp.utilities.modals.GenericMenuModal
import com.mis.parentapp.utilities.modals.MenuItem

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun MainScreen(
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val studentSharedViewModel: StudentSharedViewModel = viewModel()
    val userProfileViewModel: UserProfileViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Get the ChatViewModel scoped to the Chat route if it exists in the backstack
    val isChatInBackstack = currentDestination?.hierarchy?.any { it.hasRoute(Chat::class) } == true
    val chatViewModel: ChatViewModel = if (isChatInBackstack) {
        val chatEntry = remember(navBackStackEntry) {
            navController.getBackStackEntry(Chat::class)
        }
        viewModel(chatEntry)
    } else {
        viewModel()
    }

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val authViewModel = remember { AuthViewModel(userRepository) }

    val bottomTabs = listOf(
        BottomTab("Home", Home, Icons.Filled.Home, Icons.Outlined.Home),
        BottomTab("Student", Student, Icons.Filled.School, Icons.Outlined.School),
        BottomTab("Me", Me, Icons.Filled.Person, Icons.Outlined.Person)
    )

    val showBottomBar = bottomTabs.any { tab ->
        currentDestination?.hierarchy?.any { it.hasRoute(tab.route::class) } == true
    } || currentDestination?.hasRoute(Notification::class) == true ||
            currentDestination?.hasRoute(Calendar::class) == true ||
            currentDestination?.hasRoute(StudyLoad::class) == true ||
            currentDestination?.hasRoute(UpcomingEvents::class) == true ||
            currentDestination?.hasRoute(RecentActivities::class) == true ||
            currentDestination?.hasRoute(MonitorAcademic::class) == true ||
            currentDestination?.hasRoute(TrackAttendance::class) == true ||
            currentDestination?.hasRoute(Documents::class) == true ||
            currentDestination?.hasRoute(FormsAndRequest::class) == true ||
            currentDestination?.hasRoute(FAQs::class) == true ||
            currentDestination?.hasRoute(PaymentOptions::class) == true ||
            currentDestination?.hasRoute(Announcements::class) == true ||
            currentDestination?.hasRoute(Feedbacks::class) == true ||
            currentDestination?.hasRoute(Meeting::class) == true ||
            currentDestination?.hasRoute(Messages::class) == true ||
            currentDestination?.hasRoute(Chat::class) == true ||
            currentDestination?.hasRoute(DataSafety::class) == true ||
            currentDestination?.hasRoute(EditProfile::class) == true ||
            currentDestination?.hasRoute(Preference::class) == true

    val showSharedTopBar = bottomTabs.any { tab ->
        currentDestination?.hasRoute(tab.route::class) == true
    }

    val isChatScreen = currentDestination?.hasRoute(Chat::class) == true

    val useWhiteIcons = currentDestination?.hasRoute(Student::class) == true ||
            currentDestination?.hasRoute(Me::class) == true

    val isSolidTopBar = currentDestination?.hasRoute(Home::class) == true

    val topBarBackgroundColor by animateColorAsState(
        targetValue = if (isSolidTopBar) MaterialTheme.colorScheme.background else Color.Transparent,
        animationSpec = tween(300),
        label = "TopBarBackground"
    )

    val db = AppDatabase.getDatabase(context)
    val dao = db.studentMonitoringDao()

    val menuItems = when {
        currentDestination?.hasRoute(Home::class) == true -> listOf(
            MenuItem("Upcoming events", "Stay updated on school activities.", Icons.Filled.Settings) { navController.navigate(UpcomingEvents); showBottomSheet = false },
            MenuItem("Recent activities", "Check your recent logs.", Icons.Filled.Settings) { navController.navigate(RecentActivities); showBottomSheet = false }
        )
        currentDestination?.hasRoute(Student::class) == true -> listOf(
            MenuItem("Monitor Academic", "Check academic progress.", Icons.Filled.School) { navController.navigate(MonitorAcademic); showBottomSheet = false },
            MenuItem("Track Attendance", "Daily presence records.", Icons.Filled.Settings) { navController.navigate(TrackAttendance); showBottomSheet = false }
        )
        currentDestination?.hasRoute(Me::class) == true -> listOf(
            MenuItem("About App", "Information about MIS Parent App.", Icons.Outlined.Info) { showBottomSheet = false }
        )
        else -> emptyList()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isChatScreen) {
                val chatArgs = navBackStackEntry?.toRoute<Chat>()
                ChatInputBar(
                    text = chatViewModel.chatTextState,
                    onTextChange = { chatViewModel.chatTextState = it },
                    onSend = { 
                        chatArgs?.let { chatViewModel.sendMessage(it.id) }
                    }
                )
            } else if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    bottomTabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.hasRoute(tab.route::class)
                        } == true || when (tab.route) {
                            Home -> currentDestination?.hasRoute(Notification::class) == true ||
                                    currentDestination?.hasRoute(Calendar::class) == true ||
                                    currentDestination?.hasRoute(UpcomingEvents::class) == true ||
                                    currentDestination?.hasRoute(RecentActivities::class) == true
                            Student -> currentDestination?.hasRoute(MonitorAcademic::class) == true ||
                                    currentDestination?.hasRoute(TrackAttendance::class) == true ||
                                    currentDestination?.hasRoute(StudyLoad::class) == true
                            Me -> currentDestination?.hasRoute(Announcements::class) == true ||
                                    currentDestination?.hasRoute(Feedbacks::class) == true ||
                                    currentDestination?.hasRoute(Meeting::class) == true ||
                                    currentDestination?.hasRoute(Messages::class) == true ||
                                    currentDestination?.hasRoute(Chat::class) == true ||
                                    currentDestination?.hasRoute(DataSafety::class) == true ||
                                    currentDestination?.hasRoute(EditProfile::class) == true ||
                                    currentDestination?.hasRoute(Preference::class) == true
                            else -> false
                        }
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
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Home,
                modifier = Modifier.padding(
                    top = if (isSolidTopBar) innerPadding.calculateTopPadding() else 0.dp,
                    bottom = innerPadding.calculateBottomPadding()
                ),
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
                composable<DebugMenu> {
                    DebugMenuScreen(
                        onNavigateToSignIn = { bgId -> navController.navigate(SignIn(bgId)) },
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
                            navController.navigate(Home) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<Me> {
                    MeScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        userProfileViewModel = userProfileViewModel,
                        onSignOutClick = onSignOut
                    )
                }
                composable<Home> {
                    HomeScreen(
                        studentVM = studentSharedViewModel,
                        mainNavController = navController
                    )
                }
                composable<Student> {
                    StudentScreen(
                        studentVM = studentSharedViewModel,
                        dao = dao,
                        onStudyLoadClick = { navController.navigate(StudyLoad) }
                    )
                }
                composable<Notification> {
                    SubScreen(
                        startDestination = Notification,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<Calendar> {
                    SubScreen(
                        startDestination = Calendar,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<StudyLoad> {
                    SubScreen(
                        startDestination = StudyLoad,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<UpcomingEvents> {
                    SubScreen(
                        startDestination = UpcomingEvents,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<RecentActivities> {
                    SubScreen(
                        startDestination = RecentActivities,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<MonitorAcademic> {
                    SubScreen(
                        startDestination = MonitorAcademic,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<TrackAttendance> {
                    SubScreen(
                        startDestination = TrackAttendance,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<Documents> {
                    SubScreen(
                        startDestination = Documents,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<FormsAndRequest> {
                    SubScreen(
                        startDestination = FormsAndRequest,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<FAQs> {
                    SubScreen(
                        startDestination = FAQs,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<PaymentOptions> {
                    SubScreen(
                        startDestination = PaymentOptions,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<Announcements> {
                    SubScreen(
                        startDestination = Announcements,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<Feedbacks> {
                    SubScreen(
                        startDestination = Feedbacks,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<Meeting> {
                    SubScreen(
                        startDestination = Meeting,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<Messages> {
                    SubScreen(
                        startDestination = Messages,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() },
                        onNavigate = { route: Any -> navController.navigate(route) }
                    )
                }
                composable<Chat> { backStackEntry ->
                    val args = backStackEntry.toRoute<Chat>()
                    SubScreen(
                        startDestination = args,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() },
                        chatViewModel = chatViewModel
                    )
                }
                composable<DataSafety> {
                    SubScreen(
                        startDestination = DataSafety,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<EditProfile> {
                    SubScreen(
                        startDestination = EditProfile,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() },
                        userProfileViewModel = userProfileViewModel
                    )
                }
                composable<Preference> {
                    SubScreen(
                        startDestination = Preference,
                        studentVM = studentSharedViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            if (showSharedTopBar) {
                MainTopBar(
                    onMenuClick = { showBottomSheet = true },
                    onNotificationClick = { navController.navigate(Notification) },
                    onCalendarClick = { navController.navigate(Calendar) },
                    iconTint = if (useWhiteIcons) Color.White else MaterialTheme.colorScheme.onBackground,
                    menuIconTint = if (useWhiteIcons) Color.White else MaterialTheme.colorScheme.onBackground,
                    backgroundColor = topBarBackgroundColor,
                    isMeScreen = currentDestination?.hasRoute(Me::class) == true
                )
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                GenericMenuModal(
                    items = menuItems
                )
            }
        }
    }
}

@Composable
fun MainTopBar(
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onCalendarClick: () -> Unit,
    iconTint: Color,
    menuIconTint: Color,
    backgroundColor: Color,
    isMeScreen: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.school_logo),
            contentDescription = "School Logo",
            modifier = Modifier.size(60.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNotificationClick) {
                Image(
                    painter = painterResource(id = R.drawable.ph_bell),
                    contentDescription = "Notifications",
                    modifier = Modifier.size(32.dp),
                    colorFilter = ColorFilter.tint(iconTint)
                )
            }
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = if (isMeScreen) Icons.Outlined.Info else Icons.Default.Menu,
                    contentDescription = if (isMeScreen) "About" else "Menu",
                    tint = menuIconTint,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

data class BottomTab(
    val label: String,
    val route: Any,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun ChatInputBar(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Text message") },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}