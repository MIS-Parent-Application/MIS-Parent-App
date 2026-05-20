package com.mis.parentapp.features.me.settings

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mis.parentapp.shared.AppSettingsViewModel
import com.mis.parentapp.shared.ThemeMode

@Composable
fun PreferenceScreen(
    settingsViewModel: AppSettingsViewModel = viewModel(LocalActivity.current as ComponentActivity)
) {
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
                Switch(
                    checked = settingsViewModel.pushNotificationsEnabled,
                    onCheckedChange = { settingsViewModel.setPushNotifications(it) }
                )
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Theme",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Choose how the app looks on your device.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Column(Modifier.selectableGroup()) {
            ThemeMode.entries.forEach { mode ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .selectable(
                            selected = (settingsViewModel.themeMode == mode),
                            onClick = { settingsViewModel.setTheme(mode) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (settingsViewModel.themeMode == mode),
                        onClick = null // null recommended for accessibility with selectable modifier
                    )
                    Text(
                        text = when(mode) {
                            ThemeMode.LIGHT -> "Light Mode"
                            ThemeMode.DARK -> "Dark Mode"
                            ThemeMode.SYSTEM -> "System Default"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        PreferenceItem(
            title = "Privacy Settings",
            description = "Hide your activity from other parents.",
            trailing = {
                Switch(
                    checked = settingsViewModel.privacyEnabled,
                    onCheckedChange = { settingsViewModel.setPrivacy(it) }
                )
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
