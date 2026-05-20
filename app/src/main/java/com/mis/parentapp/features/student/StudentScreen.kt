package com.mis.parentapp.features.student

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.data.StudentMonitoringDao
import com.mis.parentapp.network.Child
import com.mis.parentapp.network.ClassSchedule
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.utilities.images.RemoteImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class StudentScheduleDisplay(
    val schedule: ClassSchedule?,
    val statusLabel: String,
    val dateLabel: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    studentVM: StudentSharedViewModel,
    dao: StudentMonitoringDao,
    modifier: Modifier = Modifier,
    onStudyLoadClick: () -> Unit = {}
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val dashboard = RetrofitInstance.api.getDashboard()
            studentVM.updateStudents(dashboard.children, dashboard.unreadAnnouncements)
        } catch (_: Exception) {
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
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(modifier = Modifier.widthIn(max = 1200.dp)) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    RemoteImage(
                        url = selectedStudent?.backgroundImageUrl,
                        fallbackRes = R.drawable.bgpic,
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

                        LazyRow(
                            modifier = Modifier
                                .widthIn(max = 176.dp)
                                .padding(start = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(students, key = { it.id }) { student ->
                                RemoteImage(
                                    url = student.profileImageUrl,
                                    fallbackRes = R.drawable.student_image,
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
                        .background(MaterialTheme.colorScheme.background)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (errorMessage != null) {
                        Text(errorMessage ?: "", color = Color.Red, fontSize = 14.sp)
                    }
                    
                    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                    val isWide = configuration.screenWidthDp >= 600

                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                AcademicProgramSection(selectedStudent)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                ClassScheduleSection(
                                    now = schedulePair?.first,
                                    next = schedulePair?.second,
                                    onStudyLoadClick = onStudyLoadClick
                                )
                            }
                        }
                    } else {
                        AcademicProgramSection(selectedStudent)
                        ClassScheduleSection(
                            now = schedulePair?.first,
                            next = schedulePair?.second,
                            onStudyLoadClick = onStudyLoadClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AcademicProgramSection(student: Child?) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Academic Program",
            style = AppTypes.type_H1,
            color = MaterialTheme.colorScheme.primary
        )
        ProgramItem(icon = Icons.Default.School, text = student?.program ?: "Loading program")
        ProgramItem(icon = Icons.Default.Star, text = "Current GPA: ${student?.gpa ?: "--"}")
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
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = AppTypes.type_Body_Small,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun ClassScheduleSection(
    now: StudentScheduleDisplay?,
    next: StudentScheduleDisplay?,
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
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = "Study Load",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onStudyLoadClick() }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScheduleCardSmall(
                status = now?.statusLabel ?: "Now",
                date = now?.dateLabel ?: "",
                schedule = now?.schedule,
                fallbackSubject = "No class",
                fallbackRoom = "-",
                fallbackTime = "No class now",
                iconRes = R.drawable.basil_current_location_outline,
                isHighlight = true,
                modifier = Modifier.weight(1f)
            )
            ScheduleCardSmall(
                status = next?.statusLabel ?: "Up next",
                date = next?.dateLabel ?: "",
                schedule = next?.schedule,
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
    date: String,
    schedule: ClassSchedule?,
    fallbackSubject: String,
    fallbackRoom: String,
    fallbackTime: String,
    iconRes: Int,
    isHighlight: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 168.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                colorFilter = ColorFilter.tint(if (isHighlight) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = status,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isHighlight) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
                if (date.isNotEmpty()) {
                    Text(
                        text = date,
                        fontSize = 10.sp,
                        color = if (isHighlight) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        Text(
            text = schedule?.subject ?: fallbackSubject,
            fontSize = 15.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = schedule?.room ?: fallbackRoom,
                fontSize = 14.sp,
                color = if (isHighlight) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = schedule?.let { "${it.startTime} - ${it.endTime}" } ?: fallbackTime,
                fontSize = 12.sp,
                color = if (isHighlight) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun resolveSchedulePair(schedules: List<ClassSchedule>): Pair<StudentScheduleDisplay, StudentScheduleDisplay> {
    val calendar = Calendar.getInstance()
    val todayName = SimpleDateFormat("EEEE", Locale.US).format(calendar.time)
    val nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val todayDateStr = dateFormatter.format(calendar.time)

    val todaySchedules = schedules
        .filter { it.day.equals(todayName, ignoreCase = true) }
        .sortedBy { minutesFromTime(it.startTime) }

    val current = todaySchedules.firstOrNull {
        nowMinutes in minutesFromTime(it.startTime) until minutesFromTime(it.endTime)
    }

    val currentDisplay = StudentScheduleDisplay(
        schedule = current,
        statusLabel = "Now",
        dateLabel = todayDateStr
    )

    var nextSchedule: ClassSchedule? = todaySchedules.firstOrNull { minutesFromTime(it.startTime) > nowMinutes }
    var nextStatus = "Up next"
    var nextDate = todayDateStr

    if (nextSchedule == null && schedules.isNotEmpty()) {
        val todayIdx = dayOrder(todayName)
        val sortedAll = schedules.sortedWith(compareBy<ClassSchedule> { dayOrder(it.day) }.thenBy { minutesFromTime(it.startTime) })

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
        }
    }

    val nextDisplay = StudentScheduleDisplay(
        schedule = nextSchedule,
        statusLabel = nextStatus,
        dateLabel = nextDate
    )

    return currentDisplay to nextDisplay
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


