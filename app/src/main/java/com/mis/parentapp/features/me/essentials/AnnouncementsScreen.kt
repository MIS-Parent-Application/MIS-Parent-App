package com.mis.parentapp.features.me.essentials

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.utils.cards.AnnouncementCard
import com.mis.parentapp.utils.cards.AnnouncementData
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.utils.images.RemoteImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen() {
    var selectedTab by remember { mutableStateOf("School-wide") }
    var announcements by remember { mutableStateOf<List<AnnouncementData>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedAnnouncement by remember { mutableStateOf<AnnouncementData?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        runCatching {
            RetrofitInstance.api.getAnnouncements().map {
                AnnouncementData(
                    id = it.id.toString(),
                    title = it.title,
                    content = it.content,
                    isNew = it.urgent,
                    imageUrl = it.imageUrl,
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
                    AnnouncementCard(
                        announcement = announcement,
                        onViewClick = {
                            selectedAnnouncement = announcement
                            showBottomSheet = true
                        }
                    )
                }
            }

            if (earlierOnes.isNotEmpty()) {
                item {
                    Text(text = "Earlier", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 16.dp))
                }
                items(earlierOnes) { announcement ->
                    AnnouncementCard(
                        announcement = announcement,
                        onViewClick = {
                            selectedAnnouncement = announcement
                            showBottomSheet = true
                        }
                    )
                }
            }
        }
    }

    if (showBottomSheet && selectedAnnouncement != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            AnnouncementDetailContent(
                announcement = selectedAnnouncement!!,
                onClose = { showBottomSheet = false }
            )
        }
    }
}

@Composable
fun AnnouncementDetailContent(
    announcement: AnnouncementData,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .verticalScroll(rememberScrollState())
    ) {
        // Image Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            if (announcement.imageUrl != null) {
                RemoteImage(
                    url = announcement.imageUrl,
                    fallbackRes = com.mis.parentapp.R.drawable.colegio_de_alicia_logo,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(announcement.colors))
                )
            }
            
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = announcement.category,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = announcement.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = announcement.content,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}


