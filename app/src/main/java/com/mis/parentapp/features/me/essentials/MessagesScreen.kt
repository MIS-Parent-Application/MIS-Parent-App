package com.mis.parentapp.features.me.essentials

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.utilities.cards.MessageCard
import com.mis.parentapp.utilities.cards.MessageData

@Composable
fun MessagesScreen() {
    var selectedMessage by remember { mutableStateOf<MessageData?>(null) }

    if (selectedMessage != null) {
        ChatView(
            message = selectedMessage!!,
            onBack = { selectedMessage = null }
        )
    } else {
        MessagesList(
            onMessageClick = { selectedMessage = it }
        )
    }
}

@Composable
fun MessagesList(onMessageClick: (MessageData) -> Unit) {
    val messages = remember { getDummyMessages() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(messages) { message ->
                MessageCard(
                    message = message,
                    onClick = { onMessageClick(message) }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatView(message: MessageData, onBack: () -> Unit) {
    var textState by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = message.imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = message.senderName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Call */ }) {
                        Icon(Icons.Default.Call, contentDescription = "Call")
                    }
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                text = textState,
                onTextChange = { textState = it },
                onSend = { textState = "" }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    ChatBubble(
                        content = "(P1LOANSURF10 + P1.00 service fee) from 04/30/2026.\n\nReload now to pay. If unpaid by 05/14/2026, we will collect 1100.00 MB from your next promo purchase. Conversion is P1 = 100.00 MB.",
                        time = "Friday • 9:23 PM",
                        isOutgoing = false
                    )
                }
                item {
                    ChatBubble(
                        content = "Hi! Your loan is now fully paid. We used 1100.00 MB from your data wallet to pay P11.00 for your P1LOANSURF10 loan from 04/30/2026.\n\nYou can borrow from Globe Loans again anytime! For the lowest service fee, use the GlobeOne app.",
                        time = "9:23 PM",
                        isOutgoing = false
                    )
                }
            }

            // Quick replies
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Okay 👍", "Yes", "Wow", "All right").forEach { reply ->
                    Surface(
                        modifier = Modifier.clickable { /* Send reply */ },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = reply,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(content: String, time: String, isOutgoing: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        if (!isOutgoing) {
            Text(text = time, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
        }
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
        if (isOutgoing) {
            Text(text = time, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun ChatInputBar(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Add */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Text message") },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                trailingIcon = {
                    Row {
                        IconButton(onClick = { /* Emoji */ }) { Icon(Icons.Default.EmojiEmotions, contentDescription = "Emoji") }
                        IconButton(onClick = { /* Image */ }) { Icon(Icons.Default.Image, contentDescription = "Image") }
                    }
                }
            )
            IconButton(onClick = onSend) {
                Icon(Icons.Default.Mic, contentDescription = "Voice")
            }
        }
    }
}

fun getDummyMessages(): List<MessageData> {
    return listOf(
        MessageData("1", "Nathaniel", "Lorem ipsum dolor sit amet consectetur. Auctor platea viverra ...", "4hrs ago", R.drawable.student_image),
        MessageData("2", "Math 101", "Lorem ipsum dolor sit amet consectetur. Auctor platea viverra ...", "4hrs ago", R.drawable.student_image)
    )
}
