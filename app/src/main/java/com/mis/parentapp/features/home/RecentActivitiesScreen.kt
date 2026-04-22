package com.mis.parentapp.features.home

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mis.parentapp.data.AppDatabase
import com.mis.parentapp.data.EventItem
import com.mis.parentapp.data.EventRepository
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import com.mis.parentapp.ui.theme.ParentAppTheme

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
    
    // Fix: Remove redundant 'initial' to help type inference
    val events by viewModel.recentEvents.collectAsState()
    
    var selectedEvent by remember { mutableStateOf<EventItem?>(null) }
    val groupedEvents = events.groupBy { it.category }

    if (selectedEvent != null) {
        EventDetailScreen(event = selectedEvent!!, onBackClick = { selectedEvent = null })
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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                RecentFilterRow()

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
