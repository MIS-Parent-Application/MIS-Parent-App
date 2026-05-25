package com.mis.parentapp.utils.cards

import androidx.compose.ui.graphics.Color

data class AnnouncementData(
    val id: String,
    val title: String,
    val content: String,
    val isNew: Boolean,
    val category: String, // School-wide or College
    val imageUrl: String? = null,
    val colors: List<Color> = listOf(Color(0xFFFFA726), Color(0xFFFF7043)),
)
