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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.mis.parentapp.network.FacultyChatRetrofit
import com.mis.parentapp.network.ParentChatLoginRequest
import com.mis.parentapp.utilities.cards.MessageCard
import com.mis.parentapp.utilities.cards.MessageData
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.launch
import org.json.JSONObject

private const val ParentChatName = "Mrs. Santerna"

private val FacultyContacts = listOf(
    FacultyContactDto(
        facultyId = "2023-00154",
        name = "Prof. Reyes",
        department = "Faculty",
        email = "faculty ID 2023-00154",
        subject = "Faculty chat"
    ),
    FacultyContactDto(
        facultyId = "2018-00088",
        name = "Dr. Maria Santos",
        department = "Faculty",
        email = "faculty ID 2018-00088",
        subject = "Faculty chat"
    )
)

@Composable
fun MessagesScreen() {
    var selectedContact by remember { mutableStateOf<FacultyContactDto?>(null) }
    var contacts by remember { mutableStateOf(FacultyContacts) }
    var lastMessages by remember { mutableStateOf<Map<String, ChatMessageDto>>(emptyMap()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var chatToken by remember { mutableStateOf<String?>(null) }
    var parentChatId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching {
            val login = FacultyChatRetrofit.api.parentLogin(ParentChatLoginRequest(ParentChatName))
            val token = login.token
            val parentId = login.parent_data.userId
            val latest = FacultyContacts.associate { contact ->
                contact.facultyId to FacultyChatRetrofit.api
                    .getChatHistory(contact.facultyId, "Bearer $token")
                    .data
                    .lastOrNull()
            }.filterValues { it != null }.mapValues { it.value!! }
            Triple(token, parentId, latest)
        }.onSuccess { (token, parentId, latest) ->
            contacts = FacultyContacts
            chatToken = token
            parentChatId = parentId
            lastMessages = latest
            errorMessage = null
        }.onFailure {
            contacts = FacultyContacts
            errorMessage = "Unable to connect to faculty chat server."
        }
    }

    if (selectedContact != null) {
        ChatView(
            contact = selectedContact!!,
            token = chatToken,
            parentChatId = parentChatId,
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
private fun ChatView(
    contact: FacultyContactDto,
    token: String?,
    parentChatId: String?,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var textState by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<ChatMessageDto>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var socket by remember { mutableStateOf<Socket?>(null) }

    LaunchedEffect(contact.facultyId, token) {
        val currentToken = token ?: return@LaunchedEffect
        runCatching {
            FacultyChatRetrofit.api.getChatHistory(contact.facultyId, "Bearer $currentToken")
                .data
        }.onSuccess {
            messages = it
            errorMessage = null
        }.onFailure {
            errorMessage = "Unable to load chat history."
        }
    }

    DisposableEffect(contact.facultyId, token, parentChatId) {
        val currentToken = token
        val currentParentId = parentChatId
        if (currentToken.isNullOrBlank() || currentParentId.isNullOrBlank()) {
            onDispose { }
        } else {
            val options = IO.Options().apply {
                auth = mapOf("token" to currentToken)
                reconnection = true
            }
            val liveSocket = IO.socket(FacultyChatRetrofit.BASE_URL, options)
            val receiveListener = { args: Array<Any> ->
                val payload = args.firstOrNull() as? JSONObject
                if (payload != null) {
                    val incoming = ChatMessageDto(
                        sender_id = payload.optString("sender_id"),
                        receiver_id = payload.optString("receiver_id"),
                        message = payload.optString("message"),
                        created_at = payload.optString("created_at")
                    )
                    val belongsToThread =
                        (incoming.sender_id == contact.facultyId && incoming.receiver_id == currentParentId) ||
                            (incoming.sender_id == currentParentId && incoming.receiver_id == contact.facultyId)
                    if (belongsToThread) {
                        scope.launch {
                            messages = messages + incoming
                        }
                    }
                }
            }
            liveSocket.on("receive_message", receiveListener)
            liveSocket.connect()
            socket = liveSocket
            onDispose {
                liveSocket.off("receive_message", receiveListener)
                liveSocket.disconnect()
                socket = null
            }
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
                    val currentParentId = parentChatId
                    if (currentParentId.isNullOrBlank()) {
                        errorMessage = "Faculty chat login is not ready yet."
                        return@ChatInputBar
                    }
                    val payload = JSONObject().apply {
                        put("receiver_id", contact.facultyId)
                        put("message", outgoing)
                    }
                    socket?.emit("send_message", payload)
                    messages = messages + ChatMessageDto(
                        sender_id = currentParentId,
                        receiver_id = contact.facultyId,
                        message = outgoing,
                        created_at = "Sending..."
                    )
                    textState = ""
                    errorMessage = null
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
        Text(text = time, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ChatInputBar(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                    unfocusedIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}
