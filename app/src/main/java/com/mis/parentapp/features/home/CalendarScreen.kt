package com.mis.parentapp.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.network.CalendarEventDto
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ParentAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    studentVM: StudentSharedViewModel? = null,
    onBackClick: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var events by remember { mutableStateOf<List<CalendarEventDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val selectedStudent = studentVM?.selectedStudent

    LaunchedEffect(selectedStudent?.id) {
        isLoading = true
        errorMessage = null
        try {
            events = RetrofitInstance.api.getCalendarEvents(selectedStudent?.id)
        } catch (e: Exception) {
            errorMessage = "Unable to load calendar."
        } finally {
            isLoading = false
        }
    }

    val filteredEvents = events.filter {
        selectedFilter == "All" || it.category.equals(selectedFilter, ignoreCase = true) || it.status.equals(selectedFilter, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calendar", style = AppTypes.type_H1, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            CalendarFilterRow(selectedFilter = selectedFilter, onFilterClick = { selectedFilter = it })

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage ?: "", color = Color.Red)
                }
                filteredEvents.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No calendar events found.", color = Color.Gray)
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredEvents) { event ->
                        CalendarEventCard(event)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarFilterRow(selectedFilter: String, onFilterClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("All", "Academic", "School-wide", "College", "Exam", "Reminder").forEach { filter ->
            val isSelected = filter == selectedFilter
            Surface(
                modifier = Modifier.clickable { onFilterClick(filter) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = filter,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = AppTypes.type_M3_label_small
                )
            }
        }
    }
}

@Composable
private fun CalendarEventCard(event: CalendarEventDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShapeFallback),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(event.description, fontSize = 13.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                Text("${event.date} | ${event.time}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Text(event.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

private val CircleShapeFallback = RoundedCornerShape(100.dp)

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    ParentAppTheme {
        CalendarScreen(onBackClick = {})
    }
}
