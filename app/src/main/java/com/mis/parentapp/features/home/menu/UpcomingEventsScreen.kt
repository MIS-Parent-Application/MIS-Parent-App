package com.mis.parentapp.features.home.menu

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mis.parentapp.R
import com.mis.parentapp.data.AppDatabase
import com.mis.parentapp.data.EventItem
import com.mis.parentapp.data.EventRepository
import com.mis.parentapp.features.home.EventsViewModel
import com.mis.parentapp.ui.theme.AppTypes

@Composable
fun rememberDrawableIdFromName(imageName: String?): Int {
    val context = LocalContext.current
    return remember(imageName) {
        if (imageName.isNullOrBlank()) {
            R.drawable.event1 // Default fallback asset if null
        } else {
            val cleanName = imageName.substringBefore(".")
            val resId = context.resources.getIdentifier(cleanName, "drawable", context.packageName)
            if (resId != 0) resId else R.drawable.event1
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingEventsScreen(
    autoSelectEventId: Int? = null,
    onBackClick: () -> Unit,
    onDetailTopBarChange: (Boolean, (() -> Unit)?, (() -> Unit)?) -> Unit
) {
    val context = LocalContext.current
    val eventRepo = remember {
        EventRepository(AppDatabase.getDatabase(context).eventDao())
    }
    val viewModel: EventsViewModel = viewModel(
        factory = EventsViewModel.provideFactory(eventRepo)
    )
    val allUpcomingEvents by viewModel.upcomingEvents.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedEvent by remember { mutableStateOf<EventItem?>(null) }

    LaunchedEffect(allUpcomingEvents, autoSelectEventId) {
        if (autoSelectEventId != null && selectedEvent == null && allUpcomingEvents.isNotEmpty()) {
            val matchingEvent = allUpcomingEvents.find { it.id == autoSelectEventId }
            if (matchingEvent != null) {
                selectedEvent = matchingEvent
            }
        }
    }

    BackHandler(enabled = selectedEvent != null) {
        selectedEvent = null
        onDetailTopBarChange(false, null, null)
    }

    val filteredEvents = remember(allUpcomingEvents, selectedFilter) {
        if (selectedFilter == "All") allUpcomingEvents
        else allUpcomingEvents.filter { it.status == selectedFilter }
    }

    val groupedEvents = filteredEvents.groupBy { it.category }

    DisposableEffect(selectedEvent) {
        if (selectedEvent != null) {
            onDetailTopBarChange(
                true,
                {
                    selectedEvent = null
                },
                { shareEvent(context, selectedEvent!!) }
            )
        } else {
            onDetailTopBarChange(false, null, null)
        }
        onDispose {
            onDetailTopBarChange(false, null, null)
        }
    }

    if (selectedEvent != null) {
        EventDetailScreen(event = selectedEvent!!)
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            EventFilterRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                groupedEvents.forEach { (category, events) ->
                    item {
                        EventSection(
                            title = category,
                            events = events,
                            onEventClick = { selectedEvent = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventFilterRow(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf("All", "Postponed", "Cancelled")
        filters.forEach { filter ->
            val isSelected = filter == selectedFilter
            Surface(
                modifier = Modifier.clickable { onFilterSelected(filter) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = filter,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                    style = AppTypes.type_M3_label_small
                )
            }
        }
    }
}

@Composable
fun EventSection(title: String, events: List<EventItem>, onEventClick: (EventItem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(events) { event ->
                EventCard(event = event, onClick = { onEventClick(event) })
            }
        }
    }
}

@Composable
fun EventCard(event: EventItem, onClick: () -> Unit) {
    val context = LocalContext.current

    val imageResource = remember(event.imageUrl) {
        if (!event.imageUrl.isNullOrBlank()) {
            val cleanName = event.imageUrl.substringBefore(".")
            val resId = context.resources.getIdentifier(cleanName, "drawable", context.packageName)
            if (resId != 0) resId else R.drawable.event1
        } else {
            R.drawable.event1
        }
    }

    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column {
            Image(
                painter = painterResource(id = imageResource),
                contentDescription = "Event banner for ${event.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = event.title, style = AppTypes.type_Body_Small, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = event.date, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("View", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(event: EventItem) {
    val context = LocalContext.current

    val imageResource = remember(event.imageUrl) {
        if (!event.imageUrl.isNullOrBlank()) {
            val cleanName = event.imageUrl.substringBefore(".")
            val resId = context.resources.getIdentifier(cleanName, "drawable", context.packageName)
            if (resId != 0) resId else R.drawable.event1
        } else {
            R.drawable.event1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = imageResource),
            contentDescription = "Event banner",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = event.title,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = event.description,
            style = AppTypes.type_Body_Small,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
    }
}

private fun shareEvent(context: Context, event: EventItem) {
    val shareText = buildString {
        appendLine(event.title)
        appendLine("Date: ${event.date}")
        appendLine()
        appendLine(event.description)
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, event.title)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share Event"))
}
