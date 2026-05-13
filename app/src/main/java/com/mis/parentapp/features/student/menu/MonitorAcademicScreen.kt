package com.mis.parentapp.features.student.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.data.CourseGrade
import com.mis.parentapp.features.student.StudentViewModel
import com.mis.parentapp.ui.theme.ParentAppTheme
import java.util.Locale

// --- 1. THE WRAPPER ---
@Composable
fun MonitorAcademicScreen(
    viewModel: StudentViewModel,
    onBackClick: () -> Unit
) {
    val grades by viewModel.grades.collectAsState()

    MonitorAcademicContent(
        grades = grades,
        onBackClick = onBackClick
    )
}

// --- 2. THE UI CONTENT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorAcademicContent(
    grades: List<CourseGrade>,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Grades, 2: Performance

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Academic",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "John B. McLure 3rd Yr. BSIT 1A",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Menu action */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            CustomTabRow(
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Content based on selected tab
            when (selectedTab) {
                0 -> AllTabContent(grades)
                1 -> GradesTabContent(grades)
                2 -> PerformanceTabContent()
            }
        }
    }
}

// --- TAB LAYOUTS ---

@Composable
fun AllTabContent(grades: List<CourseGrade>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp) // Space for external bottom nav
    ) {
        item {
            MissingAssignmentAlert(modifier = Modifier.padding(16.dp))
        }
        item {
            Text("Grades", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val displayGrades = grades.ifEmpty { getDummyGrades() }
                items(displayGrades) { grade ->
                    Box(modifier = Modifier.width(280.dp)) {
                        GradientGradeCard(grade)
                    }
                }
            }
        }
        item {
            Text("Performance", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(3) { PerformanceOrangeCard() }
            }
        }
    }
}

@Composable
fun GradesTabContent(grades: List<CourseGrade>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val displayGrades = grades.ifEmpty { getDummyGrades() }
        items(displayGrades) { grade ->
            GradientGradeCard(grade)
        }
    }
}

@Composable
fun PerformanceTabContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text("Missing outputs", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(3) { PerformanceOrangeCard() }
            }
        }
        item {
            Text("High score outputs", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 24.dp, bottom = 12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(3) { PerformanceOrangeCard() }
            }
        }
    }
}

// --- UI COMPONENTS ---

@Composable
fun CustomTabRow(selectedTabIndex: Int, onTabSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    val tabs = listOf("All", "Grades", "Performance")
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Color(0xFF2E7D32) else Color(0xFFF5F5F5))
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = title,
                    color = if (isSelected) Color.White else Color.Black,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun MissingAssignmentAlert(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0)), // Soft pink
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Red, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Missing assignment", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                Text("Lorem ipsum dolor sit amet...", fontSize = 12.sp, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("English 101", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text("4hrs ago", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun GradientGradeCard(grade: CourseGrade) {
    // Diagonal gradient for a more premium feel
    val brush = Brush.linearGradient(
        colors = listOf(Color(0xFFF9FBE7), Color(0xFFAED581)),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Adds the drop shadow
    ) {
        Box(
            modifier = Modifier
                .background(brush)
                .padding(20.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column {
                        Text(grade.subjectName, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        Text("Mr. John Doe\nInstructor", fontSize = 12.sp, color = Color.DarkGray, lineHeight = 16.sp)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("100%\npassed", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold, lineHeight = 11.sp, textAlign = TextAlign.Center)
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format(Locale.US, "%.1f", grade.grade),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.Black
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("↘", fontSize = 18.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceOrangeCard() {
    Column(modifier = Modifier.width(150.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().height(110.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(Color(0xFFFFB74D), Color(0xFFFF9800))))
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Document 1", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(2.dp))
                Text("25.5.2026", fontSize = 11.sp, color = Color.Gray)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF2E7D32))
                    .clickable { /* TODO: View Action */ }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("View", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Helper function to show dummy data in the UI if the database is empty
fun getDummyGrades(): List<CourseGrade> {
    return listOf(
        CourseGrade(subjectName = "Math 101", units = 3, grade = 1.0),
        CourseGrade(subjectName = "English 101", units = 3, grade = 1.5)
    )
}

// --- 3. THE PREVIEW ---
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun MonitorAcademicPreview() {
    ParentAppTheme {
        MonitorAcademicContent(
            grades = getDummyGrades(),
            onBackClick = {}
        )
    }
}