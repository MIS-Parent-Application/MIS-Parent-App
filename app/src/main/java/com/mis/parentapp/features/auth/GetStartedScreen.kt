package com.mis.parentapp.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import com.mis.parentapp.ui.theme.ParentAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

@Composable
fun GetStartedScreen(
    onNavigateToSignIn: (Int) -> Unit
) {
    val backgroundImages = listOf(
        R.drawable.bg_one_sign_screen,
        R.drawable.bg_two_sign_screen,
        R.drawable.bg_three_sign_screen,
        R.drawable.bg_four_sign_screen
    )

    val pagerState = rememberPagerState(pageCount = { Int.MAX_VALUE })

    LaunchedEffect(Unit) {
        while (true) {
            yield()
            delay(5000)
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. DIMMED BACKGROUND SLIDESHOW
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false
        ) { page ->
            val imageResId = backgroundImages[page % backgroundImages.size]
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop, // Added missing comma here
                colorFilter = ColorFilter.tint(
                    color = Color.Black.copy(alpha = 0.3f),
                    blendMode = androidx.compose.ui.graphics.BlendMode.Darken
                )
            )
        }

        // 2. BOTTOM GRADIENT (Adjusted startY to ensure text is covered by the dark area)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.26f),
                            ColorsDefaultTheme.color_On_yellow.copy(alpha = 0.20f),
                            ColorsDefaultTheme.color_Primary_green_container.copy(alpha = 0.90f),
                            ColorsDefaultTheme.color_Primary_green_container
                        ),
                        startY = 600f // Lowered start point to begin darkening where text starts
                    )
                )
        )

        // 3. UI CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.Top, // Changed to Top to respect the fixed spacer
            horizontalAlignment = Alignment.Start
        ) {
            Image(
                painter = painterResource(id = R.drawable.coldea_logo_jk1jkwfg_1),
                contentDescription = "Logo",
                modifier = Modifier.size(85.dp)
            )

            // FIXED SPACE: 366.dp between Logo and Text
            Spacer(modifier = Modifier.height(366.dp))

            Text(
                text = stringResource(id = R.string.get_started_msg),
                color = ColorsDefaultTheme.text_color,
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 44.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = R.string.get_started_sub_msg),
                color = ColorsDefaultTheme.text_color.copy(alpha = 0.8f),
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.weight(50f)) // Pushes the bottom row down

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.padding(end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(3) { index ->
                        val isActiveStep = index == 0
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(5.dp)
                                .background(
                                    color = if (isActiveStep) ColorsDefaultTheme.color_Primary_green else Color.White,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        onNavigateToSignIn(backgroundImages[pagerState.currentPage % backgroundImages.size])
                    },
                    modifier = Modifier
                        .width(180.dp)
                        .height(60.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorsDefaultTheme.color_Primary_green
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.get_started_btn_text),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GetStartedScreenPreview() {
    ParentAppTheme {
        GetStartedScreen(onNavigateToSignIn = {})
    }
}