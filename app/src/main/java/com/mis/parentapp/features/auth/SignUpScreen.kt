package com.mis.parentapp.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.ui.theme.AppTypes

@Composable
fun SignUpScreen(
    backgroundResId: Int,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 1. Static Background from Onboarding
        Image(
            painter = painterResource(id = backgroundResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Overlay
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
                .padding(horizontal = 24.dp, vertical = 50.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.coldea_logo_jk1jkwfg_1),
                    contentDescription = "Logo",
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Create an account",
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Let’s get connected. Set up your parent portal in just a few easy steps.",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Social Sign-in options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SignUpOptionRow(icon = Icons.Filled.Key, text = "Other sign-in options")
                SignUpOptionRow(painterRes = R.drawable.coldea_logo_jk1jkwfg_1, text = "Sign in with Google")
                SignUpOptionRow(icon = Icons.Filled.Facebook, text = "Sign in with Facebook")
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color(0xfff6fde7))) {
                            append("Already have an account?")
                        }
                        withStyle(style = SpanStyle(color = Color(0xffe6ea85), fontWeight = FontWeight.Bold)) {
                            append(" Sign-in")
                        }
                    },
                    modifier = Modifier.clickable { onNavigateToSignIn() }
                )
            }
        }
    }
}

@Composable
private fun SignUpOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    painterRes: Int? = null,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(0.7f)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
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
private fun SignUpScreenPreview() {
    com.mis.parentapp.ui.theme.ParentAppTheme {
//        SignUpScreen(
//            backgroundResId = R.drawable.bg_one_sign_screen,
//            onBack = {},
//            onNavigateToSignIn = {} // Add this empty lambda
//        )
    }
}
