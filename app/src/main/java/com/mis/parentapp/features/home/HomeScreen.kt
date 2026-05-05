package com.mis.parentapp.features.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mis.parentapp.R
import com.mis.parentapp.data.AppDatabase
import com.mis.parentapp.data.EventItem
import com.mis.parentapp.data.EventRepository
import com.mis.parentapp.navigation.Analytics
import com.mis.parentapp.navigation.Home
import com.mis.parentapp.navigation.Notification
import com.mis.parentapp.navigation.RecentActivities
import com.mis.parentapp.navigation.UpcomingEvents
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val homeNavController = rememberNavController()
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    var selectedEventForDetail by remember { mutableStateOf<com.mis.parentapp.data.EventItem?>(null) }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            HomeMenuDrawer(
                onItemClick = { route ->
                    showSheet = false
                    when (route) {
                        "Upcoming events" -> homeNavController.navigate(UpcomingEvents)
                        "Recent activities" -> homeNavController.navigate(RecentActivities)
                        "Analytics" -> homeNavController.navigate(Analytics)
                    }
                }
            )
        }
    }

    if (selectedEventForDetail != null) {
        EventDetailScreen(
            event = selectedEventForDetail!!,
            onBackClick = { selectedEventForDetail = null }
        )
    } else {
        NavHost(
            navController = homeNavController,
            startDestination = Home,
            modifier = modifier.fillMaxSize()
        ) {
            composable<Home> {
                Body(
                    onNotificationClick = { homeNavController.navigate(Notification) },
                    onMenuClick = { showSheet = true },
                    onUpcomingSeeAll = { homeNavController.navigate(UpcomingEvents) },
                    onRecentSeeAll = { homeNavController.navigate(RecentActivities) },
                    onEventClick = { event -> selectedEventForDetail = event }
                )
            }

            composable<Notification> {
                NotificationScreen(onBackClick = { homeNavController.popBackStack() })
            }

            composable<UpcomingEvents> {
                UpcomingEventsScreen(onBackClick = { homeNavController.popBackStack() })
            }

            composable<RecentActivities> {
                RecentActivitiesScreen(onBackClick = { homeNavController.popBackStack() })
            }

            composable<Analytics> {
                AnalyticsScreen(onBackClick = { homeNavController.popBackStack() })
            }
        }
    }
}

@Composable
fun Body(
    modifier: Modifier = Modifier,
    onNotificationClick: () -> Unit,
    onMenuClick: () -> Unit,
    onUpcomingSeeAll: () -> Unit,
    onRecentSeeAll: () -> Unit,
    onEventClick: (com.mis.parentapp.data.EventItem) -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val repo = EventRepository(db.eventDao())
    val viewModel: EventsViewModel = viewModel(factory = EventsViewModel.provideFactory(repo))

    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val recentEvents by viewModel.recentEvents.collectAsState()

    // API Data State from master
    var attendance by remember { mutableStateOf("98%") }
    var gpa by remember { mutableStateOf("1.5") }
    var pending by remember { mutableStateOf("0.00") }
    var notifications by remember { mutableStateOf("2") }


    LaunchedEffect(Unit) {
        try {
            val data = RetrofitInstance.api.getDashboard()
            Log.d("API_TEST", "SUCCESS: $data")
            val child = data.children.firstOrNull()

            attendance = child?.attendance ?: "98%"
            gpa = child?.gpa?.toString() ?: "1.5"
            pending = child?.pendingPayments?.toString() ?: "0.00"
            notifications = data.unreadAnnouncements.toString()
        } catch (e: Exception) {
            Log.e("API_TEST", "ERROR: ${e.message}")
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.school_logo),
                    contentDescription = "School Logo",
                    modifier = Modifier.requiredSize(56.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.formkit_date),
                        contentDescription = "Date",
                        modifier = Modifier.requiredSize(28.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ph_bell),
                        contentDescription = "Notifications",
                        modifier = Modifier
                            .requiredSize(28.dp)
                            .clickable { onNotificationClick() }
                    )
                    Image(
                        painter = painterResource(id = R.drawable.studentswitcher),
                        contentDescription = "Student Switcher",
                        modifier = Modifier
                            .requiredSize(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = ColorsDefaultTheme.color_Primary_green
                        )
                    }
                }
            }
        }

        item {
            Image(
                painter = painterResource(id = R.drawable.card),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.FillWidth
            )
        }

        item {
            QuickStatsSection(attendance, gpa, pending, notifications)
        }

        item {
            EventHorizontalSection(
                title = "Upcoming Events",
                events = upcomingEvents,
                onSeeAllClick = onUpcomingSeeAll,
                onEventClick = onEventClick
            )
        }

        item {
            EventHorizontalSection(
                title = "Recent Activities",
                events = recentEvents,
                onSeeAllClick = onRecentSeeAll,
                onEventClick = onEventClick // Pass to cards
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}


@Composable
fun EventHorizontalSection(
    title: String,
    events: List<EventItem>,
    onSeeAllClick: () -> Unit,
    onEventClick: (EventItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color(0xFF1B4D13),
                style = AppTypes.type_H1,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See All",
                color = ColorsDefaultTheme.color_Primary_green,
                style = AppTypes.type_Body_Small,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }

        if (events.isEmpty()) {
            // Re-using your placeholder look if no data
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionPlaceholderContent(emptyText = "No $title yet.")
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(events) { event ->
                    // Reusing EventCard from UpcomingEventsScreen
                    EventCard(event = event, onClick = { onEventClick(event) })
                }
            }
        }
    }
}


