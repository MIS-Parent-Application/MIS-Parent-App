package com.mis.parentapp.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation.NavHostController
import com.mis.parentapp.R
import com.mis.parentapp.data.AppDatabase
import com.mis.parentapp.data.EventItem
import com.mis.parentapp.data.EventRepository
import com.mis.parentapp.data.StudentEntity
import com.mis.parentapp.data.SubjectScheduleEntity
import com.mis.parentapp.network.Child
import com.mis.parentapp.network.ClassSchedule
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.navigation.Analytics
import com.mis.parentapp.navigation.RecentActivities
import com.mis.parentapp.navigation.UpcomingEvents
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.rememberCoroutineScope
import com.mis.parentapp.data.StudentWithSchedules
import com.mis.parentapp.data.StudentsRepo
import com.mis.parentapp.data.UserRepository
import com.mis.parentapp.features.home.menu.EventCard
import com.mis.parentapp.features.home.menu.EventDetailScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    studentVM: StudentSharedViewModel? = null,
    mainNavController: NavHostController? = null
) {
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
                        "Upcoming events" -> mainNavController?.navigate(UpcomingEvents)
                        "Recent activities" -> mainNavController?.navigate(RecentActivities)
                        "Analytics" -> mainNavController?.navigate(Analytics)
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
        Body(
            modifier = modifier,
            studentVM = studentVM,
            onUpcomingSeeAll = { mainNavController?.navigate(UpcomingEvents) },
            onRecentSeeAll = { mainNavController?.navigate(RecentActivities) },
            onEventClick = { event -> selectedEventForDetail.value = event }
        )
    }
}

