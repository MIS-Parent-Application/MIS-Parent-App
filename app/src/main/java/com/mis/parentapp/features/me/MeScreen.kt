package com.mis.parentapp.features.me

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mis.parentapp.BuildConfig
import com.mis.parentapp.R
import com.mis.parentapp.features.auth.AuthViewModel
import com.mis.parentapp.features.me.sections.SettingsSection
import com.mis.parentapp.features.me.sections.YourEssentialsSection
import com.mis.parentapp.navigation.Announcements
import com.mis.parentapp.navigation.DataSafety
import com.mis.parentapp.navigation.EditProfile
import com.mis.parentapp.navigation.Feedbacks
import com.mis.parentapp.navigation.Meeting
import com.mis.parentapp.navigation.Messages
import com.mis.parentapp.navigation.Preference
import com.mis.parentapp.network.AppVersionDto
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.utilities.images.InitialsImageFallback
import com.mis.parentapp.utilities.images.RemoteImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MeScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    authViewModel: AuthViewModel,
    userProfileViewModel: UserProfileViewModel = viewModel(),
    onSignOutClick: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isWide = configuration.screenWidthDp >= 600
    val headerHeight = if (isWide) (configuration.screenHeightDp.dp * 0.5f).coerceIn(300.dp, 500.dp)
                       else (configuration.screenHeightDp.dp * 0.42f).coerceIn(260.dp, 380.dp)
    var availableUpdate by remember { mutableStateOf<AppVersionDto?>(null) }

    LaunchedEffect(Unit) {
        runCatching {
            RetrofitInstance.api.getAppVersion()
        }.onSuccess { version ->
            if (version.versionCode > BuildConfig.VERSION_CODE && !version.apkUrl.isNullOrBlank()) {
                availableUpdate = version
            }
        }
    }

    availableUpdate?.let { update ->
        AlertDialog(
            onDismissRequest = { availableUpdate = null },
            title = { Text("Update available") },
            text = {
                Text(
                    text = "Version ${update.versionName} is ready.\n\n${update.releaseNotes.orEmpty()}"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        openUpdateLink(context, update.apkUrl.orEmpty())
                        availableUpdate = null
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { availableUpdate = null }) {
                    Text("Later")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 1200.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp), // Spacing between items
            contentPadding = PaddingValues(bottom = 24.dp) // Extra bottom padding
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight)
                ) {
                    // BACKGROUND IMAGE WITH ROUNDED BOTTOM
                    val parentBackgroundUrl = userProfileViewModel.backgroundImageUrl
                        ?: userProfileViewModel.profileImageUrl

                    if (userProfileViewModel.profileBitmap != null) {
                        Image(
                            bitmap = userProfileViewModel.profileBitmap!!,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(
                                    RoundedCornerShape(
                                        bottomStart = 32.dp,
                                        bottomEnd = 32.dp
                                    )
                                )
                        )
                    } else {
                        RemoteImage(
                            url = parentBackgroundUrl,
                            fallbackRes = userProfileViewModel.profileImageRes,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(
                                    RoundedCornerShape(
                                        bottomStart = 32.dp,
                                        bottomEnd = 32.dp
                                    )
                                ),
                            fallbackContent = {
                                InitialsImageFallback(
                                    name = userProfileViewModel.fullName,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(
                                            RoundedCornerShape(
                                                bottomStart = 32.dp,
                                                bottomEnd = 32.dp
                                            )
                                        ),
                                    isLarge = true
                                )
                            }
                        )
                    }

                    // DARK OVERLAY
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(
                                RoundedCornerShape(
                                    bottomStart = 32.dp,
                                    bottomEnd = 32.dp
                                )
                            )
                            .background(Color.Black.copy(alpha = 0.25f))
                    )

                    // NAME + DETAILS + PROFILE (Match StudentScreen Layout)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // TEXT
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                userProfileViewModel.fullName,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (userProfileViewModel.isPrimaryGuardian) "Primary Guardian" else "Parent",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // VERIFIED BADGE
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF38B02D),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Verified Account",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            item {
                YourEssentialsSection(onCategoryClick = { title ->
                    when (title) {
                        "Messages" -> navController?.navigate(Messages)
                        "Announcements" -> navController?.navigate(Announcements)
                        "Meetings" -> navController?.navigate(Meeting)
                        "Feedbacks" -> navController?.navigate(Feedbacks)
                    }
                })
            }
            item {
                SettingsSection(onCategoryClick = { title ->
                    when (title) {
                        "Preferences" -> navController?.navigate(Preference)
                        "Data safety" -> navController?.navigate(DataSafety)
                        "Edit profile" -> navController?.navigate(EditProfile)
                        "Check for updates" -> checkForAppUpdate(context)
                        "Sign out" -> {
                            authViewModel.signOut {
                                onSignOutClick()
                            }
                        }
                    }
                })
            }
        }
    }
}

private fun checkForAppUpdate(context: android.content.Context) {
    CoroutineScope(Dispatchers.Main).launch {
        runCatching {
            RetrofitInstance.api.getAppVersion()
        }.onSuccess { version ->
            val apkUrl = version.apkUrl.orEmpty()
            when {
                version.versionCode <= BuildConfig.VERSION_CODE -> {
                    Toast.makeText(context, "Your app is up to date.", Toast.LENGTH_LONG).show()
                }
                apkUrl.isBlank() -> {
                    Toast.makeText(context, "Update available, but no download link is configured yet.", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(context, "Opening version ${version.versionName} update.", Toast.LENGTH_LONG).show()
                    openUpdateLink(context, apkUrl)
                }
            }
        }.onFailure {
            Toast.makeText(context, "Unable to check for updates.", Toast.LENGTH_LONG).show()
        }
    }
}

private fun openUpdateLink(context: android.content.Context, apkUrl: String) {
    context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

//@Preview(showBackground = true, widthDp = 360)
//@Composable
//private fun MeScreenPreview() {
//    ParentAppTheme {
//        MeScreen()
//    }
//}
