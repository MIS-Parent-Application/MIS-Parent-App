package com.mis.parentapp.features.me.essentials

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mis.parentapp.R
import com.mis.parentapp.utilities.cards.MessageCard
import com.mis.parentapp.utilities.cards.MessageData

@Composable
fun MessagesScreen(onMessageClick: (MessageData) -> Unit) {
    val messages = remember { getDummyMessages() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages) { message ->
                MessageCard(
                    message = message,
                    onClick = { onMessageClick(message) }
                )
            }
        }
    }
}

@Composable
fun ChatView() {
    Column(
        modifier = Modifier
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

        }
    }
}

fun getDummyMessages(): List<MessageData> {
    return listOf(
        MessageData("1", "Nathaniel", "Lorem ipsum dolor sit amet consectetur. Auctor platea viverra ...", "4hrs ago", R.drawable.student_image),
        MessageData("2", "Math 101", "Lorem ipsum dolor sit amet consectetur. Auctor platea viverra ...", "4hrs ago", null, gradientColors = listOf(Color(0xFFFF5252), Color(0xFFFF8A80)))
    )
}
