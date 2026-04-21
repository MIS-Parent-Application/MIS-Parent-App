package com.mis.parentapp.data

class EventRepository(private val eventDao: EventDao) {

    // These look at the DB (which was filled by your SQL seed)
    fun getRecentEvents() = eventDao.getEventsByType("RECENT")
    fun getUpcomingEvents() = eventDao.getEventsByType("UPCOMING")

    suspend fun refreshEvents() {
        val mockEvents = listOf(
            EventItem(1, "Science Fair", "Academic", "2026-05-10", "Description...", "UPCOMING", "Normal"),
            EventItem(2, "PTA Meeting", "Meeting", "2026-04-15", "Description...", "RECENT", "Normal"),
            EventItem(3, "Sports Day", "Sports", "2026-06-20", "Description...", "UPCOMING", "Postponed")
        )
        eventDao.insertEvents(mockEvents)
    }
}