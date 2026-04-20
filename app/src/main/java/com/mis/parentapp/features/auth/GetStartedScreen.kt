package com.mis.parentapp.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.ui.theme.ColorsDefaultTheme

@Composable
fun GetStartedScreen(
    onNavigateToSignIn: (Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.student_image),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay matching the flow style
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Welcome to\nParent App",
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 48.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Connecting you with your child's education journey.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Action Row: Matches the SignInScreen layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Minimized Progress Bars (Phase 1 of 3)
                    Row(
                        modifier = Modifier.weight(0.4f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(3) { index ->
                            // Only the first bar is active (index 0)
                            val isActive = index == 0
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .background(
                                        color = if (isActive) Color.White else Color.White.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(3.dp)
                                    )
                            )
                        }
                    }

                    // Get Started Button
                    Button(
                        onClick = { onNavigateToSignIn(R.drawable.student_image) },
                        modifier = Modifier
                            .weight(0.6f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorsDefaultTheme.color_Primary_green_container
                        )
                    ) {
                        Text(
                            text = "Get Started",
                            color = ColorsDefaultTheme.color_Primary_on_green,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}