package com.mis.parentapp.data

class EventRepository(private val eventDao: EventDao) {

    // These look at the DB (which was filled by your SQL seed)
    fun getRecentEvents() = eventDao.getEventsByType("RECENT")
    fun getUpcomingEvents() = eventDao.getEventsByType("UPCOMING")

    suspend fun refreshEvents() {
        val mockEvents = listOf(
            EventItem(2, "Sports Day", "Sports", "2026-06-20", "", "Inter-school sports competition.", "UPCOMING", "Postponed", "event1.png"),
            EventItem(3, "Art Workshop", "Creative", "2026-05-15", "", "Hands-on painting and sculpting.", "UPCOMING", "Normal", "event2.png"),
            EventItem(4, "Music Gala", "Arts", "2026-07-05", "", "Evening of classical music.", "UPCOMING", "Normal", "event3.png"),
            EventItem(5, "Summer Camp", "General", "2026-08-01", "", "Week-long outdoor activities.", "UPCOMING", "Normal", "event1.png"),

            EventItem(6, "PTA Meeting", "Meeting", "2026-04-15", "", "Discussion on school curriculum.", "RECENT", "Normal", "event2.png"),
            EventItem(7, "Math Olympiad", "Academic", "2026-04-10", "", "Regional math competition winners announced.", "RECENT", "Normal", "event3.png"),
            EventItem(8, "Field Trip", "Excursion", "2026-04-05", "", "Visit to the National Museum.", "RECENT", "Normal", "event1.png"),
            EventItem(9, "Career Talk", "Education", "2026-03-28", "", "Industry experts sharing insights.", "RECENT", "Normal", "event2.png"),
            EventItem(10, "Spring Fest", "Social", "2026-03-20", "", "Celebrating the spring season.", "RECENT", "Cancelled", "event3.png")
        )
        eventDao.insertEvents(mockEvents)
    }
}