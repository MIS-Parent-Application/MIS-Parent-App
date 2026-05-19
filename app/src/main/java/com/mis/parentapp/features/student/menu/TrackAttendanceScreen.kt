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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.ui.theme.ParentAppTheme

// --- 1. DATA MODEL (Add this to your Room entities later!) ---
data class SubjectAttendance(
    val subjectName: String,
    val instructor: String,
    val presentDays: Int,
    val totalDays: Int
) {
    val percentage: Float get() = if (totalDays > 0) presentDays.toFloat() / totalDays else 0f
}

// --- 2. THE UI CONTENT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackAttendanceContent(
    attendanceList: List<SubjectAttendance>,
    onBackClick: () -> Unit,
    onMonitorAcademicClick: () -> Unit = {},
    onTrackAttendanceClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

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
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "John B. McLure 3rd Yr. BSIT 1A",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.Black
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Monitor Academic") },
                            onClick = {
                                showMenu = false
                                onMonitorAcademicClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Track Attendance") },
                            onClick = {
                                showMenu = false
                                onTrackAttendanceClick()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp), // Bottom padding for nav bar
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Overall Summary Gradient Card
            item {
                AttendanceSummaryCard()
            }

            // 2. Recent Absence Alert (Reusing the soft-red style)
            item {
                AbsenceAlertCard()
            }

            // 3. Subject Breakdown Header
            item {
                Text(
                    text = "Subject Breakdown",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // 4. List of Subjects
            val displayData = attendanceList.ifEmpty { getDummyAttendance() }
            items(displayData) { record ->
                SubjectAttendanceCard(record)
            }
        }
    }
}

// --- UI COMPONENTS ---

@Composable
fun AttendanceSummaryCard() {
    // Using the exact same premium diagonal gradient from the Academic screen
    val brush = Brush.linearGradient(
        colors = listOf(Color(0xFFF9FBE7), Color(0xFFAED581)),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(brush)
                .padding(24.dp)
        ) {
            Column {
                Text("Overall Attendance", fontSize = 16.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text("92", fontSize = 64.sp, fontWeight = FontWeight.Light, color = Color.Black)
                    Text("%", fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.padding(bottom = 12.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Breakdown Row
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
            Text(label, fontSize = 12.sp, color = Color.DarkGray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun AbsenceAlertCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0)), // Soft pink
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE53935), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Info, contentDescription = "Notice", tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Recent Absence", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                Text("Unexcused absence recorded.", fontSize = 12.sp, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Programming 2", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Oct 12", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun SubjectAttendanceCard(record: SubjectAttendance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(record.subjectName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(record.instructor, fontSize = 12.sp, color = Color.Gray)
                }
                Text(
                    text = "${(record.percentage * 100).toInt()}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (record.percentage >= 0.8f) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom Progress Bar
            LinearProgressIndicator(
                progress = { record.percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (record.percentage >= 0.8f) Color(0xFF4CAF50) else Color(0xFFEF5350),
                trackColor = Color(0xFFE0E0E0),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${record.presentDays} of ${record.totalDays} classes attended",
                fontSize = 11.sp,
                color = Color.DarkGray
            )
        }
    }
}

// --- DUMMY DATA ---
fun getDummyAttendance(): List<SubjectAttendance> {
    return listOf(
        SubjectAttendance("Math 101", "Mr. John Doe", 28, 30),
        SubjectAttendance("English 101", "Ms. Jane Smith", 25, 30),
        SubjectAttendance("Programming 2", "Dr. Alan Turing", 21, 30) // This one will show up as red!
    )
}

// --- PREVIEW ---
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun TrackAttendancePreview() {
    ParentAppTheme {
        TrackAttendanceContent(
            attendanceList = getDummyAttendance(),
            onBackClick = {}
        )
    }
}
