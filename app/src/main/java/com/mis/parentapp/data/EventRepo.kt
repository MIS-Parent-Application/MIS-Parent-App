package com.mis.parentapp.data

import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {

    fun getRecentEvents() = eventDao.getEventsByType("RECENT")
    fun getUpcomingEvents() = eventDao.getEventsByType("UPCOMING")

    suspend fun refreshEvents() {
        // Mocking API Response
        val mockApiData = listOf(
            // Recent Events
            EventItem(101, "Recent School Fest", "School-wide", "20.4.2026", "Desc...", "RECENT"),
            // Upcoming Events
            EventItem(1, "Science Exhibition", "School-wide", "25.5.2026", "Desc...", "UPCOMING"),
            EventItem(2, "Graduation Gala", "College", "28.5.2026", "Desc...", "UPCOMING", "Postponed"),
            EventItem(3, "Sports Meet", "College", "01.6.2026", "Desc...", "UPCOMING")
        )
        eventDao.insertEvents(mockApiData)
    }
}