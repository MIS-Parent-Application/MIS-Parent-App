package com.mis.parentapp.features.me.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PreferenceScreen() {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf("English") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Text(
            text = "App Preferences",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        PreferenceItem(
            title = "Push Notifications",
            description = "Receive alerts for school activities.",
            trailing = {
                Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        PreferenceItem(
            title = "Language",
            description = language,
            trailing = {
                TextButton(onClick = { /* Change language */ }) {
                    Text("Change")
                }
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        PreferenceItem(
            title = "Privacy Settings",
            description = "Manage who can see your activity.",
            trailing = {
                IconButton(onClick = { /* Open Privacy */ }) {
                    /* Icon(Icons.Default.ChevronRight, contentDescription = null) */
                }
            }
        )
    }
}

@Composable
fun PreferenceItem(title: String, description: String, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}
