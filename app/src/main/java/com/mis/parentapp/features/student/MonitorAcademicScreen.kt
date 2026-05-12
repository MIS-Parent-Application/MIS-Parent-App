package com.mis.parentapp.features.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.data.CourseGrade
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import com.mis.parentapp.ui.theme.ParentAppTheme
import java.util.Locale

// 1. THE WRAPPER: This is what the real app uses. It talks to the Room Database.
@Composable
fun MonitorAcademicScreen(
    viewModel: StudentViewModel,
    onBackClick: () -> Unit
) {
    val grades by viewModel.grades.collectAsState()
    val gpa by viewModel.gpa.collectAsState()

    // Passes the real database data into the UI
    MonitorAcademicContent(
        grades = grades,
        gpa = gpa,
        onBackClick = onBackClick,
        onAddGrade = { subject, units, gradeVal ->
            viewModel.addGrade(subject, units, gradeVal)
        }
    )
}

// 2. THE UI CONTENT: This is "dumb". It just displays whatever data you give it.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorAcademicContent(
    grades: List<CourseGrade>,
    gpa: Double,
    onBackClick: () -> Unit,
    onAddGrade: (String, Int, Double) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Academic Progress", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = ColorsDefaultTheme.color_Primary_green_container
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ColorsDefaultTheme.color_Primary_green_container,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Grade")
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorsDefaultTheme.color_Primary_green_container),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Cumulative GPA", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                    Text(
                        text = String.format(Locale.US, "%.2f", gpa),
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Course Grades", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ColorsDefaultTheme.color_On_surface)
            Spacer(modifier = Modifier.height(12.dp))

            if (grades.isEmpty()) {
                Text("No grades recorded yet. Click the + button to add one.", color = Color.Gray, modifier = Modifier.padding(top = 16.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(grades) { grade ->
                        GradeItemCard(grade)
                    }
                }
            }
        }

        if (showAddDialog) {
            AddGradeDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { subject, units, gradeVal ->
                    onAddGrade(subject, units, gradeVal)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun GradeItemCard(grade: CourseGrade) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = grade.subjectName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ColorsDefaultTheme.color_On_surface)
                Text(text = "${grade.units} Units", fontSize = 14.sp, color = Color.Gray)
            }
            Box(
                modifier = Modifier
                    .background(ColorsDefaultTheme.color_Primary_green_container.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = grade.grade.toString(),
                    fontWeight = FontWeight.Bold,
                    color = ColorsDefaultTheme.color_Primary_green_container
                )
            }
        }
    }
}

@Composable
fun AddGradeDialog(onDismiss: () -> Unit, onAdd: (String, Int, Double) -> Unit) {
    var subject by remember { mutableStateOf("") }
    var units by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Course Grade") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject (e.g., MATH 101)") })
                OutlinedTextField(
                    value = units,
                    onValueChange = { units = it },
                    label = { Text("Units (e.g., 3)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    label = { Text("Grade (e.g., 1.5)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val u = units.toIntOrNull() ?: 0
                    val g = grade.toDoubleOrNull() ?: 0.0
                    if (subject.isNotBlank() && u > 0) {
                        onAdd(subject, u, g)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ColorsDefaultTheme.color_Primary_green_container)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)) {
                Text("Cancel")
            }
        }
    )
}

// 3. THE PREVIEW: We feed fake data into the dumb UI so Android Studio can render it!
@Preview(showBackground = true, widthDp = 360)
@Composable
fun MonitorAcademicPreview() {
    ParentAppTheme {
        MonitorAcademicContent(
            grades = listOf(
                CourseGrade(subjectName = "MATH 101", units = 3, grade = 1.5),
                CourseGrade(subjectName = "IT 302", units = 3, grade = 1.0),
                CourseGrade(subjectName = "PE 4", units = 2, grade = 1.25)
            ),
            gpa = 1.25,
            onBackClick = {},
            onAddGrade = { _, _, _ -> }
        )
    }
}