@Composable
fun SectionPlaceholderContent(emptyText: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF1F8E9))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.video_conference_streamline_bangalore),
            contentDescription = null,
            modifier = Modifier.requiredSize(80.dp)
        )
        Text(text = emptyText, color = ColorsDefaultTheme.color_Primary_on_green)
    }
}


@Composable
fun HomeMenuDrawer(onItemClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "Menu",
            style = AppTypes.type_H1,
            color = Color(0xFF1B4D13),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val menuItems = listOf("Analytics", "Upcoming events", "Recent activities")
        menuItems.forEach { label ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(label) }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = AppTypes.type_H1.copy(fontSize = 18.sp),
                    color = ColorsDefaultTheme.color_Surface_on_surface
                )
            }
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun QuickStatsSection(attendance: String, gpa: String, pending: String, notifications: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Quick Stats",
            color = Color(0xFF1B4D13),
            style = AppTypes.type_H1,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard("Attendance", attendance, R.drawable.boxicons_calendar_check_filled, Modifier.weight(1f))
            StatCard("GPA", gpa, R.drawable.material_symbols_owl, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard("Pending due", pending, R.drawable.boxicons_wallet_filled, Modifier.weight(1f))
            StatCard("Notifications", notifications, R.drawable.fluent_color_megaphone_loud_32, Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(label: String, value: String, iconRes: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .requiredHeight(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ColorsDefaultTheme.color_Surface)
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .requiredSize(32.dp)
                .align(Alignment.TopStart),
            colorFilter = ColorFilter.tint(ColorsDefaultTheme.color_Primary_on_green)
        )
        Text(
            text = label,
            style = AppTypes.type_Caption,
            color = Color(0xFF1C1B1F),
            modifier = Modifier.align(Alignment.TopEnd)
        )
        Text(
            text = value,
            color = Color(0xFF1B4D13),
            style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
fun SectionPlaceholder(title: String, emptyText: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFF1B4D13),
            style = AppTypes.type_H1,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.video_conference_streamline_bangalore),
                contentDescription = null,
                modifier = Modifier.requiredSize(120.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = emptyText,
                style = AppTypes.type_Body_Small,
                color = ColorsDefaultTheme.color_Primary_on_green,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

//@Preview(showBackground = true, widthDp = 360)
//@Composable
//private fun BodyPreview() {
//    ParentAppTheme {
//        HomeScreen()
//    }
//}