package com.mis.parentapp.features.me.settings

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mis.parentapp.shared.AppSettingsViewModel
import com.mis.parentapp.features.me.UserProfileViewModel
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File

@Composable
fun DataSafetyScreen(
    settingsViewModel: AppSettingsViewModel = viewModel(LocalActivity.current as ComponentActivity),
    userProfileViewModel: UserProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    var show2FADialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Data Safety",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Your data is encrypted and handled according to our school's privacy policy.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        SafetyControl(
            title = "Two-Factor Authentication",
            description = "Add an extra layer of security to your account.",
            enabled = settingsViewModel.twoFactorEnabled,
            onToggle = { enabled ->
                if (enabled) {
                    show2FADialog = true
                } else {
                    settingsViewModel.setTwoFactor(false)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SafetyControl(
            title = "Account Login Alerts",
            description = "Get notified if someone logs into your account.",
            enabled = settingsViewModel.loginAlertsEnabled,
            onToggle = { settingsViewModel.setLoginAlerts(it) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { exportUserData(context, userProfileViewModel) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
        ) {
            Text("Request Data Export")
        }
    }

    if (show2FADialog) {
        AlertDialog(
            onDismissRequest = { show2FADialog = false },
            title = { Text("Enable 2FA") },
            text = { Text("Actual Two-Factor Authentication will be linked to your phone number: ${userProfileViewModel.phoneNumber}. A verification code will be required during your next login.") },
            confirmButton = {
                TextButton(onClick = {
                    settingsViewModel.setTwoFactor(true)
                    show2FADialog = false
                }) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { show2FADialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SafetyControl(title: String, description: String, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

private fun exportUserData(context: Context, userVM: UserProfileViewModel) {
    try {
        val data = JSONObject().apply {
            put("full_name", userVM.fullName)
            put("email", userVM.email)
            put("phone_number", userVM.phoneNumber)
            put("is_primary_guardian", userVM.isPrimaryGuardian)
            put("export_date", java.util.Date().toString())
        }

        val file = File(context.cacheDir, "my_data_export.json")
        file.writeText(data.toString(4))

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share My Data"))
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Export failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}
