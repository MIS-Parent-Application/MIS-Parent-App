package com.mis.parentapp.features.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.data.AttendanceRecord
import com.mis.parentapp.ui.theme.ColorsDefaultTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackAttendanceScreen(
    viewModel: StudentViewModel,
    onBackClick: () -> Unit
) {
    val attendanceRecords by viewModel.attendance.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Calculate Attendance Percentage
    val totalRecords = attendanceRecords.size
    val presentCount = attendanceRecords.count { it.status.equals("Present", ignoreCase = true) }
    val attendanceRate = if (totalRecords > 0) (presentCount.toFloat() / totalRecords) * 100 else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance Tracker", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = ColorsDefaultTheme.color_Primary_green_container
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ColorsDefaultTheme.color_Primary_green_container,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Record")
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorsDefaultTheme.color_Primary_green_container),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Attendance Rate", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                        Text(
                            text = "${attendanceRate.toInt()}%",
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Days: $totalRecords", color = Color.White, fontSize = 14.sp)
                        Text("Present: $presentCount", color = Color.White, fontSize = 14.sp)
                        Text("Missed: ${totalRecords - presentCount}", color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Recent Records", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ColorsDefaultTheme.color_On_surface)
            Spacer(modifier = Modifier.height(12.dp))

            if (attendanceRecords.isEmpty()) {
                Text("No attendance records yet.", color = Color.Gray, modifier = Modifier.padding(top = 16.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(attendanceRecords) { record ->
                        AttendanceItemCard(record)
                    }
                }
            }
        }

        if (showAddDialog) {
            AddAttendanceDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { date, status, reason ->
                    viewModel.addAttendance(date, status, reason)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AttendanceItemCard(record: AttendanceRecord) {
    val statusColor = when (record.status.lowercase()) {
        "present" -> ColorsDefaultTheme.color_Primary_green_container
        "absent" -> ColorsDefaultTheme.color_Error
        else -> Color(0xFFFFA000) // Orange for Late
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = record.date, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ColorsDefaultTheme.color_On_surface)
                if (!record.reason.isNullOrBlank()) {
                    Text(text = "Note: ${record.reason}", fontSize = 14.sp, color = Color.Gray)
                }
            }
            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = record.status,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
fun AddAttendanceDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var date by remember { mutableStateOf("2026-04-16") }
    var status by remember { mutableStateOf("Present") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Attendance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") })
                OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Status (Present/Absent/Late)") })
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason (Optional)") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (date.isNotBlank() && status.isNotBlank()) {
                        onAdd(date, status, reason)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ColorsDefaultTheme.color_Primary_green_container)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)) {
                Text("Cancel")
            }
        }
    )
}