@Composable
fun Body(
    modifier: Modifier = Modifier,
    studentVM: StudentSharedViewModel? = null,
    onUpcomingSeeAll: () -> Unit,
    onRecentSeeAll: () -> Unit,
    onEventClick: (EventItem) -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val eventRepo = remember { EventRepository(db.eventDao()) }
    val studentRepo = remember { StudentsRepo(db.studentMonitoringDao(), db.userDao()) }
    val userRepo = remember { UserRepository(db.userDao()) }
    val currentUser by db.userDao().getUserFlow("user").collectAsState(initial = null)
    var showNoteDialog by remember { mutableStateOf(false) }
    var tempNote by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val localStudentsWithSchedules by studentRepo.getChildrenForParent("user").collectAsState(initial = emptyList())
    val eventViewModel: EventsViewModel = viewModel(factory = EventsViewModel.provideFactory(eventRepo))
    val upcomingEvents by eventViewModel.upcomingEvents.collectAsState()
    val recentEvents by eventViewModel.recentEvents.collectAsState()
    var dashboardError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (localStudentsWithSchedules.isEmpty()) {
            studentRepo.seedDummyStudents("user")
        }

        //Fetch retrofit
        try {
            val dashboard = RetrofitInstance.api.getDashboard()

            dashboard.children.forEach { remoteChild ->
                val homeStudent = remoteChild.toHomeStudent()
                db.studentMonitoringDao().insertStudent(homeStudent.student)
                db.studentMonitoringDao().insertSchedules(homeStudent.schedules)
            }

            dashboardError = null
        } catch (e: Exception) {
            //Use Room if offline/no server
            dashboardError = "Running in offline mode."
        }
    }

    var selectedStudent by remember { mutableStateOf<StudentWithSchedules?>(null) }

    LaunchedEffect(localStudentsWithSchedules) {
        if (localStudentsWithSchedules.isNotEmpty() && selectedStudent == null) {
            selectedStudent = localStudentsWithSchedules.first()
        }
    }

    if (showNoteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Update Status") },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = tempNote,
                    onValueChange = { tempNote = it },
                    placeholder = { Text("Post a note...") },
                    maxLines = 2
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    scope.launch {
                        val noteToSave = if (tempNote.isBlank()) "+" else tempNote
                        db.userDao().updateUserNote("user", noteToSave)
                        showNoteDialog = false
                    }
                }) {
                    Text("Share", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        item { Spacer(modifier = Modifier.height(36.dp)) }

        //STUDENT SELECTOR
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                dashboardError?.let { message ->
                    Text(
                        text = message,
                        color = Color.Red,
                        style = AppTypes.type_Body_Small,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), // Closer spacing
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //PARENT PIC
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                val currentNote = currentUser?.note ?: "+"
                                tempNote = if (currentNote == "+") "" else currentNote
                                showNoteDialog = true
                            }
                        ) {
                            Box(
                                modifier = Modifier.padding(bottom = 4.dp, end = 12.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.parent_pic),
                                    contentDescription = "Parent Profile",
                                    modifier = Modifier
                                        .requiredSize(50.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )

                                if (!currentUser?.note.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = 12.dp, y = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = currentUser!!.note!!,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            style = AppTypes.type_Caption,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Me",
                                style = AppTypes.type_Caption,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    //STUDENT SELECTORS
                    items(localStudentsWithSchedules) { studentWrapper ->
                        StudentSelectorItem(
                            student = studentWrapper.student,
                            isSelected = selectedStudent?.student?.studentId == studentWrapper.student.studentId,
                            onClick = { selectedStudent = studentWrapper }
                        )
                    }
                }
            }
        }

        // PRESENCE HEADER
        item {
            selectedStudent?.student?.let { child ->
                StudentPresenceHeader(student = child)
            }
        }

        // SCHEDULE LISTS
        item {
            selectedStudent?.let { studentWithSchedules ->
                ScheduleSection(schedules = studentWithSchedules.schedules.take(2))
            }
        }

        // QUICK STATS
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

private data class HomeStudent(
    val student: StudentEntity,
    val schedules: List<SubjectScheduleEntity>
)

private fun Child.toHomeStudent(): HomeStudent {
    val attendanceValue = attendance.removeSuffix("%").toDoubleOrNull() ?: 0.0
    val studentId = id.toString()
    return HomeStudent(
        student = StudentEntity(
            studentId = studentId,
            parentId = "server",
            name = name,
            course = course,
            year = year,
            attendanceScore = attendanceValue / 100.0,
            gpa = gpa,
            pendingPayment = pendingPayments.toDouble(),
            notificationCount = 0,
            profileImageRes = R.drawable.student_image,
            isPresent = attendanceValue > 0.0
        ),
        schedules = schedules.map { it.toScheduleEntity(studentId) }
    )
}

private fun ClassSchedule.toScheduleEntity(studentId: String): SubjectScheduleEntity {
    return SubjectScheduleEntity(
        studentId = studentId,
        subject = subject,
        room = room,
        day = day,
        time = "$startTime - $endTime"
    )
}


@Composable
fun StudentSelectorItem(
    student: StudentEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val highlightColor = if (student.isPresent) {
        ColorsDefaultTheme.color_Primary_green
    } else {
        MaterialTheme.colorScheme.error
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .requiredSize(50.dp) //circle size
                .clip(CircleShape)
                .background(if (isSelected) highlightColor.copy(alpha = 0.2f) else Color.Transparent)
                .padding(3.dp)
        ) {
            Image(
                painter = painterResource(id = student.profileImageRes),
                contentDescription = student.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            if (isSelected) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = highlightColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f) //border
                    )
                }
            }
        }
        Text(
            text = student.name.split(" ").first(),
            style = AppTypes.type_Caption,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) highlightColor else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


@Composable
fun StudentPresenceHeader(student: StudentEntity) {
    val (brightColor, deepColor) = if (student.isPresent) {
        MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
    }

    val statusText = if (student.isPresent) "At class" else "Not in class"
    val statusColor = if (student.isPresent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

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
    val backgroundColor = if (isNow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val primaryText = if (isNow) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    val secondaryText = if (isNow) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

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
                color = MaterialTheme.colorScheme.primary,
                style = AppTypes.type_H1,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See All",
                color = MaterialTheme.colorScheme.primary,
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.video_conference_streamline_bangalore),
            contentDescription = null,
            modifier = Modifier.requiredSize(80.dp)
        )
        Text(text = emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            color = MaterialTheme.colorScheme.primary,
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
            .heightIn(min = 140.dp) // Use heightIn to avoid cut-off if text grows
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .requiredSize(32.dp)
                .align(Alignment.TopStart),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = label,
            style = AppTypes.type_Caption,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.TopEnd)
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.primary,
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
