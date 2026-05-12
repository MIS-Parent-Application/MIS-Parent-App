package com.mis.parentapp.features.student

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.network.Child
import com.mis.parentapp.network.ClassSchedule
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import com.mis.parentapp.ui.theme.ParentAppTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    studentVM: StudentSharedViewModel,
    modifier: Modifier = Modifier,
    onNotificationClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onStudyLoadClick: () -> Unit = {},
    onNavigateToAcademic: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val dashboard = RetrofitInstance.api.getDashboard()
            studentVM.updateStudents(dashboard.children)
        } catch (e: Exception) {
            errorMessage = "Unable to load student data."
        }
    }

    val students = studentVM.students
    val selectedStudent = studentVM.selectedStudent
    val schedulePair = remember(selectedStudent) {
        selectedStudent?.schedules?.let { resolveSchedulePair(it) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bgpic),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                    )

                    HeaderIcons(
                        onCalendarClick = onCalendarClick,
                        onNotificationClick = onNotificationClick,
                        onMenuClick = { showBottomSheet = true }
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                selectedStudent?.name ?: "Loading student",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(selectedStudent?.course ?: "--", color = Color.White)
                            Text("ID number: ${selectedStudent?.rollNumber ?: "--"}", color = Color.White)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            students.forEach { student ->
                                Image(
                                    painter = painterResource(id = R.drawable.student_image),
                                    contentDescription = student.name,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = 2.dp,
                                            color = if (student.id == selectedStudent?.id) Color(0xFF8BE28B) else Color.White,
                                            shape = CircleShape
                                        )
                                        .clickable { studentVM.selectStudent(student) },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (errorMessage != null) {
                        Text(errorMessage ?: "", color = Color.Red, fontSize = 14.sp)
                    }
                    AcademicProgramSection(selectedStudent)
                    ClassScheduleSection(
                        now = schedulePair?.first,
                        next = schedulePair?.second,
                        onStudyLoadClick = onStudyLoadClick
                    )
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                StudentMenuContent(
                    onAcademicClick = onNavigateToAcademic,
                    onAttendanceClick = onNavigateToAttendance
                )
            }
        }
    }
}

@Composable
fun HeaderIcons(
    onCalendarClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.school_logo),
            contentDescription = "School Logo",
            modifier = Modifier.size(56.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Image(
                painter = painterResource(id = R.drawable.formkit_date),
                contentDescription = "Calendar",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onCalendarClick() },
                colorFilter = ColorFilter.tint(Color.White)
            )
            Image(
                painter = painterResource(id = R.drawable.ph_bell),
                contentDescription = "Notifications",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onNotificationClick() },
                colorFilter = ColorFilter.tint(Color.White)
            )
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.White,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onMenuClick() }
            )
        }
    }
}

@Composable
fun StudentMenuContent(
    onAcademicClick: () -> Unit,
    onAttendanceClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        StudentMenuItem(
            icon = Icons.Outlined.PersonOutline,
            title = "About Student",
            description = "Know the information that your student has.",
            onClick = {}
        )
        StudentMenuItem(
            icon = Icons.Default.School,
            title = "Monitor Academic",
            description = "Check the progress and milestones of your student.",
            onClick = onAcademicClick
        )
        StudentMenuItem(
            icon = Icons.Outlined.EventAvailable,
            title = "Track Attendance",
            description = "Be updated on your student's attendance.",
            onClick = onAttendanceClick
        )
    }
}

@Composable
fun StudentMenuItem(icon: ImageVector, title: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = ColorsDefaultTheme.color_On_surface
        )
        Column {
            Text(
                text = title,
                color = Color(0xFF1B4D13),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(text = description, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun AcademicProgramSection(student: Child?) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Academic Program",
            style = AppTypes.type_H1,
            color = ColorsDefaultTheme.color_Primary_green_container
        )
        ProgramItem(icon = Icons.Default.School, text = student?.program ?: "Loading program")
        ProgramItem(icon = Icons.Default.Star, text = student?.course ?: "--")
        ProgramItem(icon = Icons.Default.Verified, text = "Officially enrolled for ${student?.year ?: "current A.Y."}")
    }
}

