package com.mis.parentapp.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.ui.theme.ColorsDefaultTheme

@Composable
fun OnBoardingScreen(
    onNavigateToSignIn: (Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.student_image),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Surface(
            modifier = Modifier
                .fillMaxSize(),
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
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 48.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Connecting you with your child's education journey.",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontSize = 18.sp
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Button(
                    onClick = { onNavigateToSignIn(R.drawable.student_image) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorsDefaultTheme.color_Primary_green_container
                    )
                ) {
                    Text("Get Started", color = ColorsDefaultTheme.color_Primary_on_green)
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
