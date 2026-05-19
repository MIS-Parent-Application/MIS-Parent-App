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
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ParentAppTheme

data class SubjectAttendance(
    val subjectName: String,
    val instructor: String,
    val presentDays: Int,
    val totalDays: Int
) {
    val percentage: Float get() = if (totalDays > 0) presentDays.toFloat() / totalDays else 0f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackAttendanceContent(
    attendanceList: List<SubjectAttendance>,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Attendance",
                            style = AppTypes.type_H2,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "John B. McLure 3rd Yr. BSIT 1A",
                            style = AppTypes.type_Caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Menu action */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AttendanceSummaryCard()
            }

            item {
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

            val displayData = attendanceList.ifEmpty { getDummyAttendance() }
            items(displayData) { record ->
                SubjectAttendanceCard(record)
            }
        }
    }
}

@Composable
fun AttendanceSummaryCard() {
    // Dynamically pulls the Yellow from your secondaryContainer theme!
    val yellowRadialBrush = Brush.radialGradient(
        colors = listOf(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0f)
        ),
        radius = 800f
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFFF9FBE7)) // Fixed light background so gradient pops
                .background(yellowRadialBrush)
                .padding(24.dp)
        ) {
            Column {
                Text("Overall Attendance", style = AppTypes.type_Body_Small, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text("92", fontSize = 64.sp, fontWeight = FontWeight.Light, color = Color.Black)
                    Text("%", style = AppTypes.type_H2, color = Color.Black, modifier = Modifier.padding(bottom = 12.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AttendanceStatItem("Present", "45", Color(0xFF2E7D32))
                    AttendanceStatItem("Absent", "2", Color(0xFFD32F2F))
                    AttendanceStatItem("Late", "3", Color(0xFFF57C00))
                }
            }
        }
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
        TrackAttendanceContent(attendanceList = getDummyAttendance(), onBackClick = {})
    }
}