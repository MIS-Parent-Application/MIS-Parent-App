package com.mis.parentapp.features.student.menu

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ParentAppTheme

data class AcademicGradeItem(
    val subjectName: String,
    val units: Int,
    val grade: Double,
    val instructor: String
)

data class AcademicPerformanceItem(
    val id: Int,
    val type: String,
    val title: String,
    val summary: String,
    val details: String,
    val criteria: String,
    val subject: String,
    val teacher: String,
    val imageUrl: String?,
    val score: String?,
    val status: String,
    val assignedDate: String,
    val dueDate: String,
    val timeAgo: String,
    val isPositive: Boolean
)

// --- 1. THE WRAPPER (Integrated with Teammate's API Logic) ---
@Composable
fun MonitorAcademicScreen(
    studentVM: StudentSharedViewModel,
    onBackClick: () -> Unit,
    onDetailTopBarChange: (
        isDetailOpen: Boolean,
        detailBackAction: (() -> Unit)?,
        detailShareAction: (() -> Unit)?
    ) -> Unit = { _, _, _ -> }
) {
    val selectedStudent = studentVM.selectedStudent
    var grades by remember { mutableStateOf<List<AcademicGradeItem>>(emptyList()) }
    var performance by remember { mutableStateOf<List<AcademicPerformanceItem>>(emptyList()) }
    var gradesMessage by remember { mutableStateOf<String?>(null) }
    var performanceMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedStudent?.id) {
        grades = emptyList()
        performance = emptyList()
        gradesMessage = null
        performanceMessage = null
        val studentId = selectedStudent?.id ?: return@LaunchedEffect
        runCatching {
            RetrofitInstance.api.getStudentGrades(studentId).map {
                AcademicGradeItem(
                    subjectName = it.subjectName,
                    units = it.units,
                    grade = it.grade,
                    instructor = it.instructor
                )
            }
        }.onSuccess {
            grades = it
        }.onFailure {
            gradesMessage = "Unable to load grades from the server."
        }

        runCatching {
            RetrofitInstance.api.getAcademicPerformance(studentId).map {
                AcademicPerformanceItem(
                    id = it.id,
                    type = it.type,
                    title = it.title,
                    summary = it.summary,
                    details = it.details,
                    criteria = it.criteria,
                    subject = it.subject,
                    teacher = it.teacher,
                    imageUrl = it.imageUrl,
                    score = it.score,
                    status = it.status,
                    assignedDate = it.assignedDate,
                    dueDate = it.dueDate,
                    timeAgo = it.timeAgo,
                    isPositive = it.isPositive
                )
            }
        }.onSuccess {
            performance = it
        }.onFailure {
            performanceMessage = "Unable to load performance records from the server."
        }
    }

    MonitorAcademicContent(
        grades = grades,
        performance = performance,
        gradeEmptyMessage = gradesMessage ?: "No official grade records yet.",
        performanceEmptyMessage = performanceMessage ?: "No official performance records yet.",
        onDetailTopBarChange = onDetailTopBarChange
    )
}