@Composable
fun ProgramItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ColorsDefaultTheme.color_On_surface,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = AppTypes.type_Body_Small,
            color = ColorsDefaultTheme.color_On_surface.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun ClassScheduleSection(
    now: ClassSchedule?,
    next: ClassSchedule?,
    onStudyLoadClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Class Schedule",
                style = AppTypes.type_H1,
                color = ColorsDefaultTheme.color_Primary_green_container
            )
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = "Study Load",
                tint = ColorsDefaultTheme.color_Outline,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onStudyLoadClick() }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ScheduleCardSmall(
                status = "Now",
                schedule = now,
                fallbackSubject = "No class",
                fallbackRoom = "-",
                fallbackTime = "Current time",
                iconRes = R.drawable.basil_current_location_outline,
                isHighlight = true,
                modifier = Modifier.weight(1f)
            )
            ScheduleCardSmall(
                status = "Up next",
                schedule = next,
                fallbackSubject = "VACANT",
                fallbackRoom = "-",
                fallbackTime = "No next class",
                iconRes = R.drawable.ic_outline_watch_later,
                isHighlight = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ScheduleCardSmall(
    status: String,
    schedule: ClassSchedule?,
    fallbackSubject: String,
    fallbackRoom: String,
    fallbackTime: String,
    iconRes: Int,
    isHighlight: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .requiredHeight(148.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isHighlight) ColorsDefaultTheme.color_Primary_green_container else ColorsDefaultTheme.color_Surface)
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.TopStart),
            colorFilter = ColorFilter.tint(ColorsDefaultTheme.color_Primary_on_green)
        )
        Text(
            text = status,
            fontSize = 12.sp,
            color = if (isHighlight) Color.White.copy(alpha = 0.7f) else ColorsDefaultTheme.color_Outline,
            modifier = Modifier.align(Alignment.TopEnd)
        )
        Text(
            text = schedule?.subject ?: fallbackSubject,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) Color.White else ColorsDefaultTheme.color_On_surface,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = schedule?.room ?: fallbackRoom,
                fontSize = 14.sp,
                color = if (isHighlight) Color.White else ColorsDefaultTheme.color_On_surface
            )
            Text(
                text = schedule?.let { "${it.startTime} - ${it.endTime}" } ?: fallbackTime,
                fontSize = 12.sp,
                color = if (isHighlight) Color.White.copy(alpha = 0.9f) else ColorsDefaultTheme.color_On_surface
            )
        }
    }
}

private fun resolveSchedulePair(schedules: List<ClassSchedule>): Pair<ClassSchedule?, ClassSchedule?> {
    val calendar = Calendar.getInstance()
    val today = SimpleDateFormat("EEEE", Locale.US).format(calendar.time)
    val nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    val todaySchedules = schedules
        .filter { it.day.equals(today, ignoreCase = true) }
        .sortedBy { minutesFromTime(it.startTime) }

    val current = todaySchedules.firstOrNull {
        nowMinutes in minutesFromTime(it.startTime) until minutesFromTime(it.endTime)
    }
    val next = todaySchedules.firstOrNull { minutesFromTime(it.startTime) > nowMinutes }
        ?: schedules.sortedWith(compareBy<ClassSchedule> { dayOrder(it.day) }.thenBy { minutesFromTime(it.startTime) }).firstOrNull()

    return current to next
}

private fun minutesFromTime(value: String): Int {
    val parts = value.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return hour * 60 + minute
}

private fun dayOrder(day: String): Int {
    return listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        .indexOfFirst { it.equals(day, ignoreCase = true) }
        .let { if (it == -1) 99 else it }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun StudentScreenPreview() {
    ParentAppTheme {
        StudentScreen(studentVM = StudentSharedViewModel())
    }
}
