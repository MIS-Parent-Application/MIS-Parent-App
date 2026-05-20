package com.mis.parentapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE eventType = :type ORDER BY date ASC, time ASC")
    fun getEventsByType(type: String): Flow<List<EventItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventItem>)

    @Query("DELETE FROM events")
    suspend fun clearEvents()

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Int): EventItem?
}
