package com.mis.parentapp.features.student.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Added the missing TextOverflow import!
import androidx.compose.ui.text.style.TextOverflow
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ParentAppTheme
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.shared.StudentSharedViewModel

data class SubjectAttendance(
    val subjectName: String,
    val instructor: String,
    val presentDays: Int,
    val totalDays: Int
) {
    val percentage: Float get() = if (totalDays > 0) presentDays.toFloat() / totalDays else 0f
}

// --- 1. THE WRAPPER ---
// Removed unused parameters to clear the yellow warnings
@Composable
fun TrackAttendanceScreen(
    studentVM: StudentSharedViewModel,
    onBackClick: () -> Unit
) {
    val selectedStudent = studentVM.selectedStudent
    var attendanceList by remember { mutableStateOf<List<SubjectAttendance>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedStudent?.id) {
        attendanceList = emptyList()
        errorMessage = null
        val studentId = selectedStudent?.id ?: return@LaunchedEffect
        runCatching {
            RetrofitInstance.api.getStudentAttendance(studentId).map {
                SubjectAttendance(
                    subjectName = it.subjectName,
                    instructor = it.instructor,
                    presentDays = it.presentDays,
                    totalDays = it.totalDays
                )
            }
        }.onSuccess {
            attendanceList = it
        }.onFailure {
            errorMessage = "Unable to load attendance from the server."
        }
    }

    TrackAttendanceContent(
        attendanceList = attendanceList,
        emptyMessage = errorMessage ?: "No official attendance records yet."
    )
}

// --- 2. THE UI CONTENT ---
@Composable
fun TrackAttendanceContent(
    attendanceList: List<SubjectAttendance>,
    emptyMessage: String
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AttendanceSummaryCard(attendanceList)
        }

        item {
            // We are borrowing CustomAlertCard from MonitorAcademicScreen.kt now!
            CustomAlertCard(
                title = "Recent Absence",
                description = "Unexcused absence recorded.",
                trailingText = "Programming 2",
                trailingSubText = "Oct 12",
                icon = Icons.Default.Info,
                iconBackgroundColor = MaterialTheme.colorScheme.error,
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Text(
                text = "Subject Breakdown",
                style = AppTypes.type_H2.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        if (attendanceList.isEmpty()) {
            item { EmptyAttendanceMessage(emptyMessage) }
        } else {
            items(attendanceList) { record ->
                SubjectAttendanceCard(record)
            }
        }
    }
}

// --- 3. UI COMPONENTS ---

@Composable
fun AttendanceSummaryCard(attendanceList: List<SubjectAttendance>) {
    val yellowRadialBrush = Brush.radialGradient(
        colors = listOf(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0f)
        ),
        radius = 1500f,
        center = Offset(0f, 0f)
    )

    val present = attendanceList.sumOf { it.presentDays }
    val total = attendanceList.sumOf { it.totalDays }
    val absent = (total - present).coerceAtLeast(0)
    val percent = if (total > 0) (present * 100 / total) else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFFF9FBE7))
                .background(yellowRadialBrush)
                .padding(24.dp)
        ) {
            Column {
                Text("Overall Attendance", style = AppTypes.type_Body_Small, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(percent.toString(), fontSize = 64.sp, fontWeight = FontWeight.Light, color = Color.Black)
                    Text("%", fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.padding(bottom = 12.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AttendanceStatItem("Present", present.toString(), Color(0xFF2E7D32))
                    AttendanceStatItem("Absent", absent.toString(), Color(0xFFD32F2F))
                    AttendanceStatItem("Total", total.toString(), Color(0xFFF57C00))
                }
            }
        }
    }
}

@Composable
fun EmptyAttendanceMessage(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF6FDE7)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp,
            color = Color(0xFF1B5E20)
        )
    }
}

@Composable
fun AttendanceStatItem(label: String, value: String, dotColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(label, style = AppTypes.type_Caption, color = Color.DarkGray)
            Text(value, style = AppTypes.type_Body_Small.copy(fontWeight = FontWeight.Bold), color = Color.Black)
        }
    }
}

@Composable
fun SubjectAttendanceCard(record: SubjectAttendance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(record.subjectName, style = AppTypes.type_Body_Small.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text(record.instructor, style = AppTypes.type_Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = "${(record.percentage * 100).toInt()}%",
                    style = AppTypes.type_H2.copy(fontSize = 18.sp),
                    color = if (record.percentage >= 0.8f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { record.percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (record.percentage >= 0.8f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${record.presentDays} of ${record.totalDays} classes attended",
                style = AppTypes.type_M3_label_small,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --- PREVIEW DATA ---
fun getDummyAttendance(): List<SubjectAttendance> {
    return listOf(
        SubjectAttendance("Math 101", "Mr. John Doe", 28, 30),
        SubjectAttendance("English 101", "Ms. Jane Smith", 25, 30),
        SubjectAttendance("Programming 2", "Dr. Alan Turing", 21, 30)
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun TrackAttendancePreview() {
    ParentAppTheme {
        TrackAttendanceContent(
            attendanceList = getDummyAttendance(),
            emptyMessage = "No attendance yet"
        )
    }
}