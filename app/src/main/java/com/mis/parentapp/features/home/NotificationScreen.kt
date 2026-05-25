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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.utils.cards.NotificationCard
import com.mis.parentapp.utils.cards.NotificationData
import com.mis.parentapp.utils.cards.NotificationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    studentVM: StudentSharedViewModel? = null,
    onBackClick: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var notifications by remember { mutableStateOf<List<NotificationData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val selectedStudent = studentVM?.selectedStudent

    LaunchedEffect(selectedStudent?.id) {
        isLoading = true
        errorMessage = null
        try {
            notifications = RetrofitInstance.api
                .getNotifications(selectedStudent?.id)
                .map { dto ->
                    NotificationData(
                        id = dto.id.toString(),
                        type = try { NotificationType.valueOf(dto.type.uppercase()) } catch (_: Exception) { NotificationType.ACTIVITY },
                        content = dto.text,
                        category = dto.type.replaceFirstChar { it.uppercase() },
                        timeAgo = dto.time,
                        isNew = dto.isNew,
                        imageUrl = dto.imageUrl,
                        gradientColors = if (dto.type.lowercase() == "event") listOf(Color(0xFFFFA726), Color(0xFFFF7043)) else null
                    )
                }
        } catch (e: Exception) {
            errorMessage = "Unable to load notifications."
        } finally {
            isLoading = false
        }
    }

    val filteredNotifications = notifications.filter {
        val normalizedFilter = selectedFilter.removeSuffix("s")
        selectedFilter == "All" ||
                (selectedFilter == "Unread" && it.isNew) ||
                it.type.name.equals(normalizedFilter, ignoreCase = true) ||
                it.category.equals(selectedFilter, ignoreCase = true) ||
                it.category.equals(normalizedFilter, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                NotificationFilterRow(
                    selectedFilter = selectedFilter,
                    onFilterClick = { selectedFilter = it }
                )
            }
            // Retained screen-specific context menu action
            IconButton(
                onClick = { /* Menu */ },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val newOnes = filteredNotifications.filter { it.isNew }
                val earlierOnes = filteredNotifications.filter { !it.isNew }

                if (newOnes.isNotEmpty()) {
                    item { Text("New", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp)) }
                    items(newOnes) { notification ->
                        NotificationCard(notification)
                    }
                }

                if (earlierOnes.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { Text("Earlier", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp)) }
                    items(earlierOnes) { notification ->
                        NotificationCard(notification)
                    }
                }

                if (filteredNotifications.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text("No notifications found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationFilterRow(selectedFilter: String, onFilterClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf("All", "Unread", "Events", "Reminders", "Messages", "School-wide", "Emergency", "College")
        filters.forEach { filter ->
            val isSelected = filter == selectedFilter
            Surface(
                modifier = Modifier.clickable { onFilterClick(filter) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    text = filter,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
