package com.mis.parentapp.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
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

data class NotificationItem(
    val id: Int,
    val text: String,
    val type: String,
    val time: String,
    val imageRes: Int? = null,
    val isNew: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(onBackClick: () -> Unit) {
    val notifications = listOf(
        NotificationItem(1, "Lorem ipsum dolor sit amet consectetur. Auctor platea viverra dui amet odio id.", "Student", "4hrs ago", R.drawable.studentswitcher, true),
        NotificationItem(2, "Lorem ipsum dolor sit amet consectetur. Auctor platea viverra dui amet odio id.", "Instructor", "4hrs ago", null, true),
        NotificationItem(3, "Lorem ipsum dolor sit amet consectetur. Auctor platea viverra dui amet odio id.", "Student", "4hrs ago", R.drawable.studentswitcher, false),
        NotificationItem(4, "Lorem ipsum dolor sit amet consectetur. Auctor platea viverra dui amet odio id.", "Instructor", "4hrs ago", null, false)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notifications", style = AppTypes.type_H1, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            NotificationFilterRow()
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Text("New", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black) }
                items(notifications.filter { it.isNew }) { notification ->
                    NotificationCard(notification)
                }
                
                item { Spacer(modifier = Modifier.height(8.dp)) }
                
                item { Text("Earlier", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black) }
                items(notifications.filter { !it.isNew }) { notification ->
                    NotificationCard(notification)
                }
            }
        }
    }
}

@Composable
fun NotificationFilterRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf("All", "Unread", "Events", "Reminders", "Messages", "School-wide", "Emergency", "College")
        filters.forEach { filter ->
            val isSelected = filter == "All"
            Surface(
                modifier = Modifier.clickable { },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) ColorsDefaultTheme.color_Primary_green else Color(0xFFF1F8E9)
            ) {
                Text(
                    text = filter,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = if (isSelected) Color.White else ColorsDefaultTheme.color_Primary_green,
                    style = AppTypes.type_M3_label_small
                )
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (notification.imageRes != null) {
                Image(
                    painter = painterResource(id = notification.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5722))
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.text,
                    style = AppTypes.type_Body_Small,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(horizontalAlignment = Alignment.End) {
                Text(text = notification.type, fontSize = 10.sp, color = Color.Gray)
                Text(text = notification.time, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    ParentAppTheme {
        NotificationScreen(onBackClick = {})
    }
}
