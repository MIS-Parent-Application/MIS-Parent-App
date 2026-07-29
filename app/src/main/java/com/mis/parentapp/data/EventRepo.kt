package com.mis.parentapp.data

import com.mis.parentapp.network.ApiService
import com.mis.parentapp.network.CalendarEventDto
import com.mis.parentapp.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
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
        withContext(Dispatchers.IO) {
            val orFilter = if (studentId != null) {
                "(student_id.is.null,student_id.eq.$studentId)"
            } else {
                "(student_id.is.null)"
            }
            
            android.util.Log.d("EventRepository", "Refreshing events with filter: $orFilter")

            // 1. Fetch Upcoming Events from calendar_events
            val upcomingFromApi = try {
                val response = api.getCalendarEvents(orFilter = orFilter)
                android.util.Log.d("EventRepository", "Fetched ${response.size} calendar events")
                response.map { it.toEventItem() }
            } catch (e: Exception) {
                android.util.Log.e("EventRepository", "Failed to fetch upcoming events: ${e.message}")
                emptyList()
            }

            // 2. Fetch Recent Activities from academic_performance
            val recentFromApi = try {
                val studentFilter = studentId?.let { "eq.$it" }
                val response = api.getAcademicPerformance(idFilter = studentFilter)
                android.util.Log.d("EventRepository", "Fetched ${response.size} academic performance items")
                response.map { it.toEventItem() }
            } catch (e: Exception) {
                android.util.Log.e("EventRepository", "Failed to fetch recent activities: ${e.message}")
                emptyList()
            }

            val syncedUpcoming = upcomingFromApi.filter { it.eventType == "UPCOMING" }
            val syncedRecent = (upcomingFromApi.filter { it.eventType == "RECENT" } + recentFromApi)
                .distinctBy { it.id.toString() + it.title }

            upcomingEvents.value = syncedUpcoming.sortedWith(compareBy<EventItem> { it.date }.thenBy { it.time })
            recentEvents.value = syncedRecent.sortedWith(compareByDescending<EventItem> { it.date }.thenBy { it.time })
            
            android.util.Log.d("EventRepository", "Final State - Upcoming: ${upcomingEvents.value.size}, Recent: ${recentEvents.value.size}")
        }
    }

    private fun com.mis.parentapp.network.AcademicPerformanceDto.toEventItem(): EventItem {
        return EventItem(
            id = id,
            title = title,
            category = type,
            date = assignedDate,
            time = timeAgo,
            description = summary,
            eventType = "RECENT",
            status = status,
            imageUrl = imageUrl ?: "event2.jpg"
        )
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
