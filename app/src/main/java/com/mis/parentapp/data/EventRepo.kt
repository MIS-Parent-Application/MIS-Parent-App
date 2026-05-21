package com.mis.parentapp.data

import com.mis.parentapp.network.ApiService
import com.mis.parentapp.network.CalendarEventDto
import com.mis.parentapp.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EventRepository(
    private val api: ApiService = RetrofitInstance.api
) {
    private val recentEvents = MutableStateFlow<List<EventItem>>(emptyList())
    private val upcomingEvents = MutableStateFlow<List<EventItem>>(emptyList())

    fun getRecentEvents() = recentEvents.asStateFlow()
    fun getUpcomingEvents() = upcomingEvents.asStateFlow()

    fun clearEvents() {
        recentEvents.value = emptyList()
        upcomingEvents.value = emptyList()
    }

    suspend fun refreshEvents(studentId: Int? = null) {
        val syncedEvents = api.getCalendarEvents(studentId).map { it.toEventItem() }
        recentEvents.value = syncedEvents
            .filter { it.eventType == "RECENT" }
            .sortedWith(compareByDescending<EventItem> { it.date }.thenBy { it.time })
        upcomingEvents.value = syncedEvents
            .filter { it.eventType == "UPCOMING" }
            .sortedWith(compareBy<EventItem> { it.date }.thenBy { it.time })
    }

    private fun CalendarEventDto.toEventItem(): EventItem {
        val type = if (isBeforeToday(date)) "RECENT" else "UPCOMING"
        return EventItem(
            id = id,
            title = title,
            category = category,
            date = date,
            time = time,
            description = description,
            eventType = type,
            status = status,
            imageUrl = imageUrl ?: "event1.jpg"
        )
    }

    private fun isBeforeToday(dateText: String): Boolean {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val eventDate = runCatching { formatter.parse(dateText) }.getOrNull() ?: return false
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return eventDate.before(today.time)
    }
}
