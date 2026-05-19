package com.mis.parentapp.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.network.CalendarEventDto
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import com.mis.parentapp.ui.theme.ParentAppTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    studentVM: StudentSharedViewModel? = null,
    onBackClick: () -> Unit
) {
    var events by remember { mutableStateOf<List<CalendarEventDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var visibleMonth by remember { mutableStateOf(Calendar.getInstance().startOfMonth()) }
    var selectedEvent by remember { mutableStateOf<CalendarEventDto?>(null) }
    val selectedStudent = studentVM?.selectedStudent
    val isDark = isSystemInDarkTheme()
    val colors = remember(isDark) { calendarColors(isDark) }

    LaunchedEffect(selectedStudent?.id) {
        isLoading = true
        errorMessage = null
        try {
            events = RetrofitInstance.api.getCalendarEvents(selectedStudent?.id)
        } catch (e: Exception) {
            errorMessage = "Unable to load calendar."
        } finally {
            isLoading = false
        }
    }

    selectedEvent?.let { event ->
        CalendarEventDetailDialog(
            event = event,
            colors = colors,
            onDismiss = { selectedEvent = null }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calendar", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.onBackground,
                    navigationIconContentColor = colors.onBackground
                )
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(colors.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalendarMonthHeader(
                month = visibleMonth,
                colors = colors,
                onPrevious = { visibleMonth = visibleMonth.addMonths(-1) },
                onNext = { visibleMonth = visibleMonth.addMonths(1) }
            )
            WeekdayHeader(colors)

            when {
                isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ColorsDefaultTheme.color_Primary_green)
                }
                errorMessage != null -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(errorMessage ?: "", color = Color.Red)
                }
                else -> MonthGrid(
                    visibleMonth = visibleMonth,
                    events = events,
                    colors = colors,
                    onEventClick = { selectedEvent = it }
                )
            }
        }
    }
}

@Composable
private fun CalendarMonthHeader(
    month: Calendar,
    colors: CalendarColors,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = SimpleDateFormat("MMMM yyyy", Locale.US).format(month.time),
                color = colors.onBackground,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "View only",
                color = colors.mutedText,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(colors.surface, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Row {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = colors.onBackground)
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = colors.onBackground)
            }
        }
    }
}

@Composable
private fun WeekdayHeader(colors: CalendarColors) {
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
            Text(
                text = day,
                color = if (day == "Tue") ColorsDefaultTheme.color_Primary_green else colors.mutedText,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun MonthGrid(
    visibleMonth: Calendar,
    events: List<CalendarEventDto>,
    colors: CalendarColors,
    onEventClick: (CalendarEventDto) -> Unit
) {
    val cells = remember(visibleMonth) { monthCells(visibleMonth) }
    val eventsByDate = remember(events) { events.groupBy { it.date } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, colors.gridLine, RoundedCornerShape(12.dp))
    ) {
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    DayCell(
                        day = day,
                        currentMonth = visibleMonth,
                        events = eventsByDate[day.dateKey].orEmpty(),
                        colors = colors,
                        onEventClick = onEventClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    currentMonth: Calendar,
    events: List<CalendarEventDto>,
    colors: CalendarColors,
    onEventClick: (CalendarEventDto) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentMonth = day.month == currentMonth.get(Calendar.MONTH)
    Box(
        modifier = modifier
            .height(112.dp)
            .background(colors.cell)
            .border(0.5.dp, colors.gridLine)
            .padding(5.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = if (day.isToday) Color(0xFFBFD2FF) else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.dayOfMonth.toString(),
                    color = when {
                        day.isToday -> Color(0xFF193A7A)
                        isCurrentMonth -> colors.onBackground
                        else -> colors.outsideMonth
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            events.take(2).forEach { event ->
                EventChip(
                    event = event,
                    colors = colors,
                    onClick = { onEventClick(event) }
                )
            }
            if (events.size > 2) {
                Text(
                    text = "+${events.size - 2} more",
                    color = colors.mutedText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EventChip(event: CalendarEventDto, colors: CalendarColors, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(colors.eventChip)
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(Color(0xFFF6D44B), CircleShape)
        )
        Text(
            text = event.title,
            color = Color.White,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CalendarEventDetailDialog(
    event: CalendarEventDto,
    colors: CalendarColors,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = ColorsDefaultTheme.color_Primary_green)
            }
        },
        icon = { Icon(Icons.Default.Event, contentDescription = null, tint = ColorsDefaultTheme.color_Primary_green) },
        title = { Text(event.title, color = colors.onBackground, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${event.date} | ${event.time}", color = ColorsDefaultTheme.color_Primary_green, fontWeight = FontWeight.Bold)
                Text(event.category, color = colors.mutedText, fontSize = 12.sp)
                Text(event.description, color = colors.onBackground, lineHeight = 20.sp)
            }
        },
        containerColor = colors.dialog
    )
}

private data class CalendarColors(
    val background: Color,
    val surface: Color,
    val cell: Color,
    val dialog: Color,
    val onBackground: Color,
    val mutedText: Color,
    val outsideMonth: Color,
    val gridLine: Color,
    val eventChip: Color
)

private fun calendarColors(isDark: Boolean): CalendarColors {
    return if (isDark) {
        CalendarColors(
            background = Color(0xFF0D1210),
            surface = Color(0xFF18211A),
            cell = Color(0xFF101614),
            dialog = Color(0xFF18211A),
            onBackground = Color(0xFFF4F7EF),
            mutedText = Color(0xFFA8B5A1),
            outsideMonth = Color(0xFF59635A),
            gridLine = Color(0xFF202A24),
            eventChip = Color(0xFF267D1E)
        )
    } else {
        CalendarColors(
            background = Color.White,
            surface = Color(0xFFF6FDE7),
            cell = Color(0xFFFCFEF8),
            dialog = Color.White,
            onBackground = Color(0xFF1C1B1F),
            mutedText = Color(0xFF79747E),
            outsideMonth = Color(0xFFB6BBAE),
            gridLine = Color(0xFFE1E8D5),
            eventChip = Color(0xFF267D1E)
        )
    }
}

private data class CalendarDay(
    val calendar: Calendar,
    val dayOfMonth: Int,
    val month: Int,
    val dateKey: String,
    val isToday: Boolean
)

private fun monthCells(month: Calendar): List<CalendarDay> {
    val first = month.startOfMonth()
    val start = first.cloneCalendar().apply {
        add(Calendar.DAY_OF_MONTH, -get(Calendar.DAY_OF_WEEK) + Calendar.SUNDAY)
    }
    return (0 until 42).map { offset ->
        start.cloneCalendar().apply { add(Calendar.DAY_OF_MONTH, offset) }.toCalendarDay()
    }
}

private fun Calendar.startOfMonth(): Calendar {
    return cloneCalendar().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}

private fun Calendar.addMonths(value: Int): Calendar {
    return cloneCalendar().apply {
        add(Calendar.MONTH, value)
        set(Calendar.DAY_OF_MONTH, 1)
    }
}

private fun Calendar.cloneCalendar(): Calendar = clone() as Calendar

private fun Calendar.toCalendarDay(): CalendarDay {
    val today = Calendar.getInstance()
    return CalendarDay(
        calendar = cloneCalendar(),
        dayOfMonth = get(Calendar.DAY_OF_MONTH),
        month = get(Calendar.MONTH),
        dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(time),
        isToday = get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    )
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    ParentAppTheme {
        CalendarScreen(onBackClick = {})
    }
}
