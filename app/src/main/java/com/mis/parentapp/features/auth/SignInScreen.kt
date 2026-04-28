package com.mis.parentapp.features.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun SignInScreen(
    backgroundResId: Int,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onSignInSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var currentStep by remember { mutableIntStateOf(0) } // 0: Username, 1: Password

    val context = androidx.compose.ui.platform.LocalContext.current

    // Handles the system back button
    BackHandler {
        if (currentStep == 1) {
            currentStep = 0
        } else {
            onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Let's start by\nsigning you in",
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 48.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (currentStep == 0) "Enter your username below" else "Enter your password below",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Input Field
                if (currentStep == 0) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = { Text("Username", color = Color.White.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                } else {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password", color = Color.White.copy(alpha = 0.5f)) },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Row: Progress and Button side-by-side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Minimized Progress Bars
                    Row(
                        modifier = Modifier.weight(0.4f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(3) { index ->
                            val isActive = index <= (currentStep + 1)
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

                    // Smaller Button
                    Button(
                        onClick = {
                            if (currentStep == 0) {
                                if (username.isNotBlank()) currentStep = 1
                            } else {
                                if (password.isNotBlank()) {
                                    viewModel.signIn(username, password, onSignInSuccess) {
                                        android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(0.6f)
                            .height(48.dp), // Slightly smaller height
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorsDefaultTheme.color_Primary_green_container
                        )
                    ) {
                        Text(
                            text = if (currentStep == 0) "Next" else "Sign In",
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