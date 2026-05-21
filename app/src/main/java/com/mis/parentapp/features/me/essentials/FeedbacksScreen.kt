package com.mis.parentapp.features.me.essentials

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FeedbacksScreen(
    onOpenTeacherMessages: (String) -> Unit
) {

    val context = LocalContext.current

    var feedbackText by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf("Teacher") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        Text(
            text = "Your Feedback",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Help us improve our school services.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Feedback Input
        OutlinedTextField(
            value = feedbackText,
            onValueChange = { feedbackText = it },
            label = { Text("Write your comments here...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Send To Options
        Text(
            text = "Send Feedback To",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                modifier = Modifier
                    .weight(1f)
                    .selectable(
                        selected = selectedOption == "Teacher",
                        onClick = { selectedOption = "Teacher" }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                RadioButton(
                    selected = selectedOption == "Teacher",
                    onClick = { selectedOption = "Teacher" }
                )

                Text(text = "Teacher")
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .selectable(
                        selected = selectedOption == "Gmail",
                        onClick = { selectedOption = "Gmail" }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                RadioButton(
                    selected = selectedOption == "Gmail",
                    onClick = { selectedOption = "Gmail" }
                )

                Text(text = "Gmail")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Rating Section
        Text(
            text = "Rate our service",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            for (i in 1..5) {

                IconButton(
                    onClick = {
                        rating = i
                    }
                ) {

                    Icon(
                        imageVector = if (i <= rating)
                            Icons.Filled.Star
                        else
                            Icons.Outlined.StarBorder,
                        contentDescription = "Star Rating",
                        tint = if (i <= rating)
                            Color(0xFFFFC107)
                        else
                            Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Submit Button
        Button(
            onClick = {

                if (feedbackText.isBlank()) {

                    Toast.makeText(
                        context,
                        "Please enter feedback first.",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@Button
                }

                // If Teacher is selected
                if (selectedOption == "Teacher") {

                    val feedbackMessage = """
                        Feedback:
                        
                        $feedbackText
                        
                        Rating: $rating/5
                    """.trimIndent()

                    // Save feedback temporarily
                    SharedFeedback.message = feedbackMessage

                    // Open teacher messages
                    onOpenTeacherMessages(feedbackMessage)

                } else {

                    // Gmail Send
                    val recipientEmail = "yourgmail@gmail.com"

                    val subject = "Parent App Feedback"

                    val message = """
                        Feedback:
                        
                        $feedbackText
                        
                        Rating: $rating/5
                    """.trimIndent()

                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$recipientEmail")
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                        putExtra(Intent.EXTRA_TEXT, message)
                    }

                    try {

                        context.startActivity(
                            Intent.createChooser(intent, "Send Feedback")
                        )

                        feedbackText = ""
                        rating = 0

                    } catch (e: Exception) {

                        Toast.makeText(
                            context,
                            "No email app found.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {

            Text(
                text = "Submit Feedback",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}