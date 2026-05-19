package com.mis.parentapp.features.me.essentials

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.mis.parentapp.network.ChatMessageDto
import com.mis.parentapp.network.FacultyContactDto
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.network.SendChatMessageRequest
import com.mis.parentapp.utilities.cards.MessageCard
import com.mis.parentapp.utilities.cards.MessageData
import kotlinx.coroutines.launch

private const val ParentChatId = "parent_1"

@Composable
fun MessagesScreen() {
    var selectedContact by remember { mutableStateOf<FacultyContactDto?>(null) }
    var contacts by remember { mutableStateOf<List<FacultyContactDto>>(emptyList()) }
    var lastMessages by remember { mutableStateOf<Map<String, ChatMessageDto>>(emptyMap()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching {
            val faculty = RetrofitInstance.api.getFacultyContacts()
            val latest = faculty.associate { contact ->
                contact.facultyId to RetrofitInstance.api
                    .getChatHistory(contact.facultyId, ParentChatId)
                    .lastOrNull()
            }.filterValues { it != null }.mapValues { it.value!! }
            faculty to latest
        }.onSuccess { (faculty, latest) ->
            contacts = faculty
            lastMessages = latest
            errorMessage = null
        }.onFailure {
            errorMessage = "Unable to load messages from the server."
        }
    }

    if (selectedContact != null) {
        ChatView(
            contact = selectedContact!!,
            onBack = { selectedContact = null }
        )
    } else {
        MessagesList(
            contacts = contacts,
            lastMessages = lastMessages,
            errorMessage = errorMessage,
            onMessageClick = { selectedContact = it }
        )
    }
}

@Composable
private fun MessagesList(
    contacts: List<FacultyContactDto>,
    lastMessages: Map<String, ChatMessageDto>,
    errorMessage: String?,
    onMessageClick: (FacultyContactDto) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (contacts.isEmpty()) {
            Text(
                text = errorMessage ?: "No faculty conversations yet.",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(contacts) { contact ->
                val latest = lastMessages[contact.facultyId]
                MessageCard(
                    message = MessageData(
                        id = contact.facultyId,
                        senderName = contact.name,
                        lastMessage = latest?.message ?: "${contact.subject} - ${contact.department}",
                        timestamp = latest?.created_at?.take(10) ?: "",
                        imageRes = R.drawable.student_image
                    ),
                    onClick = { onMessageClick(contact) }
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
private fun ChatView(contact: FacultyContactDto, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var textState by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<ChatMessageDto>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(contact.facultyId) {
        runCatching {
            RetrofitInstance.api.getChatHistory(contact.facultyId, ParentChatId)
        }.onSuccess {
            messages = it
            errorMessage = null
        }.onFailure {
            errorMessage = "Unable to load chat history."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.student_image),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = contact.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(text = contact.subject, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Call, contentDescription = "Call")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                text = textState,
                onTextChange = { textState = it },
                onSend = {
                    val outgoing = textState.trim()
                    if (outgoing.isEmpty()) return@ChatInputBar
                    scope.launch {
                        runCatching {
                            RetrofitInstance.api.sendChatMessage(
                                SendChatMessageRequest(
                                    sender_id = ParentChatId,
                                    receiver_id = contact.facultyId,
                                    message = outgoing
                                )
                            )
                        }.onSuccess {
                            messages = messages + it
                            textState = ""
                            errorMessage = null
                        }.onFailure {
                            errorMessage = "Message was not sent. Please check the server."
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
            LazyColumn(
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
                        isOutgoing = item.sender_id == ParentChatId
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
        Text(text = time, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ChatInputBar(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
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
                )
            )
            IconButton(onClick = onSend) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}
