package com.mis.parentapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugMenuScreen(
    onNavigateToSignIn: (Int) -> Unit,
    onNavigateToSignUp: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Developer Debug Menu", fontSize = 18.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Authentication Flows", fontWeight = FontWeight.Bold, color = Color.Gray)
            }

            item {
                Button(
                    onClick = { onNavigateToSignIn(R.drawable.student_image) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Launch Sign In (Student BG)")
                }
            }

            item {
                Button(
                    onClick = { onNavigateToSignUp(R.drawable.bg_one_sign_screen) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Launch Sign Up (Default BG)")
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("App State Shortcuts", fontWeight = FontWeight.Bold, color = Color.Gray)
            }

            item {
                OutlinedButton(
                    onClick = { /* Add logic for Home later */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Go to Dashboard (Skip Auth)")
                }
            }
        }
    }
}
