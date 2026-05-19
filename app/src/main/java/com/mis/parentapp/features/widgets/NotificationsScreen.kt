package com.mis.parentapp.features.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.utilities.cards.NotificationCard
import com.mis.parentapp.utilities.cards.NotificationData
import com.mis.parentapp.utilities.cards.NotificationType

@Composable
fun NotificationsWidget() {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Unread", "Events", "Reminders", "Messages", "School-wide", "Emergency", "College")

    val notifications = remember { getDummyNotifications() }

    val filteredNotifications = if (selectedFilter == "All") {
        notifications
    } else {
        notifications.filter { 
            when (selectedFilter) {
                "Unread" -> it.isNew
                "Events" -> it.type == NotificationType.EVENT
                "Reminders" -> it.type == NotificationType.REMINDER
                "Messages" -> it.type == NotificationType.MESSAGE
                "Emergency" -> it.type == NotificationType.EMERGENCY
                else -> true
            }
        }
    }

    val newNotifications = filteredNotifications.filter { it.isNew }
    val earlierNotifications = filteredNotifications.filter { !it.isNew }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Filters
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.take(5).forEach { filter ->
                    FilterChip(
                        label = filter,
                        isSelected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.drop(5).forEach { filter ->
                    FilterChip(
                        label = filter,
                        isSelected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (newNotifications.isNotEmpty()) {
                item {
                    Text(
                        text = "New",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(newNotifications) { notification ->
                    NotificationCard(notification = notification)
                }
            }

            if (earlierNotifications.isNotEmpty()) {
                item {
                    Text(
                        text = "Earlier",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(earlierNotifications) { notification ->
                    NotificationCard(notification = notification)
                }
            }
            
            if (filteredNotifications.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp)) {
                        Text(
                            text = "No notifications found",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun getDummyNotifications(): List<NotificationData> {
    return listOf(
        NotificationData(
            id = "1",
            type = NotificationType.MESSAGE,
            content = "Lorem ipsum dolor sit amet consectetur. Auctor platea viverra dui amet odio id.",
            category = "Student",
            timeAgo = "4hrs ago",
            isNew = true,
            imageRes = R.drawable.student_image
        ),
        NotificationData(
            id = "2",
            type = NotificationType.EVENT,
            content = "Lorem ipsum dolor sit amet consectetur. Auctor platea viverra dui amet odio id.",
            category = "Instructor",
            timeAgo = "4hrs ago",
            isNew = true,
            gradientColors = listOf(Color(0xFFFF5252), Color(0xFFFF8A80))
        ),
        NotificationData(
            id = "3",
            type = NotificationType.ATTENDANCE,
            content = "Your child was marked absent in Math 101 class today.",
            category = "School",
            timeAgo = "1 day ago",
            isNew = false,
            imageRes = R.drawable.student_image
        ),
        NotificationData(
            id = "4",
            type = NotificationType.ACTIVITY,
            content = "New activity posted in Science 102. Deadline tomorrow.",
            category = "Instructor",
            timeAgo = "2 days ago",
            isNew = false,
            gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF81C784))
        ),
        NotificationData(
            id = "5",
            type = NotificationType.REMINDER,
            content = "Don't forget the upcoming PTA meeting this Friday.",
            category = "School",
            timeAgo = "3 days ago",
            isNew = false,
            gradientColors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
        ),
        NotificationData(
            id = "6",
            type = NotificationType.EMERGENCY,
            content = "Classes suspended due to bad weather conditions.",
            category = "Emergency",
            timeAgo = "4 days ago",
            isNew = false,
            gradientColors = listOf(Color(0xFFF44336), Color(0xFFE91E63))
        )
    )
}
