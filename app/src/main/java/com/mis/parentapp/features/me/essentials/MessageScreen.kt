package com.mis.parentapp.features.me.essentials

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.network.ChatMessageDto
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@Composable
fun MessageScreen(
    contactId: String,
    senderName: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel(),
    onFeedbackSent: (() -> Unit)? = null
) {
    val listState = rememberLazyListState()
    val messages = viewModel.messages
    val errorMessage = viewModel.errorMessage
    val parentChatId = viewModel.parentChatId
    val isLoading = viewModel.isLoading

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Initialize Chat
    LaunchedEffect(contactId) {
        viewModel.initChat(contactId)
        val pending = SharedFeedback.message
        if (!pending.isNullOrBlank()) {
            // Small delay to ensure initialization/socket is ready
            kotlinx.coroutines.delay(500)
            viewModel.sendFeedbackMessage(contactId, pending)
            SharedFeedback.message = null
            // Navigate back immediately after sending
            onFeedbackSent?.invoke() ?: onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        errorMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
        
        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { item ->
                    ChatBubble(
                        content = item.message,
                        time = item.created_at?.replace("T", " ")?.take(16) ?: "",
                        isOutgoing = item.sender_id == parentChatId
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(content: String, time: String, isOutgoing: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isOutgoing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isOutgoing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Text(
                text = content,
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp
            )
        }
        Text(
            text = time,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

