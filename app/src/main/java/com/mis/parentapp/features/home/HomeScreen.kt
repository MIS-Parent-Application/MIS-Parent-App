package com.mis.parentapp.features.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mis.parentapp.R
import com.mis.parentapp.navigation.*
import com.mis.parentapp.network.ParentDashboard
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {

    val homeNavController = rememberNavController()

    NavHost(
        navController = homeNavController,
        startDestination = Home,
        modifier = modifier.fillMaxSize()
    ) {

        composable<Home> {
            Body(
                onNotificationClick = {
                    homeNavController.navigate(Notification)
                },
                onFilterClick = { label ->
                    when (label) {
                        "Upcoming events" -> homeNavController.navigate(UpcomingEvents)
                        "Recent activities" -> homeNavController.navigate(RecentActivities)
                    }
                }
            )
        }

        composable<Notification> {
            NotificationScreen(onBackClick = { homeNavController.popBackStack() })
        }

        composable<UpcomingEvents> {
            UpcomingEventsScreen(onBackClick = { homeNavController.popBackStack() })
        }

        composable<RecentActivities> {
            RecentActivitiesScreen(onBackClick = { homeNavController.popBackStack() })
        }
    }
}

@Composable
fun Body(
    modifier: Modifier = Modifier,
    onNotificationClick: () -> Unit,
    onFilterClick: (String) -> Unit
) {

    var dashboard by remember { mutableStateOf<ParentDashboard?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // 🔥 API CALL
    LaunchedEffect(Unit) {
        try {
            val result = RetrofitInstance.api.getDashboard()
            Log.d("API_SUCCESS", result.toString())
            dashboard = result
        } catch (e: Exception) {
            Log.e("API_ERROR", e.message ?: "Unknown error")
            error = e.message
        }
    }

    if (error != null) {
        Text("Error: $error")
        return
    }

    // ⏳ LOADING STATE
    if (dashboard == null) {
        Text("Loading...")
        return
    }

    // ✅ DATA READY
    val parentName = dashboard!!.parent.name
    val firstChild = dashboard!!.children.firstOrNull()

    val attendance = firstChild?.attendance ?: "--"
    val gpa = firstChild?.gpa?.toString() ?: "--"
    val pending = firstChild?.pendingPayments?.toString() ?: "--"
    val notifications = dashboard!!.unreadAnnouncements.toString()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    painter = painterResource(id = R.drawable.school_logo),
                    contentDescription = null,
                    modifier = Modifier.requiredSize(56.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {

                    Image(
                        painter = painterResource(id = R.drawable.formkit_date),
                        contentDescription = null,
                        modifier = Modifier.requiredSize(32.dp)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.ph_bell),
                        contentDescription = null,
                        modifier = Modifier
                            .requiredSize(32.dp)
                            .clickable { onNotificationClick() }
                    )
                }
            }
        }

        // WELCOME TEXT
        item {
            Text(
                text = "Welcome, $parentName",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = AppTypes.type_H1,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // FILTER BUTTONS
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                listOf("All", "Analytics", "Upcoming events", "Recent activities")
                    .forEach { label ->

                        val isSelected = label == "All"

                        Button(
                            onClick = { onFilterClick(label) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected)
                                    ColorsDefaultTheme.color_Primary_green
                                else Color(0xFFF5F5F5),

                                contentColor = if (isSelected)
                                    Color.White
                                else ColorsDefaultTheme.color_Surface_on_surface
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(label, style = AppTypes.type_M3_label_small)
                        }
                    }
            }
        }

        // QUICK STATS
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "Quick Stats",
                    style = AppTypes.type_H1,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard("Attendance", attendance, R.drawable.boxicons_calendar_check_filled, Modifier.weight(1f))
                    StatCard("GPA", gpa, R.drawable.material_symbols_owl, Modifier.weight(1f))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard("Pending due", pending, R.drawable.boxicons_wallet_filled, Modifier.weight(1f))
                    StatCard("Notifications", notifications, R.drawable.fluent_color_megaphone_loud_32, Modifier.weight(1f))
                }
            }
        }

        item {
            SectionPlaceholder("Upcoming Events", "No events yet.")
        }

        item {
            SectionPlaceholder("Recent Activities", "No activities yet.")
        }
    }
}

@Composable
fun StatCard(label: String, value: String, iconRes: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ColorsDefaultTheme.color_Surface)
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.TopStart),
            colorFilter = ColorFilter.tint(ColorsDefaultTheme.color_Primary_on_green)
        )

        Text(
            text = label,
            modifier = Modifier.align(Alignment.TopEnd),
            style = AppTypes.type_Caption
        )

        Text(
            text = value,
            modifier = Modifier.align(Alignment.BottomStart),
            style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold),
            color = Color(0xFF1B4D13)
        )
    }
}

@Composable
fun SectionPlaceholder(title: String, emptyText: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = title,
            style = AppTypes.type_H1,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.video_conference_streamline_bangalore),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )

            Text(
                text = emptyText,
                style = AppTypes.type_Body_Small,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}