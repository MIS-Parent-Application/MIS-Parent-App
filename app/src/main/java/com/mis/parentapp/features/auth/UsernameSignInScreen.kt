package com.mis.parentapp.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import com.mis.parentapp.ui.theme.ParentAppTheme
import com.mis.parentapp.data.UserDAO
import com.mis.parentapp.data.UserEntity

@Composable
fun UsernameSignInScreen(
    backgroundResId: Int,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onSignInSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

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
                Image(
                    painter = painterResource(id = R.drawable.coldea_logo_jk1jkwfg_1),
                    contentDescription = "Logo",
                    modifier = Modifier.size(90.dp)
                )
                Text(
                    text = "Back",
                    color = ColorsDefaultTheme.color_Primary_on_green,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clickable { onBack() },
                    style = AppTypes.type_Body_Small
                )

            }

            Spacer(modifier = Modifier.height(366.dp))

            Text(
                text = stringResource(id = R.string.username_msg),
                color = ColorsDefaultTheme.text_color,
                fontSize = 36.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.username_sub_msg),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 24.sp,
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
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            viewModel.signIn(
                                email = email,
                                pass = password,
                                onSuccess = { onSignInSuccess() },
                                onError = { message ->
                                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            android.widget.Toast.makeText(context, "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
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
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun UsernameSignInScreenPreview() {
    ParentAppTheme {
        // Provide a dummy UserDAO for the AuthViewModel in the preview
        val dummyUserDao = object : UserDAO {
            override suspend fun registerUser(user: UserEntity) {}
            override suspend fun loginUser(email: String, password: String): UserEntity? = null
        }
        val viewModel = remember { AuthViewModel(dummyUserDao) }

        UsernameSignInScreen(
            backgroundResId = R.drawable.bg_one_sign_screen,
            viewModel = viewModel,
            onBack = {},
            onSignInSuccess = {},
            onNavigateToSignUp = {}
        )
    }
}
