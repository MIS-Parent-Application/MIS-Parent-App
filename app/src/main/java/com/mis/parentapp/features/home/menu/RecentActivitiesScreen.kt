package com.mis.parentapp.features.home.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mis.parentapp.data.AppDatabase
import com.mis.parentapp.data.EventItem
import com.mis.parentapp.data.EventRepository
import com.mis.parentapp.features.home.EventsViewModel
import com.mis.parentapp.ui.theme.AppTypes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentActivitiesScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: EventsViewModel = viewModel(
        factory = EventsViewModel.provideFactory(
            EventRepository(AppDatabase.getDatabase(context).eventDao())
        )
    )

    val events by viewModel.recentEvents.collectAsState(initial = emptyList())
    val selectedFilter = remember { mutableStateOf("All") } // Track state here
    val selectedEvent = remember { mutableStateOf<EventItem?>(null) }

    val filteredEvents = remember(events, selectedFilter.value) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)
        val todayStr = sdf.format(now.time)

        events.filter { event ->
            try {
                val eventCal = Calendar.getInstance()
                val parsedDate = sdf.parse(event.date)
                if (parsedDate != null) {
                    eventCal.time = parsedDate
                    when (selectedFilter.value) {
                        "Today" -> event.date == todayStr
                        "This month" -> eventCal.get(Calendar.MONTH) == currentMonth &&
                                eventCal.get(Calendar.YEAR) == currentYear
                        "This year" -> eventCal.get(Calendar.YEAR) == currentYear
                        else -> true // "All"
                    }
                } else true
            } catch (_: Exception) {
                true
            }
        }
    }

    val groupedEvents = filteredEvents.groupBy { it.category }
    if (selectedEvent.value != null) {
        EventDetailScreen(
            event = selectedEvent.value!!,
            onBackClick = { selectedEvent.value = null })
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Recent events", style = AppTypes.type_H1, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                // Pass the state and the setter to the row
                RecentFilterRow(
                    selectedFilter = selectedFilter.value,
                    onFilterSelected = { selectedFilter.value = it }
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    groupedEvents.forEach { (category, eventList) ->
                        item {
                            EventSection(
                                title = category,
                                events = eventList,
                                onEventClick = { selectedEvent.value = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentFilterRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf("All", "Today", "This month", "This year")
        filters.forEach { filter ->
            val isSelected = filter == selectedFilter
            Surface(
                modifier = Modifier.clickable { onFilterSelected(filter) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = filter,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = AppTypes.type_M3_label_small
                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun RecentActivitiesScreenPreview() {
//    ParentAppTheme {
//        RecentActivitiesScreen(onBackClick = {})
//    }
//}
