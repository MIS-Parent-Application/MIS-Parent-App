package com.mis.parentapp.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mis.parentapp.data.EventItem
import com.mis.parentapp.data.EventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

//api ready
class EventsViewModel(private val repository: EventRepository) : ViewModel() {
    val upcomingEvents = repository.getUpcomingEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentEvents = repository.getRecentEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            try {
                repository.refreshEvents()
            } catch (e: Exception) {
                // Handle potential errors (e.g., no internet if using an API)
            }
        }
    }

    companion object {
        fun provideFactory(repository: EventRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EventsViewModel(repository) as T
            }
        }
    }
}
