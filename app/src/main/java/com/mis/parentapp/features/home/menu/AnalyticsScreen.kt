package com.mis.parentapp.features.home.menu

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

@Composable
fun AnalyticsScreen(onBackClick: () -> Unit) {
    var selectedTab by remember { mutableStateOf("Summary") }

    Scaffold(
        topBar = {
            AnalyticsHeader(onBackClick)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                AnalyticsTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            when (selectedTab) {
                "Summary" -> {
                    item { GpaCard() }
                    item { StatsRow() }
                    item { AcademicYearCard() }
                    item { AcademicTrendCard() }
                }
                "Grades" -> {
                    item { EnrolledCoursesCard() }
                    item {
                        Text(
                            text = "Breakdown",
                            style = AppTypes.type_H1,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    item { CourseBreakdownCard("English 101", "Mr. Doe") }
                }
                "Attendance" -> {
                    item { AcademicYearCard() }
                    item { OverallAttendanceCard() }
                }
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
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Analytics",
                style = AppTypes.type_H2,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = { /* More options */ }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Text(
            text = "John B. Mclure 3rd Yr. BSIT 1A",
            style = AppTypes.type_Caption,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AnalyticsTabs(selectedTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf("Summary", "Grades", "Attendance", "Export")

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
                    .clickable { onTabSelected(tab) },
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = tab,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
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
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "GPA",
                    style = AppTypes.type_Caption,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
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
        StatCircle(label = "Exam & Quiz", percentage = 0.46f, points = "78 points", color = MaterialTheme.colorScheme.secondary)
        StatCircle(label = "Assignment", percentage = 0.74f, points = "127 points", color = MaterialTheme.colorScheme.primary)
        StatCircle(label = "Projects", percentage = 0.14f, points = "22 points", color = MaterialTheme.colorScheme.outline)
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
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
            Text(
                text = "${(percentage * 100).toInt()}%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = AppTypes.type_Caption, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = points, style = AppTypes.type_Caption, color = MaterialTheme.colorScheme.outline)
        Text(text = "earned", style = AppTypes.type_Caption, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun AcademicYearCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "A.Y. 2025-2026",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { /* Next */ }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                val errorColor = MaterialTheme.colorScheme.error
                val outlineColor = MaterialTheme.colorScheme.outline
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 12.dp.toPx()
                    // Simplified donut chart representation
                    drawArc(primaryColor, -90f, 250f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                    drawArc(secondaryColor, 160f, 40f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                    drawArc(errorColor, 200f, 15f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                    drawArc(outlineColor, 215f, 55f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "205",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "days of this A.Y.",
                        style = AppTypes.type_Caption,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    LegendItem(MaterialTheme.colorScheme.outline, "Excuse", "15 ds")
                    LegendItem(MaterialTheme.colorScheme.error, "Absent", "2 ds")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    LegendItem(MaterialTheme.colorScheme.secondary, "Late", "20 ds")
                    LegendItem(MaterialTheme.colorScheme.primary, "Present", "185 ds")
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
        Text(
            text = label,
            style = AppTypes.type_Body_Small,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = AppTypes.type_Body_Small,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AcademicTrendCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Academic Trend",
                    style = AppTypes.type_H2,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "2025-2026",
                    color = MaterialTheme.colorScheme.outline,
                    style = AppTypes.type_Caption
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Outputs", style = AppTypes.type_Caption, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Performance", style = AppTypes.type_Caption, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            TrendChart()
        }
    }
}

@Composable
fun TrendChart() {
    val yLabels = listOf("100%", "95%", "80%", "75%", "60%")
    val data = listOf(
        Pair(0.80f, 0.97f), // Jan
        Pair(0.97f, 0.99f), // Feb
        Pair(0.82f, 0.95f), // Mar
        Pair(0.80f, 0.97f), // Apr
        Pair(0.93f, 1.00f), // May
        Pair(0.80f, 1.00f)  // Jun
    )
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
    val performanceColor = MaterialTheme.colorScheme.primary
    val outputsColor = MaterialTheme.colorScheme.secondary

    Row(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        // Y-axis labels
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            yLabels.forEach { label ->
                Text(
                    text = label,
                    style = AppTypes.type_Caption,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Chart area
        Row(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, pair ->
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Performance bar (Dark Green)
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .fillMaxHeight(pair.first)
                                .background(performanceColor, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                        // Outputs bar (Light Green)
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .fillMaxHeight(pair.second)
                                .background(outputsColor, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = months[index],
                        style = AppTypes.type_Caption,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EnrolledCoursesCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enrolled courses",
                    style = AppTypes.type_H2,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "2025-2026",
                    color = MaterialTheme.colorScheme.outline,
                    style = AppTypes.type_Caption
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            val courses = listOf(
                "Eng 101" to 0.85f,
                "Math 101" to 0.75f,
                "Art" to 0.80f,
                "IntCom" to 0.90f,
                "Bio 101" to 0.95f,
                "Physics" to 0.78f
            )

            courses.forEach { (name, grade) ->
                CourseGradeItem(name, grade)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CourseGradeItem(name: String, grade: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = name,
            style = AppTypes.type_Body_Small,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.width(80.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(grade)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(7.dp))
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${(grade * 100).toInt()}%",
            style = AppTypes.type_Body_Small,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CourseBreakdownCard(courseName: String, instructor: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = courseName,
                    style = AppTypes.type_H2,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Instructor: $instructor",
                    color = MaterialTheme.colorScheme.outline,
                    style = AppTypes.type_Caption
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            CourseGradeItem("Quizzes", 0.85f)
            Spacer(modifier = Modifier.height(16.dp))
            CourseGradeItem("Attendance", 0.90f)
            Spacer(modifier = Modifier.height(16.dp))
            CourseGradeItem("Final Exam", 0.82f)
        }
    }
}

@Composable
fun OverallAttendanceCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Over all attendance",
                style = AppTypes.type_H2,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "A.Y. 2025-2026",
                    style = AppTypes.type_Caption,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.width(100.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(7.dp))
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "85%",
                    style = AppTypes.type_Body_Small,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
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
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Track attendance", color = MaterialTheme.colorScheme.onSurface, style = AppTypes.type_M3_label_small)
        }
        Button(
            onClick = { /* Monitor academic */ },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Monitor academic", color = MaterialTheme.colorScheme.onSurface, style = AppTypes.type_M3_label_small)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    AnalyticsScreen(onBackClick = {})
}
