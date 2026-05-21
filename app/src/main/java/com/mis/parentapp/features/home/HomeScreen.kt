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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mis.parentapp.R
import com.mis.parentapp.data.EventItem
import com.mis.parentapp.data.EventRepository
import com.mis.parentapp.data.StudentEntity
import com.mis.parentapp.data.SubjectScheduleEntity
import com.mis.parentapp.features.home.menu.EventCard
import com.mis.parentapp.features.me.UserProfileViewModel
import com.mis.parentapp.navigation.RecentActivities
import com.mis.parentapp.navigation.UpcomingEvents
import com.mis.parentapp.network.Child
import com.mis.parentapp.network.ClassSchedule
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.utilities.images.InitialsImageFallback
import com.mis.parentapp.utilities.images.RemoteImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    studentVM: StudentSharedViewModel? = null,
    userProfileViewModel: UserProfileViewModel = viewModel(),
    mainNavController: NavHostController? = null
) {
    val sheetState = rememberModalBottomSheetState()
    val showSheet = remember { mutableStateOf(false) }

    if (showSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showSheet.value = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            HomeMenuDrawer(
                onItemClick = { route ->
                    showSheet.value = false
                    when (route) {
                        "Upcoming events" -> mainNavController?.navigate(UpcomingEvents())
                        "Recent activities" -> mainNavController?.navigate(RecentActivities())
                    }
                }
            )
        }
    }

    Body(
        modifier = modifier,
        studentVM = studentVM,
        userProfileViewModel = userProfileViewModel,
        onUpcomingSeeAll = { mainNavController?.navigate(UpcomingEvents()) },
        onRecentSeeAll = { mainNavController?.navigate(RecentActivities()) },
        onUpcomingEventClick = { clickedEvent ->
            mainNavController?.navigate(UpcomingEvents(autoSelectEventId = clickedEvent.id))
        },
        onRecentEventClick = { clickedEvent ->
            mainNavController?.navigate(RecentActivities(autoSelectEventId = clickedEvent.id))
        }
    )
}

