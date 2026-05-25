package com.mis.parentapp.features.me.essentials

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mis.parentapp.network.ChatMessageDto
import com.mis.parentapp.network.FacultyChatRetrofit
import com.mis.parentapp.network.FacultyContactDto
import com.mis.parentapp.network.ParentChatLoginRequest
import com.mis.parentapp.utils.cards.MessageCard
import com.mis.parentapp.utils.cards.MessageData

private val FacultyChatContacts = listOf(
    FacultyContactDto(
        facultyId = "2023-00154",
        name = "Prof. Reyes",
        department = "Faculty",
        email = "faculty ID 2023-00154",
        subject = "Mobile Development"
    ),
    FacultyContactDto(
        facultyId = "2018-00088",
        name = "Dr. Maria Santos",
        department = "Faculty",
        email = "faculty ID 2018-00088",
        subject = "Database Systems"
    )
)

@Composable
fun MessagesScreen(onMessageClick: (MessageData) -> Unit) {
    var contacts by remember { mutableStateOf(FacultyChatContacts) }
    var lastMessages by remember { mutableStateOf<Map<String, ChatMessageDto>>(emptyMap()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching {
            val login = FacultyChatRetrofit.api.parentLogin(ParentChatLoginRequest("Mrs. Santerna"))
            val latest = FacultyChatContacts.associate { contact ->
                val history = FacultyChatRetrofit.api.getChatHistory(
                    facultyId = contact.facultyId,
                    authorization = "Bearer ${login.token}"
                ).data
                contact.facultyId to history.lastOrNull()
            }.filterValues { it != null }.mapValues { it.value!! }
            latest
        }.onSuccess { latest ->
            contacts = FacultyChatContacts
            lastMessages = latest
            errorMessage = null
        }.onFailure {
            contacts = FacultyChatContacts
            errorMessage = "Unable to connect to the faculty chat backend."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (SharedFeedback.message != null) {
            Text(
                text = "Select a teacher to send your feedback.",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.error
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(contacts) { contact ->
                val latest = lastMessages[contact.facultyId]
                val message = MessageData(
                    id = contact.facultyId,
                    senderName = contact.name,
                    lastMessage = latest?.message ?: "${contact.subject} - ${contact.department}",
                    timestamp = latest?.created_at?.toMessageDate().orEmpty(),
                    imageRes = null,
                    gradientColors = listOf(Color(0xFF267D1E), Color(0xFFDEF731))
                )
                MessageCard(
                    message = message,
                    onClick = { onMessageClick(message) }
                )
            }
        }
    }
}

private fun String.toMessageDate(): String {
    return replace("T", " ").take(16)
}
