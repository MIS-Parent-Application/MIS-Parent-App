package com.mis.parentapp.data

import com.mis.parentapp.network.ApiService
import com.mis.parentapp.network.CalendarEventDto
import com.mis.parentapp.network.RetrofitInstance
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EventRepository(
    private val eventDao: EventDao,
    private val api: ApiService = RetrofitInstance.api
) {

    fun getRecentEvents() = eventDao.getEventsByType("RECENT")
    fun getUpcomingEvents() = eventDao.getEventsByType("UPCOMING")

    suspend fun refreshEvents(studentId: Int? = null) {
        val syncedEvents = api.getCalendarEvents(studentId).map { it.toEventItem() }
        eventDao.clearEvents()
        eventDao.insertEvents(syncedEvents)
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
