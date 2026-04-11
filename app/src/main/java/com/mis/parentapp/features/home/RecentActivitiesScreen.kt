package com.mis.parentapp.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import com.mis.parentapp.ui.theme.ParentAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentActivitiesScreen(onBackClick: () -> Unit) {
    var selectedEvent by remember { mutableStateOf<EventItem?>(null) }

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
                    actions = {
                        IconButton(onClick = { /* Handle menu */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = Color.White
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                RecentFilterRow()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        EventSection(
                            title = "School-wide",
                            events = listOf(
                                EventItem(101, "School event", "School-wide", "25.5.2026"),
                                EventItem(102, "School event", "School-wide", "25.5.2026")
                            ),
                            onEventClick = { selectedEvent = it }
                        )
                    }

                    item {
                        EventSection(
                            title = "College",
                            events = listOf(
                                EventItem(103, "College event", "College", "25.5.2026"),
                                EventItem(104, "College event", "College", "25.5.2026")
                            ),
                            onEventClick = { selectedEvent = it }
                        )
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
