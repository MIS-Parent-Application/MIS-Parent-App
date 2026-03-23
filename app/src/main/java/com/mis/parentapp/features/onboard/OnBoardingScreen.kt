package com.mis.parentapp.features.onboard

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.ui.theme.AppTypes
import kotlinx.coroutines.delay

@Composable
fun OnBoardingScreen(
    onSignInClick: (Int) -> Unit = {},
    onCreateAccountClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val images = listOf(
        R.drawable.bg_one_sign_screen,
        R.drawable.bg_two_sign_screen,
        R.drawable.bg_three_sign_screen,
        R.drawable.bg_four_sign_screen
    )

    var currentImageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(15000) // 15 seconds
            currentImageIndex = (currentImageIndex + 1) % images.size
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Background Image Slider
        Crossfade(
            targetState = images[currentImageIndex],
            animationSpec = tween(durationMillis = 2000),
            label = "BackgroundCrossfade"
        ) { imageRes ->
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // 2. Dark/Green Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color(0xFF0D230D).copy(alpha = 0.85f)
                        ),
                        startY = 0f
                    )
                )
        )

        // 3. Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 50.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Logo and School Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.coldea_logo_jk1jkwfg_1),
                    contentDescription = "Logo",
                    modifier = Modifier.size(64.dp)
                )
                Column {
                    Text(
                        text = "Colegio De Alicia",
                        color = Color.White,
                        style = AppTypes.type_H2,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Alicia, Bohol, Philippines",
                        color = Color(0xFFC0CA33), // Yellow-green tint
                        style = AppTypes.type_Caption
                    )
                }
            }

            // Middle: Slogan
            Text(
                text = "Stay connected to their classroom, anywhere, anytime.",
                color = Color.White,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 42.sp
                ),
                modifier = Modifier.padding(bottom = 60.dp)
            )

            // Bottom: Buttons and Socials
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Sign-in Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1B5E20))
                        .clickable { onSignInClick(images[currentImageIndex]) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Sign-in", color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Create account Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFF1F8E9))
                        .clickable { onCreateAccountClick(images[currentImageIndex]) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Create an account", color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Social Links
                Column(
                    modifier = Modifier.wrapContentWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    SocialLinkRow(icon = Icons.Filled.Key, text = "Other sign-in options")
                    SocialLinkRow(painterRes = R.drawable.coldea_logo_jk1jkwfg_1, text = "Sign in with Google")
                    SocialLinkRow(icon = Icons.Filled.Facebook, text = "Sign in with Facebook")
                }
            }
        }
    }
}

@Composable
fun SocialLinkRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    painterRes: Int? = null,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (icon != null) {
            Image(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
        } else if (painterRes != null) {
            Image(
                painter = painterResource(id = painterRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = text,
            color = Color.White,
            style = AppTypes.type_Body_Small
        )
    }
}

@Preview(widthDp = 412, heightDp = 917)
@Composable
private fun OnBoardingScreenPreview() {
    OnBoardingScreen()
}
