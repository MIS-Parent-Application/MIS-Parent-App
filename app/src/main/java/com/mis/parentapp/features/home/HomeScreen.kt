package com.mis.parentapp.features.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mis.parentapp.R
import com.mis.parentapp.navigation.Home
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import com.mis.parentapp.ui.theme.ParentAppTheme
import com.mis.parentapp.navigation.Notification
import com.mis.parentapp.navigation.RecentActivities
import com.mis.parentapp.navigation.UpcomingEvents



@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val homeNavController = rememberNavController()
    // State for the bottom drawer
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    // This handles the bottom drawer visibility and content
    if (showSheet) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            // Drawer Content
            HomeMenuDrawer(
                onItemClick = { route ->
                    showSheet = false // Close drawer
                    when (route) {
                        "Upcoming events" -> homeNavController.navigate(UpcomingEvents)
                        "Recent activities" -> homeNavController.navigate(RecentActivities)
                        // Add "Analytics" here if you create a route for it later
                    }
                }
            )
        }
    }

    NavHost(
        navController = homeNavController,
        startDestination = Home,
        modifier = modifier.fillMaxSize()
    ) {
        composable<Home> {
            Body(
                onNotificationClick = { homeNavController.navigate(Notification) },
                onMenuClick = { showSheet = true } // Trigger drawer
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
    onMenuClick: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.school_logo),
                    contentDescription = "School Logo",
                    modifier = Modifier.requiredSize(56.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.formkit_date),
                        contentDescription = "Date",
                        modifier = Modifier.requiredSize(28.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ph_bell),
                        contentDescription = "Notifications",
                        modifier = Modifier
                            .requiredSize(28.dp)
                            .clickable { onNotificationClick() }
                    )
                    Image(
                        painter = painterResource(id = R.drawable.studentswitcher),
                        contentDescription = "Student Switcher",
                        modifier = Modifier
                            .requiredSize(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    // NEW BURGER MENU ICON
                    androidx.compose.material3.IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = ColorsDefaultTheme.color_Primary_green
                        )
                    }
                }
            }
        }

        // Horizontal filter buttons have been REMOVED as requested

        item {
            Image(
                painter = painterResource(id = R.drawable.card),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.FillWidth
            )
        }

        item {
            QuickStatsSection()
        }

        item {
            SectionPlaceholder(title = "Upcoming Events", emptyText = "No events yet.")
        }
        item {
            SectionPlaceholder(title = "Recent Activities", emptyText = "No activities yet.")
        }
    }
}

@Composable
fun HomeMenuDrawer(onItemClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "Menu",
            style = AppTypes.type_H1,
            color = Color(0xFF1B4D13),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val menuItems = listOf("Analytics", "Upcoming events", "Recent activities")
        menuItems.forEach { label ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(label) }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = AppTypes.type_H1.copy(fontSize = 18.sp),
                    color = ColorsDefaultTheme.color_Surface_on_surface
                )
            }
            androidx.compose.material3.HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        }
    }
}

// Extracted for cleanliness
@Composable
fun QuickStatsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Quick Stats",
            color = Color(0xFF1B4D13),
            style = AppTypes.type_H1,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard("Attendance", "98%", R.drawable.boxicons_calendar_check_filled, Modifier.weight(1f))
            StatCard("GPA", "1.5", R.drawable.material_symbols_owl, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard("Pending due", "0.00", R.drawable.boxicons_wallet_filled, Modifier.weight(1f))
            StatCard("Notifications", "2", R.drawable.fluent_color_megaphone_loud_32, Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(label: String, value: String, iconRes: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .requiredHeight(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ColorsDefaultTheme.color_Surface)
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .requiredSize(32.dp)
                .align(Alignment.TopStart),
            colorFilter = ColorFilter.tint(ColorsDefaultTheme.color_Primary_on_green)
        )
        Text(
            text = label,
            style = AppTypes.type_Caption,
            color = Color(0xFF1C1B1F),
            modifier = Modifier.align(Alignment.TopEnd)
        )
        Text(
            text = value,
            color = Color(0xFF1B4D13),
            style = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.BottomStart)
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
            color = Color(0xFF1B4D13),
            style = AppTypes.type_H1,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.video_conference_streamline_bangalore),
                contentDescription = null,
                modifier = Modifier.requiredSize(120.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = emptyText,
                style = AppTypes.type_Body_Small,
                color = ColorsDefaultTheme.color_Primary_on_green,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BodyPreview() {
    ParentAppTheme {
        HomeScreen()
    }
}
