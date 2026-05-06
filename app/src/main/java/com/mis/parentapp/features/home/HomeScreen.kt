package com.mis.parentapp.features.home

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
import androidx.compose.foundation.layout.offset
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
import com.mis.parentapp.data.StudentEntity
import com.mis.parentapp.data.StudentWithSchedules
import com.mis.parentapp.data.StudentsRepo
import com.mis.parentapp.data.SubjectScheduleEntity
import com.mis.parentapp.navigation.Analytics
import com.mis.parentapp.navigation.Home
import com.mis.parentapp.navigation.Notification
import com.mis.parentapp.navigation.RecentActivities
import com.mis.parentapp.navigation.UpcomingEvents
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import androidx.compose.foundation.lazy.itemsIndexed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val homeNavController = rememberNavController()
    val sheetState = rememberModalBottomSheetState()
    val showSheet = remember { mutableStateOf(false) }
    val selectedEventForDetail = remember { mutableStateOf<EventItem?>(null) }

    if (showSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showSheet.value = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            HomeMenuDrawer(
                onItemClick = { route ->
                    showSheet.value = false
                    when (route) {
                        "Upcoming events" -> homeNavController.navigate(UpcomingEvents)
                        "Recent activities" -> homeNavController.navigate(RecentActivities)
                        "Analytics" -> homeNavController.navigate(Analytics)
                    }
                }
            )
        }
    }

    if (selectedEventForDetail.value != null) {
        EventDetailScreen(
            event = selectedEventForDetail.value!!,
            onBackClick = { selectedEventForDetail.value = null }
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
                    onMenuClick = { showSheet.value = true },
                    onUpcomingSeeAll = { homeNavController.navigate(UpcomingEvents) },
                    onRecentSeeAll = { homeNavController.navigate(RecentActivities) },
                    onEventClick = { event -> selectedEventForDetail.value = event }
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
    onEventClick: (EventItem) -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val repo = EventRepository(db.eventDao())
    val studentsRepo = StudentsRepo(db.studentMonitoringDao())
    val viewModel: EventsViewModel = viewModel(factory = EventsViewModel.provideFactory(repo))

    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val recentEvents by viewModel.recentEvents.collectAsState()

    //mock data - replace username with what you've used to sign in
    val username = "user"

    LaunchedEffect(Unit) {
        studentsRepo.seedDummyStudents(username)
    }

    val students by studentsRepo.getChildrenForParent(username).collectAsState(initial = emptyList())
    var selectedStudent by remember { mutableStateOf<StudentWithSchedules?>(null) }

    LaunchedEffect(students) {
        if (selectedStudent == null && students.isNotEmpty()) {
            selectedStudent = students.first()
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier.fillMaxSize().background(Color.White)
    ) {
        // TOP BAR
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
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

        //HORIZONTAL STUDENT SELECTOR
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(students) { studentWithSchedules ->
                        StudentSelectorItem(
                            student = studentWithSchedules.student,
                            isSelected = selectedStudent?.student?.studentId == studentWithSchedules.student.studentId,
                            onClick = { selectedStudent = studentWithSchedules }
                        )
                    }
                }
            }
        }

        //PRESENCE HEADER
        item {
            selectedStudent?.student?.let { child ->
                StudentPresenceHeader(student = child)
            }
        }

        //SCHEDULE LISTS
        item {
            selectedStudent?.let { studentWithSchedules ->
                ScheduleSection(schedules = studentWithSchedules.schedules)
            }
        }

        //QUICK STATS
        item {
            selectedStudent?.student?.let { child ->
                QuickStatsSection(
                    attendance = "${(child.attendanceScore * 100).toInt()}%",
                    gpa = child.gpa.toString(),
                    pending = "₱${child.pendingPayment}",
                    notifications = child.notificationCount.toString()
                )
            }
        }

        //UPCOMING EVENTS
        item {
            EventHorizontalSection(
                title = "Upcoming Events",
                events = upcomingEvents,
                onSeeAllClick = onUpcomingSeeAll,
                onEventClick = onEventClick
            )
        }

        //RECENT ACTIVITIES
        item {
            EventHorizontalSection(
                title = "Recent Activities",
                events = recentEvents,
                onSeeAllClick = onRecentSeeAll,
                onEventClick = onEventClick
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}


@Composable
fun StudentSelectorItem(
    student: StudentEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) ColorsDefaultTheme.color_Primary_green else Color.Transparent
    val scale = if (isSelected) 1.1f else 1.0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .requiredSize(70.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color(0xFFE8F5E9) else Color.Transparent)
                .padding(4.dp) // Space for the "border" effect
        ) {
            Image(
                painter = painterResource(id = student.profileImageRes),
                contentDescription = student.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            // Optional Selection Ring
            if (isSelected) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = ColorsDefaultTheme.color_Primary_green,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                    )
                }
            }
        }
        Text(
            text = student.name.split(" ").first(), // Show only first name
            style = AppTypes.type_Caption,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) ColorsDefaultTheme.color_Primary_green else Color.Black,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


