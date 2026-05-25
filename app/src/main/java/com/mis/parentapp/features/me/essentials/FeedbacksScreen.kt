package com.mis.parentapp.features.me.essentials

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.BuildConfig
import com.mis.parentapp.data.AppDatabase
import com.mis.parentapp.data.FeedbackEntity
import com.mis.parentapp.network.FeedbackRequest
import com.mis.parentapp.network.RetrofitInstance
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbacksScreen(
    onOpenTeacherMessages: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val feedbackDao = remember { database.feedbackDao() }

    var subjectText by remember { mutableStateOf("") }
    var feedbackText by remember { mutableStateOf("") }
    
    val feedbackTypes = listOf("Bug", "Feature Request", "General Feedback")
    var selectedType by remember { mutableStateOf(feedbackTypes[2]) }
    var expanded by remember { mutableStateOf(false) }
    
    val savedFeedbacks by feedbackDao.getAllFeedbacks().collectAsState(initial = emptyList())

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
            text = "Help us improve our app for better school services.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Feedback Type Dropdown
        Box {
            OutlinedTextField(
                value = selectedType,
                onValueChange = { },
                label = { Text("Feedback Type") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                readOnly = true,
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                feedbackTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            selectedType = type
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = subjectText,
            onValueChange = { subjectText = it },
            label = { Text("Subject") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Text(
            text = "Send Feedback To: App Developers",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (subjectText.isBlank()) {
                    Toast.makeText(context, "Please enter a subject.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (feedbackText.isBlank()) {
                    Toast.makeText(context, "Please enter feedback first.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                scope.launch {
                    try {
                        // 1. Save to Local DB
                        val feedback = FeedbackEntity(
                            subject = subjectText,
                            feedbackType = selectedType,
                            content = feedbackText,
                            appVersion = BuildConfig.VERSION_NAME
                        )
                        feedbackDao.insertFeedback(feedback)
                        
                        // 2. Send to Postgres via Backend
                        runCatching {
                            RetrofitInstance.api.submitFeedback(
                                FeedbackRequest(
                                    userEmail = "parent@example.com", // In a real app, use the logged in user's email
                                    feedbackType = selectedType,
                                    message = "[$subjectText] $feedbackText",
                                    appVersion = BuildConfig.VERSION_NAME
                                )
                            )
                        }.onSuccess { response ->
                            if (response.success) {
                                println("Feedback successfully sent to server: ${response.message}")
                            }
                        }.onFailure {
                            // Even if network fails, it's saved locally
                            println("Network feedback submission failed: ${it.message}")
                        }
                        
                        Toast.makeText(context, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show()
                        
                        subjectText = ""
                        feedbackText = ""
                        selectedType = feedbackTypes[2]
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to submit feedback.", Toast.LENGTH_SHORT).show()
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

        if (savedFeedbacks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Previous Feedbacks",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            savedFeedbacks.forEach { feedback ->
                FeedbackItem(feedback)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FeedbackItem(feedback: FeedbackEntity) {
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val dateString = remember(feedback.timestamp) { sdf.format(Date(feedback.timestamp)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = feedback.subject,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = feedback.feedbackType,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Text(
                    text = dateString,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = feedback.content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
