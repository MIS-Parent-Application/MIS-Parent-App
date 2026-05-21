package com.mis.parentapp.features.home.menu

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    studentId: Int? = null,
    autoSelectEventId: Int? = null,
    onBackClick: () -> Unit,
    onDetailTopBarChange: (Boolean, (() -> Unit)?, (() -> Unit)?) -> Unit
) {
    val context = LocalContext.current
    val viewModel: EventsViewModel = viewModel(
        factory = EventsViewModel.provideFactory(EventRepository())
    )

    val events by viewModel.recentEvents.collectAsState(initial = emptyList())
    val selectedFilter = remember { mutableStateOf("All") }
    var selectedEvent by remember { mutableStateOf<EventItem?>(null) }

    LaunchedEffect(studentId) {
        viewModel.refreshData(studentId)
    }

    LaunchedEffect(events, autoSelectEventId) {
        if (autoSelectEventId != null && selectedEvent == null && events.isNotEmpty()) {
            val matchingEvent = events.find { it.id == autoSelectEventId }
            if (matchingEvent != null) {
                selectedEvent = matchingEvent
            }
        }
    }

    BackHandler(enabled = selectedEvent != null) {
        selectedEvent = null
        onDetailTopBarChange(false, null, null)
    }

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
                        else -> true
                    }
                } else true
            } catch (_: Exception) {
                true
            }
        }
    }

    val groupedEvents = filteredEvents.groupBy { it.category }

    DisposableEffect(selectedEvent) {
        if (selectedEvent != null) {
            onDetailTopBarChange(
                true,
                {
                    selectedEvent = null
                },
                { shareActivity(context, selectedEvent!!) }
            )
        } else {
            onDetailTopBarChange(false, null, null)
        }
        onDispose {
            onDetailTopBarChange(false, null, null)
        }
    }

    if (selectedEvent != null) {
        EventDetailScreen(event = selectedEvent!!)
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
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
                            onEventClick = { selectedEvent = it }
                        )
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

private fun shareActivity(context: Context, event: EventItem) {
    val shareText = buildString {
        appendLine("Activity: ${event.title}")
        appendLine("Date: ${event.date}")
        appendLine()
        appendLine(event.description)
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, event.title)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share Activity"))
}
