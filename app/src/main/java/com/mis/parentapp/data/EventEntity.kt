package com.mis.parentapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventItem(
    @PrimaryKey
    val id: Int,
    val title: String,
    val category: String,
    val date: String,
    val time: String = "",
    val description: String,
    val eventType: String,         // "RECENT" or "UPCOMING"
    val status: String = "Normal", // "Normal", "Postponed", "Cancelled"

    @ColumnInfo(name = "image_url")
    val imageUrl: String = "event1.jpg"
)