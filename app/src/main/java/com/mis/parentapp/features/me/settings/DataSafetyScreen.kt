package com.mis.parentapp.features.me.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mis.parentapp.features.me.UserProfileViewModel
import android.widget.Toast
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSafetyScreen(
    userProfileViewModel: UserProfileViewModel = viewModel()
) {

    val context = LocalContext.current

    var show2FADialog by remember { mutableStateOf(false) }
    var pending2FAToggle by remember { mutableStateOf(false) }

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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your data is encrypted and handled according to our school's privacy policy.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // =========================
        // TWO FACTOR AUTHENTICATION
        // =========================

        SafetyControl(
            title = "Two-Factor Authentication",
            description = "Add an extra layer of security to your account.",
            enabled = userProfileViewModel.twoFactorEnabled,

            onToggle = {

                pending2FAToggle = it
                show2FADialog = true
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        // =========================
        // LOGIN ALERTS
        // =========================
        SafetyControl(
            title = "Account Login Alerts",
            description = "Get notified if someone logs into your account.",
            enabled = userProfileViewModel.loginAlertsEnabled,

            onToggle = {
                userProfileViewModel.toggleLoginAlerts(it)
            }
        )
        Spacer(modifier = Modifier.height(32.dp))

        // =========================
        // EXPORT DATA BUTTON
        // =========================

        Button(
            onClick = {
                userProfileViewModel.requestDataExport()
                Toast.makeText(
                    context,
                    "Data export request sent to your email",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Request Data Export")
        }
    }

    // =========================
    // ENABLE / DISABLE 2FA DIALOG
    // =========================

    if (show2FADialog) {
        AlertDialog(
            onDismissRequest = {
                show2FADialog = false
            },
            title = {
                Text(
                    if (pending2FAToggle)
                        "Enable 2FA?"
                    else
                        "Disable 2FA?"
                )
            },
            text = {
                Text(
                    if (pending2FAToggle)
                        "Enabling Two-Factor Authentication will require a code from your email for every new login."
                    else
                        "Are you sure you want to disable Two-Factor Authentication? Disabling Two-Factor Authentication may make your account less secure."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        userProfileViewModel.toggleTwoFactor(
                            pending2FAToggle
                        )
                        show2FADialog = false
                        Toast.makeText(
                            context,
                            if (pending2FAToggle)
                                "2FA Enabled"
                            else
                                "2FA Disabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        show2FADialog = false
                    }
                ) {
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
