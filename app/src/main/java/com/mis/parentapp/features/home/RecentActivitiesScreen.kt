package com.mis.parentapp.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mis.parentapp.data.EventItem
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import com.mis.parentapp.ui.theme.ParentAppTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentActivitiesScreen(
    onBackClick: () -> Unit,
    viewModel: EventsViewModel = viewModel()
) {
    // 1. Explicitly specify the type to fix "Cannot infer type"
    // 2. Ensure the name matches (recentEvents vs eventsState)
    val events by viewModel.recentEvents.collectAsState(initial = emptyList<EventItem>())

    var selectedEvent by remember { mutableStateOf<EventItem?>(null) }

    // Grouping logic
    val groupedEvents = events.groupBy { it.category }

    // ... rest of your Scaffold code
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        groupedEvents.forEach { (category, eventList) ->
            item {
                EventSection(
                    title = category,
                    events = eventList, // Now properly typed
                    onEventClick = { selectedEvent = it }
                )
            }
        }
    }
}


@Composable
fun RecentFilterRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf("All", "Today", "This month", "This year")
        filters.forEach { filter ->
            val isSelected = filter == "All"
            Surface(
                modifier = Modifier.clickable { },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) ColorsDefaultTheme.color_Primary_green else Color(0xFFF1F8E9)
            ) {
                Text(
                    text = filter,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) Color.White else ColorsDefaultTheme.color_Primary_green,
                    style = AppTypes.type_M3_label_small
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecentActivitiesScreenPreview() {
    ParentAppTheme {
        RecentActivitiesScreen(onBackClick = {})
    }
}
