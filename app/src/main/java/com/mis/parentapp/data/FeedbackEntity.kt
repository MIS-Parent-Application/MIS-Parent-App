package com.mis.parentapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feedbacks")
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val feedbackType: String = "General Feedback", // 'Bug', 'Feature Request', 'General Feedback'
    val content: String,
    val rating: Int = 0, // Keep for local DB compatibility if needed, but not used in UI
    val appVersion: String = "1.0",
    val timestamp: Long = System.currentTimeMillis()
)
