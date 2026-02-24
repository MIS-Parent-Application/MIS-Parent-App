package com.example.parentapp

import android.graphics.DashPathEffect
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parentapp.ui.theme.ParentAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParentAppTheme {
                Dashboard()
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Dashboard(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(scrollState)
    ) {
        Header(
            "Dashboard",
            "Welcome back! Here's what's happening with your child's education."
        )
        Spacer(
            modifier = Modifier.height(20.dp)
        )
        StatBox(
            R.drawable.baseline_calendar_month_24,
            "Overall attendance",
            "92.5%",
            "+2.5%"
        )
        Spacer(
            modifier = Modifier.height(20.dp)
        )
        StatBox(
            R.drawable.baseline_trending_up_24,
            "Average Grade",
            "A-",
            "Improved"
        )
        Spacer(
            modifier = Modifier.height(20.dp)
        )
        StatBox(
            R.drawable.baseline_menu_book_24,
            "Pending Assignments",
            "3",
            ""
        )
        Spacer(
            modifier = Modifier.height(20.dp)
        )
        StatBox(
            R.drawable.baseline_attach_money_24,
            "Fee Status",
            "Paid",
            "Up to date"
        )
        Spacer(
            modifier = Modifier.height(20.dp)
        )
        RecentAttendance()
        Spacer(
            modifier = Modifier.height(20.dp)
        )
        Announcements()
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecentAttendance() {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column(

            ) {
                Text(
                    text = "Recent Attendance"
                )
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
                Attendance(AttendanceStatus.PRESENT, LocalDate.of(2020, 2, 13))
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
                Attendance(AttendanceStatus.ABSENT, LocalDate.of(2020, 2, 14))
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
                Attendance(AttendanceStatus.HOLIDAY, LocalDate.of(2020, 2, 15))
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
                Attendance(AttendanceStatus.LATE, LocalDate.of(2020, 2, 16))
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
                AttendanceStats()
            }
        }
    }
}


@Composable
fun AttendanceStats() {
    Surface(
        color = Color.LightGray,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(

            ) {
                Column(
                    modifier = Modifier.weight(.5f)
                ) {
                    AttendanceVal(
                        "Present",
                        156,
                        Color.Green,
                        modifier = Modifier.weight(.5f)
                    )
                    Spacer(
                        modifier = Modifier.height(15.dp)
                    )
                    AttendanceVal(
                        "Absent",
                        8,
                        Color.Red,
                        modifier = Modifier
                    )
                }
                Column(
                    modifier = Modifier.weight(.5f)
                ) {
                    AttendanceVal(
                        "Late",
                        4,
                        Color.Red,
                        modifier = Modifier.weight(.5f)
                    )
                    Spacer(
                        modifier = Modifier.height(15.dp)
                    )
                    AttendanceVal(
                        "Total Days",
                        168,
                        Color.Black,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}


@Composable
fun AttendanceVal(name: String, statVal: Int, statColor: Color, modifier: Modifier) {
    Column(

    ) {
        Text(
            text = name
        )
        Text(
            text = statVal.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = statColor
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Attendance(status: AttendanceStatus, date: LocalDate) {
    var icon: Int = 0
    var statusStr = ""
    var statusColor: Color? = null
    when (status) {
        AttendanceStatus.PRESENT -> {
            icon = R.drawable.baseline_check_circle_24
            statusStr = "Present"
            statusColor = Color.Black
        }
        AttendanceStatus.LATE -> {
            icon = R.drawable.baseline_access_time_24
            statusStr = "Late"
            statusColor = Color.Gray
        }
        AttendanceStatus.ABSENT -> {
            icon = R.drawable.baseline_close_24
            statusStr = "Absent"
            statusColor = Color.Red
        }
        else -> {
            icon = R.drawable.baseline_calendar_month_24
            statusStr = "Holiday"
            statusColor = Color.Magenta
        }
    }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(icon),
                            contentDescription = "$statusStr icon"
                        )
                        Spacer(
                            modifier = Modifier.width(20.dp)
                        )
                        Column(

                        ) {
                            Text(
                                text = FormatDate(date, "MMM d")
                            )
                            Text(
                                text = date.dayOfWeek.getDisplayName(
                                    TextStyle.SHORT,
                                    Locale.ENGLISH
                                )
                            )
                        }
                    }
                }
                TextWithBG(
                    statusStr,
                    statusColor
                )
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Announcements() {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column(

            ) {
                Row(

                ) {
                    Text(
                        text = "Announcements",
                        modifier = Modifier.weight(1f)
                    )
                    Image(
                        painter = painterResource(R.drawable.baseline_notifications_24),
                        contentDescription = "Notification icon"
                    )
                }
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
                Announcement(
                    AnnouncementType.EVENT,
                    "Parent-Teacher Meeting",
                    "Scheduled for March 5th, 2026 at 10:00 AM. Please confirm your attendance.",
                    "School Event",
                    LocalDate.of(2026,2,18)
                )
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
                Announcement(
                    AnnouncementType.URGENT,
                    "Exam Schedule Released",
                    "Mid-term examination schedule has been published. Check the academic calendar.",
                    "Academics",
                    LocalDate.of(2026,2,17)
                )
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
                Announcement(
                    AnnouncementType.INFO,
                    "Library Book Return Reminder",
                    "Please return all borrowed books by end of this month.",
                    "Library",
                    LocalDate.of(2026,2,12)
                )
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Announcement(type: AnnouncementType, title: String, details: String, from: String, date: LocalDate) {
    var icon: Int
    var statusStr: String
    var statusColor: Color
    when (type) {
        AnnouncementType.EVENT -> {
            icon = R.drawable.baseline_calendar_month_24
            statusStr = "Event"
            statusColor = Color.Blue
        }
        AnnouncementType.URGENT -> {
            icon = R.drawable.baseline_error_24
            statusStr = "Urgent"
            statusColor = Color.Red
        }
        else -> {
            icon = R.drawable.baseline_info_24
            statusStr = "Info"
            statusColor = Color.Green
        }
    }
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column(

            ) {
                Row(

                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(icon),
                            contentDescription = "$type icon"
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    TextWithBG(statusStr, statusColor)
                }
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = details
                )
                Spacer(modifier = Modifier.height(15.dp))
                Row(

                ) {
                    Text(
                        text = from,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = FormatDate(date, "MMMM d, Y")
                    )
                }
            }

        }
    }
}


enum class AnnouncementType {
    EVENT,
    URGENT,
    INFO
}


enum class AttendanceStatus {
    PRESENT,
    LATE,
    HOLIDAY,
    ABSENT
}


@RequiresApi(Build.VERSION_CODES.O)
fun FormatDate(date: LocalDate, pattern: String): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return date.format(formatter)
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    ParentAppTheme {
        RecentAttendance()
    }
}