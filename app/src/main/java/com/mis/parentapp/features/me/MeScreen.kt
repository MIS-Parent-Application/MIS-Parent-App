package com.mis.parentapp.features.me

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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

@Composable
fun MeScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    authViewModel: AuthViewModel,
    userProfileViewModel: UserProfileViewModel = viewModel(),
    onSignOutClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp), // Spacing between items
            contentPadding = PaddingValues(bottom = 24.dp) // Extra bottom padding
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp) // Match StudentScreen height
                ) {
                    // BACKGROUND IMAGE WITH ROUNDED BOTTOM
                    Image(
                        painter = painterResource(id = userProfileViewModel.profileImageRes),
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
                                fontWeight = FontWeight.Bold
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
                                fontWeight = FontWeight.SemiBold
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

//@Preview(showBackground = true, widthDp = 360)
//@Composable
//private fun MeScreenPreview() {
//    ParentAppTheme {
//        MeScreen()
//    }
//}
