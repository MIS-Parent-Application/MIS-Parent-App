package com.mis.parentapp.features.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.network.StudyLoadSubject
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyLoadScreen(
    studentVM: StudentSharedViewModel,
    onBackClick: () -> Unit
) {
    val selectedStudent = studentVM.selectedStudent
    var subjects by remember { mutableStateOf<List<StudyLoadSubject>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedStudent?.id) {
        isLoading = true
        errorMessage = null
        try {
            subjects = selectedStudent?.let { RetrofitInstance.api.getStudyLoad(it.id) } ?: emptyList()
        } catch (e: Exception) {
            errorMessage = "Unable to load study load."
            subjects = selectedStudent?.studyLoad ?: emptyList()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Study Load", style = AppTypes.type_H1, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = selectedStudent?.name ?: "Student",
                color = Color(0xFF1B4D13),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Text(
                text = "${selectedStudent?.course ?: "--"} | ${selectedStudent?.rollNumber ?: "--"}",
                color = Color.Gray,
                fontSize = 14.sp
            )

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                errorMessage != null && subjects.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage ?: "", color = Color.Red)
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        StudyLoadSummary(subjects)
                    }
                    items(subjects) { subject ->
                        StudyLoadSubjectCard(subject)
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyLoadSummary(subjects: List<StudyLoadSubject>) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        SummaryBox("Subjects", subjects.size.toString(), Modifier.weight(1f))
        SummaryBox("Units", subjects.sumOf { it.units }.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun SummaryBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(96.dp)
            .background(ColorsDefaultTheme.color_Surface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 13.sp)
        Text(value, color = Color(0xFF1B4D13), fontSize = 30.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StudyLoadSubjectCard(subject: StudyLoadSubject) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9).copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = Color(0xFF1B4D13),
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(subject.code, color = Color(0xFF1B4D13), fontWeight = FontWeight.Bold)
                Text(subject.title, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subject.instructor, color = Color.DarkGray, fontSize = 13.sp)
                Text("${subject.schedule} | ${subject.room}", color = Color.Gray, fontSize = 12.sp)
            }
            Text("${subject.units} units", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}


