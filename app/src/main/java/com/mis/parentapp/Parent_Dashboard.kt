package com.mis.parentapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

/**
 * NEW DASHBOARD DESIGN (RESERVE)
 * Colors and styles updated to match the latest design request.
 */
val DarkGreenReserve = Color(0xFF1B5E20)
val LightGreenBgReserve = Color(0xFFF1F8E9)
val StatCardBgReserve = Color(0xFFF9FDF5)
val TextSecondaryReserve = Color(0xFF4A4A4A)

enum class ScreenReserve {
    Home, Student, Services, Me
}

class ParentDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParentAppTheme {
                MainAppReserve()
            }
        }
    }
}

@Composable
fun MainAppReserve() {
    var currentScreen by remember { mutableStateOf(ScreenReserve.Home) }

    Scaffold(
        bottomBar = {
            DashboardBottomNavigationReserve(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                ScreenReserve.Home -> DashboardScreenReserve()
                ScreenReserve.Student -> StudentProfileScreenReserve()
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
fun DashboardScreenReserve() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            BannerCardReserve()
        }

        item {
            Column {
                Text(
                    text = "Quick Stats",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreenReserve,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    StatCardReserve(
                        modifier = Modifier.weight(1f),
                        title = "Attendance",
                        value = "98%",
                        icon = Icons.Default.CalendarToday
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    StatCardReserve(
                        modifier = Modifier.weight(1f),
                        title = "GPA",
                        value = "1.5",
                        icon = Icons.Default.AutoGraph
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatCardReserve(
                        modifier = Modifier.weight(1f),
                        title = "Pending due",
                        value = "0.00",
                        icon = Icons.Default.AccountBalanceWallet
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    StatCardReserve(
                        modifier = Modifier.weight(1f),
                        title = "Unread notifications",
                        value = "2",
                        icon = Icons.Default.Campaign
                    )
                }
            }
        }

        item {
            SectionPlaceholderReserve(
                title = "Upcoming Events",
                emptyText = "No events yet."
            )
        }

        item {
            SectionPlaceholderReserve(
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
fun StudentProfileScreenReserve() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray.copy(alpha = 0.3f))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(50.dp).border(1.dp, Color.Yellow, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = DarkGreenReserve,
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

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.fillMaxSize().padding(4.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = CircleShape,
                        color = LightGreenBgReserve,
                        modifier = Modifier.size(50.dp).border(1.dp, Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, null, tint = DarkGreenReserve, modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        color = TextSecondaryReserve
                    )
                    Text(
                        text = "ID no.: XXXXXX",
                        fontSize = 12.sp,
                        color = TextSecondaryReserve
                    )
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Academic Program",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreenReserve,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ProgramItemReserve(Icons.Outlined.School, "Bachelor of Science in Information Technology")
                ProgramItemReserve(Icons.Default.StarOutline, "BSIT - 3rd year")
                ProgramItemReserve(Icons.Default.CheckCircleOutline, "Officially enrolled for A.Y. 2025-2026")
            }
        }

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
                    color = DarkGreenReserve
                )
                Icon(Icons.Default.GridView, null, tint = TextSecondaryReserve)
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ScheduleCardReserve(
                    modifier = Modifier.weight(1f),
                    backgroundColor = DarkGreenReserve,
                    contentColor = Color.White,
                    status = "Now",
                    title = "MATH 101",
                    subtitle = "Room 402",
                    time = "10:00 AM - 11:30 AM",
                    icon = Icons.Default.MyLocation
                )
                ScheduleCardReserve(
                    modifier = Modifier.weight(1f),
                    backgroundColor = StatCardBgReserve,
                    contentColor = Color.Black,
                    status = "Up next",
                    title = "VACANT TIME",
                    subtitle = "",
                    time = "11:30 AM - 1:30 PM",
                    icon = Icons.Outlined.AccessTime
                )
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Contacts",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreenReserve,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                ContactItemReserve("John Doe B. McClure", "+63 1234567890", "Parent", DarkGreenReserve)
                ContactItemReserve("Thomas B. McClure", "+63 1234567890", "Emergency contact", Color(0xFFB71C1C))
            }
        }

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
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreenReserve),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Make a call", color = Color(0xFFFDD835))
                }
                TextButton(onClick = { /* Handle edit */ }) {
                    Text("Edit contacts", color = TextSecondaryReserve)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun ScheduleCardReserve(
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
fun ContactItemReserve(
    name: String,
    phone: String,
    badgeText: String,
    badgeColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PersonOutline, null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = name, color = DarkGreenReserve, modifier = Modifier.weight(1f))

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
            Text(text = phone, color = DarkGreenReserve)
        }
    }
}

@Composable
fun ProgramItemReserve(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.Black, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 14.sp, color = DarkGreenReserve)
    }
}

@Composable
fun SectionPlaceholderReserve(title: String, emptyText: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreenReserve,
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
            color = DarkGreenReserve,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun BannerCardReserve() {
    Card(
        colors = CardDefaults.cardColors(containerColor = LightGreenBgReserve),
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
                    .border(2.dp, DarkGreenReserve, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(40.dp),
                    tint = DarkGreenReserve
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
                    color = TextSecondaryReserve,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun StatCardReserve(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = StatCardBgReserve),
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
                    tint = DarkGreenReserve.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryReserve,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkGreenReserve
            )
        }
    }
}

@Composable
fun DashboardBottomNavigationReserve(
    currentScreen: ScreenReserve,
    onScreenSelected: (ScreenReserve) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentScreen == ScreenReserve.Home,
            onClick = { onScreenSelected(ScreenReserve.Home) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DarkGreenReserve,
                selectedTextColor = DarkGreenReserve,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = LightGreenBgReserve
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.School, contentDescription = "Student") },
            label = { Text("Student") },
            selected = currentScreen == ScreenReserve.Student,
            onClick = { onScreenSelected(ScreenReserve.Student) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Build, contentDescription = "Services") },
            label = { Text("Services") },
            selected = currentScreen == ScreenReserve.Services,
            onClick = { onScreenSelected(ScreenReserve.Services) }
        )
        NavigationBarItem(
            icon = {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (currentScreen == ScreenReserve.Me) LightGreenBgReserve else Color.LightGray)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp).align(Alignment.Center))
                }
            },
            label = { Text("Me") },
            selected = currentScreen == ScreenReserve.Me,
            onClick = { onScreenSelected(ScreenReserve.Me) }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ParentDashboardPreview() {
    ParentAppTheme {
        MainAppReserve()
    }
}
