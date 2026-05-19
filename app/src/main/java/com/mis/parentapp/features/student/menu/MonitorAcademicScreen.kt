package com.mis.parentapp.features.student.menu

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.data.CourseGrade
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.ui.theme.ParentAppTheme
import androidx.compose.ui.draw.drawBehind

// --- 1. THE WRAPPER (Integrated with Teammate's API Logic) ---
@Composable
fun MonitorAcademicScreen(
    studentVM: StudentSharedViewModel,
    onBackClick: () -> Unit
) {
    val selectedStudent = studentVM.selectedStudent
    var grades by remember { mutableStateOf<List<CourseGrade>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedStudent?.id) {
        grades = emptyList()
        errorMessage = null
        val studentId = selectedStudent?.id ?: return@LaunchedEffect
        runCatching {
            RetrofitInstance.api.getStudentGrades(studentId).map {
                CourseGrade(
                    subjectName = it.subjectName,
                    units = it.units,
                    grade = it.grade
                )
            }
        }.onSuccess {
            grades = it
        }.onFailure {
            errorMessage = "Unable to load grades from the server."
        }
    }

    MonitorAcademicContent(
        grades = grades,
        studentLabel = selectedStudent?.let { "${it.name} - ${it.section}" } ?: "No student selected",
        emptyMessage = errorMessage ?: "No official grade records yet.",
        onBackClick = onBackClick
    )
}

// --- 2. THE UI CONTENT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorAcademicContent(
    grades: List<CourseGrade>,
    studentLabel: String,
    emptyMessage: String,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

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
                            style = AppTypes.type_H2,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Repaired the duplicated text parameters here!
                        Text(
                            text = studentLabel,
                            style = AppTypes.type_Caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Menu action */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CustomTabRow(
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when (selectedTab) {
                0 -> AllTabContent(grades, emptyMessage)
                1 -> GradesTabContent(grades, emptyMessage)
                2 -> PerformanceTabContent()
            }
        }
    }
}

// --- TAB LAYOUTS ---

@Composable
fun AllTabContent(grades: List<CourseGrade>, emptyMessage: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            CustomAlertCard(
                title = "Missing assignment",
                description = "Please submit your final essay draft before the deadline.",
                trailingText = "English 101",
                trailingSubText = "4hrs ago",
                icon = Icons.Default.Warning,
                iconBackgroundColor = MaterialTheme.colorScheme.error,
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                contentColor = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(16.dp)
            )
        }
        item {
            Text(
                text = "Grades",
                style = AppTypes.type_H2.copy(fontSize = 20.sp),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (grades.isEmpty()) {
                    item { EmptyAcademicMessage(emptyMessage, Modifier.width(280.dp)) }
                }
                items(grades) { grade ->
                    Box(modifier = Modifier.width(280.dp)) {
                        GradientGradeCard(grade)
                    }
                }
            }
        }
        item {
            Text(
                text = "Performance",
                style = AppTypes.type_H2.copy(fontSize = 20.sp),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            )
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
fun GradesTabContent(grades: List<CourseGrade>, emptyMessage: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (grades.isEmpty()) {
            item { EmptyAcademicMessage(emptyMessage) }
        }
        items(grades) { grade ->
            GradientGradeCard(grade)
        }
    }
}

@Composable
fun EmptyAcademicMessage(message: String, modifier: Modifier = Modifier.fillMaxWidth()) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF6FDE7)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp,
            color = Color(0xFF1B5E20)
        )
    }
}

@Composable
fun PerformanceTabContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "High score outputs",
                style = AppTypes.type_H2.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )
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
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = title,
                    style = AppTypes.type_Body_Small,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun CustomAlertCard(
    title: String,
    description: String,
    trailingText: String,
    trailingSubText: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
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
                    .background(iconBackgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = AppTypes.type_Body_Small.copy(fontWeight = FontWeight.Bold), color = contentColor)
                Text(description, style = AppTypes.type_Caption, color = contentColor.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(trailingText, style = AppTypes.type_Caption.copy(fontWeight = FontWeight.Bold), color = contentColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text(trailingSubText, style = AppTypes.type_M3_label_small, color = contentColor.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun GradientGradeCard(grade: CourseGrade) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), // 1. Fixed height to guarantee uniformity!
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize() // Force the box to respect the 200.dp height
                .background(Color(0xFFE8F5E9))
                .drawBehind {
                    val brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor,
                            primaryColor.copy(alpha = 0f)
                        ),
                        radius = size.width,
                        center = Offset(size.width / 2f, size.height)
                    )
                    drawRect(brush)
                }
                .padding(20.dp)
        ) {
            // Force the Column to take up the full card height
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        // Added maxLines to ensure crazy long subject names don't break the UI
                        Text(grade.subjectName, style = AppTypes.type_H2, color = Color(0xFF1B5E20), maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Text("Mr. John Doe\nInstructor", style = AppTypes.type_Caption, color = Color(0xFF2E7D32), lineHeight = 16.sp)
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("100%\npassed", style = AppTypes.type_M3_label_small.copy(fontSize = 9.sp), color = Color.White, fontWeight = FontWeight.Bold, lineHeight = 11.sp, textAlign = TextAlign.Center)
                    }
                }

                // 2. The flexible spacer! This acts as a spring, pushing the grade to the bottom.
                Spacer(modifier = Modifier.weight(1f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format(java.util.Locale.US, "%.1f", grade.grade),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Light,
                        color = Color(0xFF1B5E20),
                        modifier = Modifier.padding(bottom = 4.dp)
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
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.width(160.dp)
    ) {
        Column {
            Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                drawRect(color = Color(0xFFF99623))
                drawCircle(color = Color(0xFFFBB430), radius = size.width * 0.75f, center = Offset(0f, size.height))
                drawCircle(color = Color(0xFFED811A), radius = size.width * 0.8f, center = Offset(size.width, size.height * 0.5f))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Document 1", style = AppTypes.type_Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("25.5.2026", style = AppTypes.type_Body_Small.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { /* TODO: View Action */ }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("View", color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Preview-only sample data.
fun getDummyGrades(): List<CourseGrade> {
    return listOf(
        CourseGrade(subjectName = "Math 101", units = 3, grade = 1.0),
        CourseGrade(subjectName = "English 101", units = 3, grade = 1.5)
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun MonitorAcademicPreview() {
    ParentAppTheme {
        MonitorAcademicContent(
            grades = getDummyGrades(),
            studentLabel = "Test Student - 3rd Yr",
            emptyMessage = "No grades yet",
            onBackClick = {}
        )
    }
}