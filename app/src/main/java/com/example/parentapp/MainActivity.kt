package com.example.parentapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.parentapp.ui.theme.ParentAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
fun Dashboard() {
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
fun formatDate(date: LocalDate, pattern: String): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return date.format(formatter)
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    ParentAppTheme {
        Dashboard()
    }
}