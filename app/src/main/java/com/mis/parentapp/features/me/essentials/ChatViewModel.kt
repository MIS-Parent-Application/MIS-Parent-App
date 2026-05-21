package com.mis.parentapp.features.me.essentials

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mis.parentapp.network.ChatMessageDto
import com.mis.parentapp.network.FacultyChatRetrofit
import com.mis.parentapp.network.ParentChatLoginRequest
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ParentChatName = "Mrs. Santerna"

class ChatViewModel : ViewModel() {
    var messages by mutableStateOf<List<ChatMessageDto>>(emptyList())
        private set

    var chatTextState by mutableStateOf("")

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var chatToken by mutableStateOf<String?>(null)
        private set

    var parentChatId by mutableStateOf("parent_1")
        private set

    private var currentContactId: String? = null
    private var socket: Socket? = null
    private var pollingJob: Job? = null

    fun initChat(contactId: String) {
        if (currentContactId == contactId && (socket?.connected() == true || pollingJob?.isActive == true)) return
        currentContactId = contactId
        messages = emptyList()
        errorMessage = null
        pollingJob?.cancel()
        socket?.disconnect()

        viewModelScope.launch {
            runCatching {
                val login = FacultyChatRetrofit.api.parentLogin(ParentChatLoginRequest(ParentChatName))
                chatToken = login.token
                parentChatId = login.parent_data.userId
                FacultyChatRetrofit.api.getChatHistory(contactId, "Bearer ${login.token}").data
            }.onSuccess {
                messages = it
                errorMessage = null
                connectSocket(contactId)
                startPollingFallback(contactId)
            }.onFailure {
                errorMessage = "Unable to connect to the faculty chat backend."
            }
        }
    }

    private fun connectSocket(contactId: String) {
        val token = chatToken ?: return
        val pId = parentChatId
        val options = IO.Options().apply {
            auth = mapOf("token" to token)
            reconnection = true
        }

        val liveSocket = IO.socket(FacultyChatRetrofit.BASE_URL, options)
        liveSocket.on("receive_message") { args ->
            val payload = args.firstOrNull() as? JSONObject ?: return@on
            val incoming = ChatMessageDto(
                sender_id = payload.optString("sender_id"),
                receiver_id = payload.optString("receiver_id"),
                message = payload.optString("message"),
                created_at = payload.optString("created_at")
            )
            val belongsToThread =
                (incoming.sender_id == contactId && incoming.receiver_id == pId) ||
                    (incoming.sender_id == pId && incoming.receiver_id == contactId)
            if (belongsToThread) {
                viewModelScope.launch {
                    messages = (messages + incoming).distinctBy { it.created_at.orEmpty() + it.sender_id + it.message }
                }
            }
        }
        liveSocket.connect()
        socket = liveSocket
    }

    private fun startPollingFallback(contactId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive && currentContactId == contactId) {
                delay(5000)
                refreshHistory(contactId)
            }
        }
    }

    private suspend fun refreshHistory(contactId: String) {
        val token = chatToken ?: return
        runCatching {
            FacultyChatRetrofit.api.getChatHistory(contactId, "Bearer $token").data
        }.onSuccess {
            messages = it
            errorMessage = null
        }.onFailure {
            errorMessage = "Unable to refresh faculty chat history."
        }
    }

    fun sendMessage(contactId: String) {
        val outgoing = chatTextState.trim()
        if (outgoing.isEmpty()) return
        val pId = parentChatId
        val payload = JSONObject().apply {
            put("receiver_id", contactId)
            put("message", outgoing)
        }

        socket?.emit("send_message", payload)

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        messages = messages + ChatMessageDto(
            sender_id = pId,
            receiver_id = contactId,
            message = outgoing,
            created_at = timestamp
        )
        chatTextState = ""
        errorMessage = null
    }

    fun sendFeedbackMessage(contactId: String, message: String) {
        if (message.isBlank()) return
        val pId = parentChatId
        val payload = JSONObject().apply {
            put("receiver_id", contactId)
            put("message", message)
        }
        socket?.emit("send_message", payload)
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        messages = messages + ChatMessageDto(
            sender_id = pId,
            receiver_id = contactId,
            message = message,
            created_at = timestamp
        )
        errorMessage = null
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        socket?.disconnect()
    }
}
