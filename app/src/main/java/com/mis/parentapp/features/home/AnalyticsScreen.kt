package com.mis.parentapp.features.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme

@Composable
fun AnalyticsScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            AnalyticsHeader(onBackClick)
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                AnalyticsTabs()
            }
            item {
                GpaCard()
            }
            item {
                StatsRow()
            }
            item {
                AcademicYearCard()
            }
            item {
                AcademicTrendCard()
            }
            item {
                ActionButtons()
            }
        }
    }
}

@Composable
fun AnalyticsHeader(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Analytics",
                style = AppTypes.type_H2,
                color = Color.Black
            )
            IconButton(onClick = { /* More options */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
        }
        Text(
            text = "John B. Mclure 3rd Yr. BSIT 1A",
            style = AppTypes.type_Caption,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AnalyticsTabs() {
    val tabs = listOf("Summary", "Grades", "Attendance", "Export")
    var selectedTab by remember { mutableStateOf("Summary") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { selectedTab = tab },
                color = if (isSelected) ColorsDefaultTheme.color_Primary_green_container else ColorsDefaultTheme.color_Surface,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = tab,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) Color.White else ColorsDefaultTheme.color_On_surface,
                    style = AppTypes.type_M3_label_small,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GpaCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9),
                        Color(0xFFF1F8E9),
                        Color.White
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1FDE7)),
            modifier = Modifier.size(160.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "1.5",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "GPA",
                    style = AppTypes.type_Caption,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun StatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatCircle(label = "Exam & Quiz", percentage = 0.46f, points = "78 points", color = Color(0xFFDEF731))
        StatCircle(label = "Assignment", percentage = 0.74f, points = "127 points", color = Color(0xFF267D1E))
        StatCircle(label = "Projects", percentage = 0.14f, points = "22 points", color = Color.Gray)
    }
}

@Composable
fun StatCircle(label: String, percentage: Float, points: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
            CircularProgressIndicator(
                progress = { percentage },
                modifier = Modifier.fillMaxSize(),
                color = color,
                strokeWidth = 6.dp,
                trackColor = Color(0xFFF5F5F5),
                strokeCap = StrokeCap.Round,
            )
            Text(
                text = "${(percentage * 100).toInt()}%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = AppTypes.type_Caption, fontWeight = FontWeight.Bold)
        Text(text = points, style = AppTypes.type_Caption, color = Color.Gray)
    }
}

@Composable
fun AcademicYearCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1FDE7)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Prev */ }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                }
                Text(text = "A.Y. 2025-2026", fontWeight = FontWeight.Bold)
                IconButton(onClick = { /* Next */ }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 12.dp.toPx()
                    // Simplified donut chart representation
                    drawArc(Color(0xFF267D1E), -90f, 250f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                    drawArc(Color(0xFFDEF731), 160f, 40f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                    drawArc(Color.Red, 200f, 15f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                    drawArc(Color.Gray, 215f, 55f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "205", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(text = "days of this A.Y.", style = AppTypes.type_Caption)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    LegendItem(Color.Gray, "Excuse", "15 ds")
                    LegendItem(Color.Red, "Absent", "2 ds")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    LegendItem(Color(0xFFDEF731), "Late", "20 ds")
                    LegendItem(Color(0xFF267D1E), "Present", "185 ds")
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(120.dp)) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = AppTypes.type_Body_Small, modifier = Modifier.weight(1f))
        Text(text = value, style = AppTypes.type_Body_Small, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AcademicTrendCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1FDE7)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Academic Trend", style = AppTypes.type_H2, fontSize = 20.sp)
                Text(text = "2025-2026", color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFDEF731)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Outputs", style = AppTypes.type_Caption)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF267D1E)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Performance", style = AppTypes.type_Caption)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            // Placeholder for Bar Chart
            Row(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
                months.forEach { month ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.width(8.dp).height(100.dp).background(Color(0xFF267D1E), RoundedCornerShape(4.dp)))
                            Box(modifier = Modifier.width(8.dp).height(120.dp).background(Color(0xFFDEF731), RoundedCornerShape(4.dp)))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = month, style = AppTypes.type_Caption, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { /* Track attendance */ },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1FDE7)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Track attendance", color = Color.Black, style = AppTypes.type_M3_label_small)
        }
        Button(
            onClick = { /* Monitor academic */ },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1FDE7)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Monitor academic", color = Color.Black, style = AppTypes.type_M3_label_small)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    AnalyticsScreen(onBackClick = {})
}