@Composable
fun Body(
    modifier: Modifier = Modifier,
    studentVM: StudentSharedViewModel? = null,
    userProfileViewModel: UserProfileViewModel = viewModel(),
    onUpcomingSeeAll: () -> Unit,
    onRecentSeeAll: () -> Unit,
    onUpcomingEventClick: (EventItem) -> Unit,
    onRecentEventClick: (EventItem) -> Unit
) {
    val context = LocalContext.current
    val eventRepo = remember { EventRepository() }
    val eventViewModel: EventsViewModel = viewModel(factory = EventsViewModel.provideFactory(eventRepo))
    val upcomingEvents by eventViewModel.upcomingEvents.collectAsState()
    val recentEvents by eventViewModel.recentEvents.collectAsState()
    var dashboardError by remember { mutableStateOf<String?>(null) }
    val selectedBackendStudentId = studentVM?.selectedStudent?.id

    LaunchedEffect(Unit) {
        try {
            val dashboard = RetrofitInstance.api.getDashboard()
            studentVM?.updateStudents(dashboard.children, dashboard.unreadAnnouncements)
            dashboardError = null
        } catch (e: Exception) {
            dashboardError = "Unable to load server student data."
        }
    }

    LaunchedEffect(selectedBackendStudentId) {
        if (selectedBackendStudentId != null) {
            eventViewModel.refreshData(selectedBackendStudentId)
        }
    }

    val students = remember(studentVM?.students) {
        studentVM?.students?.map { it.toHomeStudent() } ?: emptyList()
    }
    val selectedStudent = remember(students, studentVM?.selectedStudent) {
        val selectedId = studentVM?.selectedStudent?.id?.toString()
        students.firstOrNull { it.student.studentId == selectedId } ?: students.firstOrNull()
    }

    val configuration = LocalConfiguration.current
    val isWide = configuration.screenWidthDp >= 600

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(modifier = Modifier.height(if (isWide) 16.dp else 36.dp)) }

        //STUDENT SELECTOR
        item {
            Column(modifier = Modifier.widthIn(max = 1200.dp).fillMaxWidth().padding(top = 16.dp)) {
                dashboardError?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = AppTypes.type_Body_Small,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //PARENT PIC
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            if (userProfileViewModel.profileBitmap != null) {
                                Image(
                                    bitmap = userProfileViewModel.profileBitmap!!,
                                    contentDescription = "Parent Profile",
                                    modifier = Modifier
                                        .requiredSize(50.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                RemoteImage(
                                    url = userProfileViewModel.profileImageUrl,
                                    fallbackRes = R.drawable.parent_pic,
                                    contentDescription = "Parent Profile",
                                    modifier = Modifier
                                        .requiredSize(50.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop,
                                    fallbackContent = {
                                        InitialsImageFallback(
                                            name = userProfileViewModel.fullName,
                                            modifier = Modifier
                                                .requiredSize(50.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                        )
                                    }
                                )
                            }

                            Text(
                                text = "Me",
                                style = AppTypes.type_Caption,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    //STUDENT SELECTORS
                    items(students) { studentWrapper ->
                        StudentSelectorItem(
                            student = studentWrapper.student,
                            profileImageUrl = studentWrapper.profileImageUrl,
                            isSelected = selectedStudent?.student?.studentId == studentWrapper.student.studentId,
                            onClick = {
                                studentVM?.students
                                    ?.firstOrNull { it.id.toString() == studentWrapper.student.studentId }
                                    ?.let { studentVM.selectStudent(it) }
                            }
                        )
                    }
                }
            }
        }

        // CONTENT WRAPPER
        item {
            Column(
                modifier = Modifier.widthIn(max = 1200.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            selectedStudent?.let { studentWithSchedules ->
                                val schedulePair = resolveHomeSchedulePair(studentWithSchedules.schedules)
                                StudentPresenceHeader(
                                    student = studentWithSchedules.student,
                                    profileImageUrl = studentWithSchedules.profileImageUrl,
                                    isInClass = schedulePair.first.schedule != null
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                ScheduleSection(now = schedulePair.first, next = schedulePair.second)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            selectedStudent?.student?.let { child ->
                                QuickStatsSection(
                                    attendance = "${(child.attendanceScore * 100).toInt()}%",
                                    gpa = child.gpa.toString(),
                                    performance = "${child.performanceScore}%",
                                    notifications = child.notificationCount.toString()
                                )
                            }
                        }
                    }
                } else {
                    // PRESENCE HEADER
                    selectedStudent?.let { studentWithSchedules ->
                        val schedulePair = resolveHomeSchedulePair(studentWithSchedules.schedules)
                        StudentPresenceHeader(
                            student = studentWithSchedules.student,
                            profileImageUrl = studentWithSchedules.profileImageUrl,
                            isInClass = schedulePair.first.schedule != null
                        )
                    }

                    // SCHEDULE LISTS
                    selectedStudent?.let { studentWithSchedules ->
                        val schedulePair = resolveHomeSchedulePair(studentWithSchedules.schedules)
                        ScheduleSection(now = schedulePair.first, next = schedulePair.second)
                    }

                    // QUICK STATS
                    selectedStudent?.student?.let { child ->
                        QuickStatsSection(
                            attendance = "${(child.attendanceScore * 100).toInt()}%",
                            gpa = child.gpa.toString(),
                            performance = "${child.performanceScore}%",
                            notifications = child.notificationCount.toString()
                        )
                    }
                }
            }
        }

        //UPCOMING EVENTS
        item {
            Box(modifier = Modifier.widthIn(max = 1200.dp).fillMaxWidth()) {
                EventHorizontalSection(
                    title = "Upcoming Events",
                    events = upcomingEvents,
                    onSeeAllClick = onUpcomingSeeAll,
                    onEventClick = onUpcomingEventClick
                )
            }
        }

        //RECENT ACTIVITIES
        item {
            Box(modifier = Modifier.widthIn(max = 1200.dp).fillMaxWidth()) {
                EventHorizontalSection(
                    title = "Recent Activities",
                    events = recentEvents,
                    onSeeAllClick = onRecentSeeAll,
                    onEventClick = onRecentEventClick
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

private data class HomeStudent(
    val student: StudentEntity,
    val schedules: List<SubjectScheduleEntity>,
    val profileImageUrl: String?,
    val backgroundImageUrl: String?
)

data class HomeScheduleDisplay(
    val schedule: SubjectScheduleEntity?,
    val statusLabel: String,
    val dateLabel: String
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
            performanceScore = performancePercentage,
            notificationCount = notificationCount,
            profileImageRes = R.drawable.student_image,
            isPresent = resolveCurrentClass(schedules) != null
        ),
        schedules = schedules.map { it.toScheduleEntity(studentId) },
        profileImageUrl = profileImageUrl,
        backgroundImageUrl = backgroundImageUrl
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
    profileImageUrl: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val highlightColor = if (student.isPresent) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .requiredSize(50.dp)
                .clip(CircleShape)
                .background(if (isSelected) highlightColor.copy(alpha = 0.2f) else Color.Transparent)
                .padding(3.dp)
        ) {
            RemoteImage(
                url = profileImageUrl,
                fallbackRes = student.profileImageRes,
                contentDescription = student.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                fallbackContent = {
                    InitialsImageFallback(
                        name = student.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            )

            if (isSelected) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = highlightColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
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
    StudentPresenceHeader(student = student, isInClass = student.isPresent)
}

@Composable
fun StudentPresenceHeader(
    student: StudentEntity,
    profileImageUrl: String? = null,
    isInClass: Boolean
) {
    val highlightColor = if (isInClass) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    val statusText = if (isInClass) "At class" else "Not in class"

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
            Box(
                modifier = Modifier
                    .offset(x = (-40).dp)
                    .requiredSize(300.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(highlightColor.copy(alpha = 0.4f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .offset(x = 40.dp)
                    .requiredSize(300.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(highlightColor.copy(alpha = 0.4f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .requiredSize(116.dp)
                    .clip(CircleShape)
                    .background(highlightColor.copy(alpha = 0.2f))
                    .border(width = 3.dp, color = highlightColor, shape = CircleShape)
                    .padding(4.dp)
            ) {
                RemoteImage(
                    url = profileImageUrl,
                    fallbackRes = student.profileImageRes,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Crop,
                    fallbackContent = {
                        InitialsImageFallback(
                            name = student.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(highlightColor)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = statusText,
                color = if (isInClass) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError,
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
fun ScheduleSection(now: HomeScheduleDisplay, next: HomeScheduleDisplay) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ScheduleCard(
                    schedule = now.schedule,
                    status = now.statusLabel,
                    date = now.dateLabel,
                    fallbackSubject = "No class",
                    fallbackRoom = "-",
                    fallbackTime = "No class now",
                    isNow = true
                )
            }
            item {
                ScheduleCard(
                    schedule = next.schedule,
                    status = next.statusLabel,
                    date = next.dateLabel,
                    fallbackSubject = "VACANT",
                    fallbackRoom = "-",
                    fallbackTime = "No next class",
                    isNow = false
                )
            }
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: SubjectScheduleEntity?,
    status: String,
    date: String,
    fallbackSubject: String,
    fallbackRoom: String,
    fallbackTime: String,
    isNow: Boolean
) {
    val backgroundColor = if (isNow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val primaryText = if (isNow) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    val secondaryText = if (isNow) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Column(
        modifier = Modifier
            .requiredSize(width = 220.dp, height = 156.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(id = R.drawable.formkit_date),
                contentDescription = null,
                modifier = Modifier.requiredSize(24.dp),
                tint = primaryText
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = status,
                    style = AppTypes.type_Caption,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
                Text(
                    text = date,
                    style = AppTypes.type_Caption.copy(fontSize = 10.sp),
                    color = secondaryText
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = schedule?.subject ?: fallbackSubject,
                style = AppTypes.type_H1,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = primaryText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = schedule?.room ?: fallbackRoom,
                style = AppTypes.type_Body_Small,
                color = secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = schedule?.time ?: fallbackTime,
                style = AppTypes.type_Body_Small,
                fontWeight = FontWeight.Bold,
                color = secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun resolveHomeSchedulePair(
    schedules: List<SubjectScheduleEntity>
): Pair<HomeScheduleDisplay, HomeScheduleDisplay> {
    val calendar = Calendar.getInstance()
    val todayName = SimpleDateFormat("EEEE", Locale.US).format(calendar.time)
    val nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val todayDateStr = dateFormatter.format(calendar.time)

    val todaySchedules = schedules
        .filter { it.day.equals(todayName, ignoreCase = true) }
        .sortedBy { startMinutesFromRange(it.time) }

    val current = todaySchedules.firstOrNull {
        val start = startMinutesFromRange(it.time)
        val end = endMinutesFromRange(it.time)
        nowMinutes in start until end
    }

    val currentDisplay = HomeScheduleDisplay(
        schedule = current,
        statusLabel = "Now",
        dateLabel = todayDateStr
    )

    var nextSchedule: SubjectScheduleEntity? = todaySchedules.firstOrNull { startMinutesFromRange(it.time) > nowMinutes }
    var nextStatus = "Up Next"
    var nextDate = todayDateStr

    if (nextSchedule != null) {
        if (startMinutesFromRange(nextSchedule.time) - nowMinutes >= 720) {
            nextStatus = "Upcoming"
        }
    } else if (schedules.isNotEmpty()) {
        val todayIdx = dayOrder(todayName)
        val sortedAll = schedules.sortedWith(compareBy<SubjectScheduleEntity> { dayOrder(it.day) }.thenBy { startMinutesFromRange(it.time) })

        nextSchedule = sortedAll.firstOrNull { dayOrder(it.day) > todayIdx }
            ?: sortedAll.firstOrNull()

        if (nextSchedule != null) {
            nextStatus = nextSchedule.day
            val targetIdx = dayOrder(nextSchedule.day)
            var daysToAdd = targetIdx - todayIdx
            if (daysToAdd <= 0) daysToAdd += 7

            val nextCal = Calendar.getInstance()
            nextCal.add(Calendar.DAY_OF_YEAR, daysToAdd)
            nextDate = dateFormatter.format(nextCal.time)

            val totalGap = (daysToAdd * 24 * 60) + startMinutesFromRange(nextSchedule.time) - nowMinutes
            if (totalGap < 720) {
                nextStatus = "Up Next"
            }
        }
    }

    val nextDisplay = HomeScheduleDisplay(
        schedule = nextSchedule,
        statusLabel = nextStatus,
        dateLabel = nextDate
    )

    return currentDisplay to nextDisplay
}

private fun resolveCurrentClass(schedules: List<ClassSchedule>): ClassSchedule? {
    val calendar = Calendar.getInstance()
    val today = SimpleDateFormat("EEEE", Locale.US).format(calendar.time)
    val nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    return schedules.firstOrNull {
        it.day.equals(today, ignoreCase = true) &&
                nowMinutes in minutesFromTime(it.startTime) until minutesFromTime(it.endTime)
    }
}

private fun startMinutesFromRange(value: String): Int = minutesFromTime(value.substringBefore("-").trim())

private fun endMinutesFromRange(value: String): Int = minutesFromTime(value.substringAfter("-", value).trim())

private fun minutesFromTime(value: String): Int {
    val parts = value.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = parts.getOrNull(1)?.take(2)?.toIntOrNull() ?: 0
    return hour * 60 + minute
}

private fun dayOrder(day: String): Int {
    return listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        .indexOfFirst { it.equals(day, ignoreCase = true) }
        .let { if (it == -1) 99 else it }
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
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionPlaceholderContent(emptyText = "No $title yet.")
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(events) { event ->
                    EventCard(
                        event = event,
                        modifier = Modifier.width(200.dp),
                        onClick = { onEventClick(event) }
                    )
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
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val menuItems = listOf("Upcoming events", "Recent activities")
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun QuickStatsSection(attendance: String, gpa: String, performance: String, notifications: String) {
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
            StatCard("Performance", performance, R.drawable.baseline_trending_up_24, Modifier.weight(1f))
            StatCard("Notifications", notifications, R.drawable.fluent_color_megaphone_loud_32, Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(label: String, value: String, iconRes: Int, modifier: Modifier = Modifier) {
    var textSize by remember { mutableStateOf(40.sp) }

    Box(
        modifier = modifier
            .heightIn(min = 140.dp)
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
            modifier = Modifier.align(Alignment.TopEnd),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.primary,
            style = TextStyle(fontSize = textSize, fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.BottomStart),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                if (textLayoutResult.hasVisualOverflow) {
                    textSize = (textSize.value * 0.8f).sp
                }
            }
        )
    }
}
