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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.utilities.cards.AnnouncementCard
import com.mis.parentapp.utilities.cards.AnnouncementData
import com.mis.parentapp.network.RetrofitInstance

@Composable
fun AnnouncementsScreen() {
    var selectedTab by remember { mutableStateOf("School-wide") }
    var announcements by remember { mutableStateOf<List<AnnouncementData>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching {
            RetrofitInstance.api.getAnnouncements().map {
                AnnouncementData(
                    id = it.id.toString(),
                    title = it.title,
                    content = it.content,
                    isNew = it.urgent,
                    category = when (it.category.lowercase()) {
                        "college" -> "College"
                        else -> "School-wide"
                    }
                )
            }
        }.onSuccess {
            announcements = it
            errorMessage = null
        }.onFailure {
            errorMessage = "Unable to load announcements from the server."
        }
    }

    val filteredAnnouncements = announcements.filter { it.category == selectedTab }
    val newOnes = filteredAnnouncements.filter { it.isNew }
    val earlierOnes = filteredAnnouncements.filter { !it.isNew }

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
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
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
            if (filteredAnnouncements.isEmpty()) {
                item {
                    Text(
                        text = errorMessage ?: "No announcements for $selectedTab.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (newOnes.isNotEmpty()) {
                item {
                    Text(text = "New", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
                items(newOnes) { announcement ->
                    AnnouncementCard(announcement = announcement, onViewClick = { /* View Detail */ })
                }
            }

            if (earlierOnes.isNotEmpty()) {
                item {
                    Text(text = "Earlier", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 16.dp))
                }
                items(earlierOnes) { announcement ->
                    AnnouncementCard(announcement = announcement, onViewClick = { /* View Detail */ })
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
        AnnouncementData("3", "College News", "Important update for college students.", true, "College"),
        AnnouncementData("4", "Announcement", "Earlier news item for school.", false, "School-wide")
    )
}