@Composable
fun StudentPresenceHeader(student: StudentEntity) {
    val (brightColor, deepColor) = if (student.isPresent) {
        Color(0xFFDEF731) to Color(0xFF267D1E) //green
    } else {
        Color(0xFFE57373) to Color(0xFFC62828) //red
    }

    val statusText = if (student.isPresent) "At class" else "Not in class"
    val statusColor = if (student.isPresent) Color(0xFF4CAF50) else Color(0xFFF44336)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.height(180.dp).fillMaxWidth()
        ) {
            //left aura
            Box(
                modifier = Modifier
                    .offset(x = (-40).dp)
                    .requiredSize(300.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(brightColor.copy(alpha = 1f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            //right aura
            Box(
                modifier = Modifier
                    .offset(x = 40.dp)
                    .requiredSize(300.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(deepColor.copy(alpha = 1f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            Image(
                painter = painterResource(id = student.profileImageRes),
                contentDescription = null,
                modifier = Modifier
                    .requiredSize(110.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(4.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        //presence status badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(statusColor)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = statusText,
                color = Color.White,
                style = AppTypes.type_Caption.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}


@Composable
fun ScheduleSection(schedules: List<SubjectScheduleEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(schedules) { index, item ->
                val isNow = index == 0
                ScheduleCard(schedule = item, status = if (isNow) "Now" else "Up Next", isNow = isNow)
            }
        }
    }
}

@Composable
fun ScheduleCard(schedule: SubjectScheduleEntity, status: String, isNow: Boolean) {
    // Theme logic
    val backgroundColor = if (isNow) Color(0xFF1B4D13) else ColorsDefaultTheme.color_Surface
    val primaryText = if (isNow) Color(0xFFFFF59D) else Color(0xFF1B4D13) // Light Yellow vs Dark Green
    val secondaryText = if (isNow) Color.White.copy(alpha = 0.8f) else Color.Gray

    Box(
        modifier = Modifier
            .requiredSize(width = 220.dp, height = 150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.formkit_date), // or a specific schedule icon
            contentDescription = null,
            modifier = Modifier.requiredSize(24.dp).align(Alignment.TopStart),
            tint = primaryText
        )

        Text(
            text = status,
            style = AppTypes.type_Caption,
            fontWeight = FontWeight.Bold,
            color = primaryText,
            modifier = Modifier.align(Alignment.TopEnd)
        )

        Column(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = schedule.subject,
                style = AppTypes.type_H1,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = primaryText,
                maxLines = 1
            )
            Text(
                text = schedule.room,
                style = AppTypes.type_Body_Small,
                color = secondaryText
            )
            Text(
                text = schedule.time,
                style = AppTypes.type_Body_Small,
                fontWeight = FontWeight.Bold,
                color = secondaryText
            )
        }
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

//@Preview(showBackground = true, widthDp = 360)
//@Composable
//private fun BodyPreview() {
//    ParentAppTheme {
//        HomeScreen()
//    }
//}