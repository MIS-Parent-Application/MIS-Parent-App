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
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel : ViewModel() {
    var messages by mutableStateOf<List<ChatMessageDto>>(emptyList())
        private set

    var chatTextState by mutableStateOf("")

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var chatToken by mutableStateOf<String?>(null)
        private set

    var parentChatId by mutableStateOf<String?>(null)
        private set

    private var socket: Socket? = null
    private var currentContactId: String? = null

    private val ParentChatName = "Mrs. Santerna"

    fun initChat(contactId: String) {
        if (currentContactId == contactId) return
        currentContactId = contactId
        messages = emptyList()
        
        viewModelScope.launch {
            runCatching {
                val login = FacultyChatRetrofit.api.parentLogin(ParentChatLoginRequest(ParentChatName))
                chatToken = login.token
                parentChatId = login.parent_data.userId
                
                val history = FacultyChatRetrofit.api.getChatHistory(contactId, "Bearer ${login.token}").data
                history
            }.onSuccess {
                messages = it
                errorMessage = null
                connectSocket(contactId)
            }.onFailure {
                errorMessage = "Unable to connect to chat server."
            }
        }
    }

    private fun connectSocket(contactId: String) {
        val token = chatToken
        val pId = parentChatId
        if (token.isNullOrBlank() || pId.isNullOrBlank()) return

        socket?.disconnect()

        val options = IO.Options().apply {
            auth = mapOf("token" to token)
            reconnection = true
        }
        
        val liveSocket = IO.socket(FacultyChatRetrofit.BASE_URL, options)
        
        liveSocket.on("receive_message") { args ->
            val payload = args.firstOrNull() as? JSONObject
            if (payload != null) {
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
                        messages = messages + incoming
                    }
                }
            }
        }
        
        liveSocket.connect()
        socket = liveSocket
    }

    fun sendMessage(contactId: String) {
        val outgoing = chatTextState.trim()
        if (outgoing.isEmpty()) return
        
        val pId = parentChatId
        if (pId.isNullOrBlank()) {
            errorMessage = "Login not ready."
            return
        }

        val payload = JSONObject().apply {
            put("receiver_id", contactId)
            put("message", outgoing)
        }
        
        socket?.emit("send_message", payload)
        
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        
        messages = messages + ChatMessageDto(
            sender_id = pId,
            receiver_id = contactId,
            message = outgoing,
            created_at = currentTime
        )
        chatTextState = ""
    }

    override fun onCleared() {
        super.onCleared()
        socket?.disconnect()
    }
}
