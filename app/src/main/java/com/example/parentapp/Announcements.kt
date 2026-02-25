package com.example.parentapp

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.parentapp.ui.theme.ParentAppTheme
import java.time.LocalDate

class AnnouncementsPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParentAppTheme {

            }
        }
    }
}


enum class AnnouncementType {
    EVENT,
    URGENT,
    INFO
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
            Column {
                Row {
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
            Column {
                Row {
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
                Row {
                    Text(
                        text = from,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatDate(date, "MMMM d, Y")
                    )
                }
            }

        }
    }
}


@Preview(showBackground = true)
@Composable
fun AnnouncmentsPreview() {
    ParentAppTheme {
        Announcements()
    }
}