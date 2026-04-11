package com.mis.parentapp.features.home

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import com.mis.parentapp.ui.theme.ParentAppTheme

data class EventItem(
    val id: Int,
    val title: String,
    val category: String,
    val date: String,
    val description: String = "Lorem ipsum dolor sit amet consectetur. Sed varius dolor ipsum mauris in lacus sapien risus vestibulum. Urna dui vitae integer bibendum. Eget sed metus eget accumsan fusce morbi id. Id vulputate euismod sed vitae pretium diam. Est nunc diam morbi neque ipsum accumsan ac ipsum. Tincidunt sed nibh lacus sed. Adispscing congue gravida at magna. Sit lacus eget sed varius nec. Et tortor at et pellentesque."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingEventsScreen(onBackClick: () -> Unit) {
    var selectedEvent by remember { mutableStateOf<EventItem?>(null) }

    if (selectedEvent != null) {
        EventDetailScreen(event = selectedEvent!!, onBackClick = { selectedEvent = null })
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Upcoming events", style = AppTypes.type_H1, fontSize = 20.sp) },
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
                EventFilterRow()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        EventSection(
                            title = "School-wide",
                            events = listOf(
                                EventItem(1, "School event", "School-wide", "25.5.2026"),
                                EventItem(2, "School event", "School-wide", "25.5.2026")
                            ),
                            onEventClick = { selectedEvent = it }
                        )
                    }

                    item {
                        EventSection(
                            title = "College",
                            events = listOf(
                                EventItem(3, "College event", "College", "25.5.2026"),
                                EventItem(4, "College event", "College", "25.5.2026")
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
fun EventFilterRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf("All", "Postponed", "Cancelled")
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

@Composable
fun EventSection(title: String, events: List<EventItem>, onEventClick: (EventItem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.Black)
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(events) { event ->
                EventCard(event = event, onClick = { onEventClick(event) })
            }
        }
    }
}

@Composable
fun EventCard(event: EventItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9).copy(alpha = 0.5f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFFFA726)) // Orange placeholder
            )
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = event.title, style = AppTypes.type_Body_Small, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = event.date, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D13)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("View", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(event: EventItem, onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFA726)) // Orange placeholder
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = event.title, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.Black)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Lorem ipsum dolor sit amet consectetur.",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = event.description,
                style = AppTypes.type_Body_Small,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = Color.Black
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpcomingEventsScreenPreview() {
    ParentAppTheme {
        UpcomingEventsScreen(onBackClick = {})
    }
}
