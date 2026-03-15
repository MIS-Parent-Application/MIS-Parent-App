package com.mis.parentapp

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import com.mis.parentapp.ui.theme.ParentAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class RecentAttendancePage : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParentAppTheme {
                RecentAttendance()
            }
        }
    }
}


enum class AttendanceStatus {
    PRESENT,
    LATE,
    HOLIDAY,
    ABSENT
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
            Column {
                Text(
                    text = "Recent Attendance",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
                Attendance(AttendanceStatus.PRESENT, LocalDate.of(2025, 2, 13))
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
                Attendance(AttendanceStatus.ABSENT, LocalDate.of(2025, 2, 14))
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
                Attendance(AttendanceStatus.HOLIDAY, LocalDate.of(2025, 2, 15))
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
                Attendance(AttendanceStatus.LATE, LocalDate.of(2025, 2, 16))
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
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row {
                Column(
                    modifier = Modifier.weight(.5f)
                ) {
                    AttendanceVal(
                        "Present",
                        156,
                        Color(0xFF2E7D32),
                        modifier = Modifier
                    )
                    Spacer(
                        modifier = Modifier.height(15.dp)
                    )
                    AttendanceVal(
                        "Absent",
                        8,
                        Color(0xFFC62828),
                        modifier = Modifier
                    )
                }
                Column(
                    modifier = Modifier.weight(.5f)
                ) {
                    AttendanceVal(
                        "Late",
                        4,
                        Color(0xFFEF6C00),
                        modifier = Modifier
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
    Column(modifier = modifier) {
        Text(
            text = name,
            fontSize = 14.sp,
            color = Color.Gray
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
    var icon: Int
    var statusStr: String
    var statusColor: Color
    when (status) {
        AttendanceStatus.PRESENT -> {
            icon = R.drawable.baseline_check_circle_24
            statusStr = "Present"
            statusColor = Color(0xFF2E7D32)
        }
        AttendanceStatus.LATE -> {
            icon = R.drawable.baseline_access_time_24
            statusStr = "Late"
            statusColor = Color(0xFFEF6C00)
        }
        AttendanceStatus.ABSENT -> {
            icon = R.drawable.baseline_close_24
            statusStr = "Absent"
            statusColor = Color(0xFFC62828)
        }
        else -> {
            icon = R.drawable.baseline_calendar_month_24
            statusStr = "Holiday"
            statusColor = Color(0xFF1565C0)
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
                        Column {
                            Text(
                                text = formatDateAttendance(date, "MMM d"),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = date.dayOfWeek.getDisplayName(
                                    TextStyle.FULL,
                                    Locale.ENGLISH
                                ),
                                fontSize = 12.sp,
                                color = Color.Gray
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
fun formatDateAttendance(date: LocalDate, pattern: String): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return date.format(formatter)
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun AttendancePreview() {
    ParentAppTheme {
        RecentAttendance()
    }
}
