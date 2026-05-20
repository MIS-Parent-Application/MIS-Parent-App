package com.mis.parentapp.features.me.essentials

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.mis.parentapp.data.AppDatabase
import com.mis.parentapp.data.EventRepository
import com.mis.parentapp.features.home.EventsViewModel
import com.mis.parentapp.features.home.menu.EventCard
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mis.parentapp.utilities.cards.AnnouncementData
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnnouncementsScreen() {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val repo = EventRepository(db.eventDao())
    val viewModel: EventsViewModel = viewModel(
        factory = EventsViewModel.provideFactory(repo)
    )
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val recentEvents by viewModel.recentEvents.collectAsState()

    var selectedTab by remember { mutableStateOf("School-wide") }

    val allEvents = upcomingEvents + recentEvents
    val filteredEvents = allEvents.filter {
        val cat = it.category.lowercase()
        if (selectedTab == "College") cat.contains("college")
        else !cat.contains("college")
    }

    val locale = LocalConfiguration.current.locales[0]
    val sdf = remember(locale) { SimpleDateFormat("yyyy-MM-dd", locale) }
    val todayStr = remember(sdf) { sdf.format(Date()) }

    val newOnes = filteredEvents.filter { it.date >= todayStr }
    val earlierOnes = filteredEvents.filter { it.date < todayStr }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("School-wide", "College").forEach { tab ->
                val isSelected = selectedTab == tab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = tab },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Text(text = tab, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (filteredEvents.isEmpty()) {
                item {
                    Text(
                        text = "No announcements for $selectedTab.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (newOnes.isNotEmpty()) {
                item {
                    Text(text = "New", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
                items(newOnes) { event ->
                    EventCard(event = event, onClick = { /* View Detail */ })
                }
            }

            if (earlierOnes.isNotEmpty()) {
                item {
                    Text(text = "Earlier", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 16.dp))
                }
                items(earlierOnes) { event ->
                    EventCard(event = event, onClick = { /* View Detail */ })
                }
            }
        }
    }
}

// Preview-only sample data.
fun getDummyAnnouncements(): List<AnnouncementData> {
    return listOf(
        AnnouncementData("1", "Announcement", "Lorem ipsum dolor sit amet consectetur...", true, "School-wide"),
        AnnouncementData("2", "Announcement", "Lorem ipsum dolor sit amet consectetur...", false, "School-wide"),
        AnnouncementData(
            "3",
            "College News",
            "Important update for college students.",
            true,
            "College"
        ),
        AnnouncementData("4", "Announcement", "Earlier news item for school.", false, "School-wide")
    )
}
