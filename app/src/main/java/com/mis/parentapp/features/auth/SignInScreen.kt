package com.mis.parentapp.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

@Composable
fun SignInScreen(
    backgroundResId: Int,
    onBack: () -> Unit,
    onSignInSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Static Background passed from Onboarding
        Image(
            painter = painterResource(id = backgroundResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Dark/Green Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color(0xFF0D230D).copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // 3. Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp)
        ) {
            // Header: Back Button and Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Back",
                    color = ColorsDefaultTheme.color_Primary_on_green,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clickable { onBack() },
                    style = AppTypes.type_Body_Small
                )
                Image(
                    painter = painterResource(id = R.drawable.coldea_logo_jk1jkwfg_1),
                    contentDescription = "Logo",
                    modifier = Modifier.size(90.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Sign In",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Empowering parents with real-time updates. Your dashboard is ready.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 28.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.height(56.dp))

            // Login Fields
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = ColorsDefaultTheme.color_Surface,
                        unfocusedContainerColor = ColorsDefaultTheme.color_Surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = true
                )

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = ColorsDefaultTheme.color_Surface,
                        unfocusedContainerColor = ColorsDefaultTheme.color_Surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onSignInSuccess() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorsDefaultTheme.color_Primary_green_container
                    )
                ) {
                    Text(
                        text = "Sign-in",
                        color = ColorsDefaultTheme.color_Primary_on_green,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }

                Text(
                    text = "Create an account",
                    color = ColorsDefaultTheme.color_On_yellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Social Sign-in options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SignInOptionRow(icon = Icons.Filled.Key, text = "Other sign-in options")
                SignInOptionRow(icon = Icons.Filled.AccountCircle, text = "Sign in with Google")
                SignInOptionRow(icon = Icons.Filled.Facebook, text = "Sign in with Facebook")
            }
        }
    }
}

@Composable
private fun SignInOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    painterRes: Int? = null,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(0.65f)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = Color.White
            )
        } else if (painterRes != null) {
            Image(
                painter = painterResource(id = painterRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = text,
            color = Color.White,
            style = AppTypes.type_Body_Small,
            fontSize = 15.sp
        )
    }
}

@Preview(widthDp = 412, heightDp = 917)
@Composable
private fun SignInScreenPreview() {
    com.mis.parentapp.ui.theme.ParentAppTheme {
        SignInScreen(
            backgroundResId = R.drawable.student_image,
            onBack = {},
            onSignInSuccess = {},
            onNavigateToSignUp = {}
        )
    }
}
