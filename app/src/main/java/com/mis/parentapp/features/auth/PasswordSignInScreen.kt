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
fun PasswordSignInScreen(
    email: String,
    backgroundResId: Int,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = backgroundResId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        // 2. BOTTOM GRADIENT
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
                        startY = 600f
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = R.drawable.coldea_logo_jk1jkwfg_1),
                    contentDescription = "Logo",
                    modifier = Modifier.size(85.dp)
                )
                Text(
                    text = "Back",
                    color = ColorsDefaultTheme.color_Surface,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clickable { onBack() },
                    style = AppTypes.type_Body_Small
                )
            }

            Spacer(modifier = Modifier.height(330.dp))

            Text(
                text = stringResource(id = R.string.auth_msg),
                color = ColorsDefaultTheme.text_color,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = R.string.password_sub_msg) + " " + "for $email",
                color = ColorsDefaultTheme.text_color.copy(alpha = 0.8f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.height(22.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text(stringResource(id = R.string.password_hint), color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = ColorsDefaultTheme.color_Surface,
                        unfocusedContainerColor = ColorsDefaultTheme.color_Surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = ColorsDefaultTheme.color_On_surface,
                        unfocusedTextColor = ColorsDefaultTheme.color_On_surface
                    ),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.padding(end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(3) { index ->
                            val isActiveStep = index <= 2
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
                            if (password.isNotEmpty()) {
                                viewModel.signIn(
                                    email = email,
                                    pass = password,
                                    onSuccess = { onSignInSuccess() },
                                    onError = { message ->
                                        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                android.widget.Toast.makeText(context, "Please enter your password", android.widget.Toast.LENGTH_SHORT).show()
                            }
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
                            text = stringResource(id = R.string.sign_in_btn_text),
                            color = ColorsDefaultTheme.color_Surface,
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
fun PasswordSignInScreenPreview() {
    ParentAppTheme {
        val dummyUserDao = object : UserDAO {
            override suspend fun registerUser(user: UserEntity) {}
            override suspend fun loginUser(email: String, password: String): UserEntity? = null
        }
        val viewModel = remember { AuthViewModel(dummyUserDao) }
        PasswordSignInScreen(
            email = "test@example.com",
            backgroundResId = R.drawable.bg_one_sign_screen,
            viewModel = viewModel,
            onBack = {},
            onSignInSuccess = {}
        )
    }
}