// --- 2. THE UI CONTENT ---
@Composable
fun MonitorAcademicContent(
    grades: List<AcademicGradeItem>,
    performance: List<AcademicPerformanceItem>,
    gradeEmptyMessage: String,
    performanceEmptyMessage: String,
    onDetailTopBarChange: (
        isDetailOpen: Boolean,
        detailBackAction: (() -> Unit)?,
        detailShareAction: (() -> Unit)?
    ) -> Unit = { _, _, _ -> }
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedPerformance by remember { mutableStateOf<AcademicPerformanceItem?>(null) }
    val context = LocalContext.current

    DisposableEffect(selectedPerformance) {
        val item = selectedPerformance
        if (item == null) {
            onDetailTopBarChange(false, null, null)
        } else {
            onDetailTopBarChange(
                true,
                { selectedPerformance = null },
                { sharePerformanceTask(context, item) }
            )
        }
        onDispose {
            onDetailTopBarChange(false, null, null)
        }
    }

    selectedPerformance?.let {
        PerformanceDetailScreen(item = it)
        return
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CustomTabRow(
            selectedTabIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        when (selectedTab) {
            0 -> AllTabContent(grades, performance, gradeEmptyMessage, performanceEmptyMessage) { selectedPerformance = it }
            1 -> GradesTabContent(grades, gradeEmptyMessage)
            2 -> PerformanceTabContent(performance, performanceEmptyMessage) { selectedPerformance = it }
        }
    }
}

// --- TAB LAYOUTS ---

@Composable
fun AllTabContent(
    grades: List<AcademicGradeItem>,
    performance: List<AcademicPerformanceItem>,
    gradeEmptyMessage: String,
    performanceEmptyMessage: String,
    onPerformanceClick: (AcademicPerformanceItem) -> Unit
) {
    val leadAlert = performance.firstOrNull { it.type == "missing_input" || it.type == "low_score" }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        if (leadAlert != null) {
            item {
                CustomAlertCard(
                    title = leadAlert.title,
                    description = leadAlert.summary,
                    trailingText = leadAlert.subject.shortCourseLabel(),
                    trailingSubText = leadAlert.timeAgo,
                    icon = Icons.Default.Warning,
                    iconBackgroundColor = MaterialTheme.colorScheme.error,
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable { onPerformanceClick(leadAlert) }
                )
            }
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
                    item { EmptyAcademicMessage(gradeEmptyMessage, Modifier.width(280.dp)) }
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
            if (performance.isEmpty()) {
                EmptyAcademicMessage(
                    message = performanceEmptyMessage,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    performance.take(3).forEach { item ->
                        PerformanceAlertCard(item, onClick = { onPerformanceClick(item) })
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceAlertList(
    records: List<AcademicPerformanceItem>,
    emptyMessage: String,
    onPerformanceClick: (AcademicPerformanceItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (records.isEmpty()) {
        EmptyAcademicMessage(emptyMessage, modifier.fillMaxWidth())
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            records.forEach { item ->
                PerformanceAlertCard(item, onClick = { onPerformanceClick(item) })
            }
        }
    }
}

@Composable
fun GradesTabContent(grades: List<AcademicGradeItem>, emptyMessage: String) {
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
    val isDark = isSystemInDarkTheme()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) {
            Color(0xFF143B18)
        } else {
            Color(0xFFF6FDE7)
        }
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp,
            color = if (isDark) {
                Color(0xFFEFFFE8)
            } else {
                Color(0xFF1B5E20)
            }
        )
    }
}

@Composable
fun PerformanceTabContent(
    performance: List<AcademicPerformanceItem>,
    emptyMessage: String,
    onPerformanceClick: (AcademicPerformanceItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Performance",
                style = AppTypes.type_H2.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (performance.isEmpty()) {
            item { EmptyAcademicMessage(emptyMessage) }
        } else {
            val missing = performance.filter { it.type == "missing_input" }
            val lowScores = performance.filter { it.type == "low_score" }
            val highScores = performance.filter { it.type == "high_score" }

            performanceSection("Missing outputs", missing, onPerformanceClick)
            performanceSection("Low score outputs", lowScores, onPerformanceClick)
            performanceSection("High score outputs", highScores, onPerformanceClick)
            val other = performance.filterNot { it.type == "missing_input" || it.type == "low_score" || it.type == "high_score" }
            performanceSection("Other academic matters", other, onPerformanceClick)
        }
    }
}

private fun LazyListScope.performanceSection(
    title: String,
    records: List<AcademicPerformanceItem>,
    onPerformanceClick: (AcademicPerformanceItem) -> Unit
) {
    if (records.isEmpty()) return
    item {
        Text(
            text = title,
            style = AppTypes.type_Body_Small.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
    item {
        PerformanceAlertList(
            records = records,
            emptyMessage = "",
            onPerformanceClick = onPerformanceClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PerformanceDetailScreen(item: AcademicPerformanceItem) {
    val isDark = isSystemInDarkTheme()
    val detailContainer = if (isDark) {
        Color(0xFF143B18)
    } else {
        Color(0xFFF3FDE5)
    }
    val detailContent = if (isDark) {
        Color(0xFFF3FFE9)
    } else {
        Color(0xFF162013)
    }
    val detailSecondary = if (isDark) {
        Color(0xFFBFEFB5)
    } else {
        Color(0xFF345034)
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(detailContainer)
                    .padding(14.dp)
            ) {
                Text(
                    text = item.title,
                    style = AppTypes.type_H2.copy(fontSize = 20.sp),
                    color = detailContent,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = item.summary,
                    style = AppTypes.type_Body_Small,
                    color = detailContent.copy(alpha = 0.86f),
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                PerformanceMetaRow("Subject", item.subject, detailContent, detailSecondary)
                PerformanceMetaRow("Teacher", item.teacher, detailContent, detailSecondary)
                PerformanceMetaRow("Status", item.status, detailContent, detailSecondary)
                if (!item.score.isNullOrBlank()) {
                    PerformanceMetaRow("Score", item.score, detailContent, detailSecondary)
                }
                PerformanceMetaRow("Assigned", item.assignedDate, detailContent, detailSecondary)
                PerformanceMetaRow("Due", item.dueDate, detailContent, detailSecondary)
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = item.details,
                    style = AppTypes.type_Body_Small,
                    color = detailContent.copy(alpha = 0.9f),
                    lineHeight = 21.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = item.criteria,
                    style = AppTypes.type_Caption,
                    color = detailSecondary,
                    lineHeight = 18.sp
                )
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
fun GradientGradeCard(grade: AcademicGradeItem) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val gradeText = String.format(java.util.Locale.US, "%.1f", grade.grade)
    val gradeFontSize = when {
        gradeText.length >= 6 -> 38.sp
        gradeText.length >= 4 -> 46.sp
        else -> 56.sp
    }

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
                        Text(
                            text = grade.subjectName,
                            style = AppTypes.type_H2.copy(fontSize = 20.sp),
                            color = Color(0xFF1B5E20),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 23.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = grade.instructor,
                            style = AppTypes.type_Caption.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF2E7D32),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = "Instructor",
                            style = AppTypes.type_M3_label_small,
                            color = Color(0xFF2E7D32).copy(alpha = 0.82f),
                            maxLines = 1
                        )
                    }

                    val isPassed = grade.grade <= 3.0
                    Box(
                        modifier = Modifier
                            .background(if (isPassed) Color(0xFF4CAF50) else Color(0xFFD32F2F), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPassed) "PASSED" else "FAILED",
                            style = AppTypes.type_M3_label_small.copy(fontSize = 9.sp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 2. The flexible spacer! This acts as a spring, pushing the grade to the bottom.
                Spacer(modifier = Modifier.weight(1f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = gradeText,
                        fontSize = gradeFontSize,
                        fontWeight = FontWeight.Light,
                        color = Color(0xFF1B5E20),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 4.dp, end = 10.dp)
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
fun PerformanceAlertCard(item: AcademicPerformanceItem, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val iconBackground = if (item.isPositive) Color(0xFF22A53E) else Color(0xFFD64035)
    val container = if (isDark) {
        Color(0xFF123516)
    } else {
        Color(0xFFF1FBE7)
    }
    val titleColor = if (isDark) Color(0xFFF3FFE9) else Color(0xFF172018)
    val secondaryColor = if (isDark) Color(0xFFC8F2BE) else Color(0xFF3D4F3D)
    val actionColor = if (isDark) Color(0xFF93F27D) else MaterialTheme.colorScheme.primary
    val icon = if (item.isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = container),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(iconBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = item.title, tint = Color.White, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = item.title,
                    style = AppTypes.type_Body_Small.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.summary,
                    style = AppTypes.type_Caption,
                    color = secondaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.widthIn(min = 78.dp, max = 96.dp)
            ) {
                Text(
                    text = item.subject.shortCourseLabel(),
                    style = AppTypes.type_M3_label_small,
                    color = secondaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.timeAgo,
                    style = AppTypes.type_Caption.copy(fontWeight = FontWeight.Bold),
                    color = titleColor,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "View",
                    style = AppTypes.type_M3_label_small.copy(fontWeight = FontWeight.Bold),
                    color = actionColor
                )
            }
        }
    }
}

@Composable
fun PerformanceMetaRow(
    label: String,
    value: String,
    valueColor: Color,
    labelColor: Color
) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = AppTypes.type_Caption.copy(fontWeight = FontWeight.Bold),
            color = labelColor,
            modifier = Modifier.width(84.dp)
        )
        Text(
            text = value,
            style = AppTypes.type_Caption,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun sharePerformanceTask(context: Context, item: AcademicPerformanceItem) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, item.title)
        putExtra(Intent.EXTRA_TEXT, item.toShareText())
    }
    context.startActivity(Intent.createChooser(intent, "Share performance task"))
}

private fun AcademicPerformanceItem.toShareText(): String = buildString {
    appendLine(title)
    appendLine(subject)
    appendLine("Teacher: $teacher")
    if (!score.isNullOrBlank()) appendLine("Score: $score")
    appendLine("Status: $status")
    appendLine("Assigned: $assignedDate")
    appendLine("Due: $dueDate")
    appendLine()
    appendLine(summary)
    appendLine()
    appendLine(details)
    appendLine()
    appendLine(criteria)
}

private fun String.shortCourseLabel(): String {
    return substringBefore("-").trim().ifBlank { this }.take(12)
}

// Preview-only sample data.
fun getDummyGrades(): List<AcademicGradeItem> {
    return listOf(
        AcademicGradeItem(subjectName = "Math 101", units = 3, grade = 1.0, instructor = "Ms. Cruz"),
        AcademicGradeItem(subjectName = "English 101", units = 3, grade = 2.4, instructor = "Mr. Reyes")
    )
}

fun getDummyPerformance(): List<AcademicPerformanceItem> {
    return listOf(
        AcademicPerformanceItem(
            id = 1,
            type = "high_score",
            title = "High score in exam",
            summary = "Strong output for the recent exam.",
            details = "The student completed the task accurately and explained the solution during checking.",
            criteria = "Criteria: accuracy, completeness, and explanation.",
            subject = "English 101",
            teacher = "Ms. Cruz",
            imageUrl = null,
            score = "46/50",
            status = "Completed",
            assignedDate = "2026-05-18",
            dueDate = "2026-05-18",
            timeAgo = "4hrs ago",
            isPositive = true
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun MonitorAcademicPreview() {
    ParentAppTheme {
        MonitorAcademicContent(
            grades = getDummyGrades(),
            performance = getDummyPerformance(),
            gradeEmptyMessage = "No grades yet",
            performanceEmptyMessage = "No performance yet"
        )
    }
}
