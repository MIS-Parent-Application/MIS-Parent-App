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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.ui.theme.ParentAppTheme

val DarkGreen = Color(0xFF1B5E20)
val LightGreenBg = Color(0xFFF1F8E9)
val StatCardBg = Color(0xFFF9FDF5)
val TextSecondary = Color(0xFF4A4A4A)

enum class Screen {
    Home, Student, Services, Me
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParentAppTheme {

                AppNavigation()
                MainApp()
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
@Composable
fun MainApp() {
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    Scaffold(
        bottomBar = {
            DashboardBottomNavigation(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                Screen.Home -> DashboardScreen()
                Screen.Student -> StudentProfileScreen()
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Coming Soon: \${currentScreen.name}")
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            BannerCard()
        }

        item {
            Column {
                Text(
                    text = "Quick Stats",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Attendance",
                        value = "98%",
                        icon = Icons.Default.CalendarToday
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "GPA",
                        value = "1.5",
                        icon = Icons.Default.AutoGraph
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Pending due",
                        value = "0.00",
                        icon = Icons.Default.AccountBalanceWallet
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Unread notifications",
                        value = "2",
                        icon = Icons.Default.Campaign
                    )
                }
            }
        }

        item {
            SectionPlaceholder(
                title = "Upcoming Events",
                emptyText = "No events yet."
            )
        }

        item {
            SectionPlaceholder(
                title = "Recent Activities",
                emptyText = "No activities yet."
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StudentProfileScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Top Header with Background Image placeholder
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // Background Placeholder (Gradient or Gray)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray.copy(alpha = 0.3f))
                )

                // Top Icons Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // School Logo Placeholder
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(50.dp).border(1.dp, Color.Yellow, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = DarkGreen,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.NotificationsNone, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.Menu, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }

                // Profile selection (Bottom of header)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Active Profile
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.fillMaxSize().padding(4.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // Add Button
                    Surface(
                        shape = CircleShape,
                        color = LightGreenBg,
                        modifier = Modifier.size(50.dp).border(1.dp, Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, null, tint = DarkGreen, modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }

        // 2. Main Profile Info
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Profile Picture
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.fillMaxSize().padding(16.dp))
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column {
                    Text(
                        text = "Nathaniel B. McClure",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Student's grade level",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "ID no.: XXXXXX",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // 3. Academic Program Section
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Academic Program",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ProgramItem(Icons.Outlined.School, "Bachelor of Science in Information Technology")
                ProgramItem(Icons.Default.StarOutline, "BSIT - 3rd year")
                ProgramItem(Icons.Default.CheckCircleOutline, "Officially enrolled for A.Y. 2025-2026")
            }
        }

        // 4. Class Schedule Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Class Schedule",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
                Icon(Icons.Default.GridView, null, tint = TextSecondary)
            }
        }

        // 5. The actual schedule cards row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ScheduleCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = DarkGreen,
                    contentColor = Color.White,
                    status = "Now",
                    title = "MATH 101",
                    subtitle = "Room 402",
                    time = "10:00 AM - 11:30 AM",
                    icon = Icons.Default.MyLocation
                )
                ScheduleCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = StatCardBg,
                    contentColor = Color.Black,
                    status = "Up next",
                    title = "VACANT TIME",
                    subtitle = "",
                    time = "11:30 AM - 1:30 PM",
                    icon = Icons.Outlined.AccessTime
                )
            }
        }

        // 6. Contacts Section
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Contacts",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                ContactItem("John Doe B. McClure", "+63 1234567890", "Parent", DarkGreen)
                ContactItem("Thomas B. McClure", "+63 1234567890", "Emergency contact", Color(0xFFB71C1C))
            }
        }

        // 7. Action Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* Handle call */ },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Make a call", color = Color(0xFFFDD835))
                }
                TextButton(onClick = { /* Handle edit */ }) {
                    Text("Edit contacts", color = TextSecondary)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun ScheduleCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    contentColor: Color,
    status: String,
    title: String,
    subtitle: String,
    time: String,
    icon: ImageVector
) {
    Card(
        modifier = modifier.height(150.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(icon, null, tint = contentColor, modifier = Modifier.size(24.dp))
                Text(text = status, color = contentColor, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(text = title, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(text = subtitle, color = contentColor, fontSize = 14.sp)
            Text(text = time, color = contentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ContactItem(
    name: String,
    phone: String,
    badgeText: String,
    badgeColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PersonOutline, null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = name, color = DarkGreen, modifier = Modifier.weight(1f))

            Surface(color = badgeColor, shape = RoundedCornerShape(12.dp)) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            Icon(Icons.Default.PhoneEnabled, null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = phone, color = DarkGreen)
        }
    }
}

@Composable
fun ProgramItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.Black, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 14.sp, color = DarkGreen)
    }
}

@Composable
fun SectionPlaceholder(title: String, emptyText: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Icon(
            imageVector = Icons.Default.Groups,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFFC5E1A5)
        )
        Text(
            text = emptyText,
            fontSize = 14.sp,
            color = DarkGreen,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun BannerCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = LightGreenBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, DarkGreen, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(40.dp),
                    tint = DarkGreen
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Good morning!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "See more about nathaniel's progress",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = StatCardBg),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DarkGreen.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkGreen
            )
        }
    }
}

@Composable
fun DashboardBottomNavigation(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentScreen == Screen.Home,
            onClick = { onScreenSelected(Screen.Home) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DarkGreen,
                selectedTextColor = DarkGreen,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = LightGreenBg
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.School, contentDescription = "Student") },
            label = { Text("Student") },
            selected = currentScreen == Screen.Student,
            onClick = { onScreenSelected(Screen.Student) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Build, contentDescription = "Services") },
            label = { Text("Services") },
            selected = currentScreen == Screen.Services,
            onClick = { onScreenSelected(Screen.Services) }
        )
        NavigationBarItem(
            icon = {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (currentScreen == Screen.Me) LightGreenBg else Color.LightGray)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp).align(Alignment.Center))
                }
            },
            label = { Text("Me") },
            selected = currentScreen == Screen.Me,
            onClick = { onScreenSelected(Screen.Me) }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardPreview() {
    ParentAppTheme {
        MainApp()
    }
}
