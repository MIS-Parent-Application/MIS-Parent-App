package com.mis.parentapp.features.auth

import android.widget.Toast
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import kotlinx.coroutines.delay

@Composable
fun OtpSignInScreen(
    username: String,
    password: String,
    otpToken: String,
    email: String,
    backgroundResId: Int,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var code by remember { mutableStateOf("") }
    var currentOtpToken by remember(otpToken) { mutableStateOf(otpToken) }
    var resendCooldown by remember { mutableStateOf(60) }
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            delay(1000)
            resendCooldown -= 1
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = if (backgroundResId != 0) backgroundResId else R.drawable.bg_one_sign_screen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
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

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Verify your email",
                    color = ColorsDefaultTheme.text_color,
                    fontSize = 34.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Enter the 6-digit code sent to $email",
                    color = ColorsDefaultTheme.text_color.copy(alpha = 0.8f),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Light
                )

                Text(
                    text = "The code expires in 10 minutes. You have up to 5 attempts.",
                    color = ColorsDefaultTheme.text_color.copy(alpha = 0.72f),
                    fontSize = 14.sp,
                    lineHeight = 19.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = code,
                    onValueChange = { value ->
                        code = value.filter { it.isDigit() }.take(6)
                    },
                    placeholder = { Text("Verification code", color = Color.Gray) },
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
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        enabled = !isLoading && resendCooldown == 0,
                        onClick = {
                            viewModel.resendOtp(
                                otpToken = currentOtpToken,
                                onSuccess = { newOtpToken, retryAfterSeconds ->
                                    currentOtpToken = newOtpToken
                                    code = ""
                                    resendCooldown = retryAfterSeconds
                                    Toast.makeText(context, "A new code was sent", Toast.LENGTH_SHORT).show()
                                },
                                onError = { message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    ) {
                        Text(
                            text = if (resendCooldown > 0) {
                                "Resend code in ${resendCooldown}s"
                            } else {
                                "Resend code"
                            },
                            color = if (resendCooldown > 0) {
                                ColorsDefaultTheme.text_color.copy(alpha = 0.62f)
                            } else {
                                ColorsDefaultTheme.color_Primary_green
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.padding(end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(5.dp)
                                    .background(
                                        color = ColorsDefaultTheme.color_Primary_green,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (code.length == 6) {
                                viewModel.verifyOtp(
                                    username = username,
                                    pass = password,
                                    otpToken = currentOtpToken,
                                    code = code,
                                    onSuccess = onSignInSuccess,
                                    onError = { message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                Toast.makeText(context, "Enter the 6-digit code", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .widthIn(min = 132.dp, max = 180.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorsDefaultTheme.color_Primary_green
                        )
                    ) {
                        Text(
                            text = "Verify",
                            color = ColorsDefaultTheme.color_Surface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ColorsDefaultTheme.color_Primary_green)
            }
        }
    }
}
