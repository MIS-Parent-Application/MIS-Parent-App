package com.mis.parentapp.features.student

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mis.parentapp.R
import com.mis.parentapp.network.Child
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.network.StudyLoadSubject
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import com.mis.parentapp.ui.theme.ParentAppTheme
import java.io.IOException

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
    val context = LocalContext.current

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
                title = { Text("Study Load", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        enabled = selectedStudent != null && subjects.isNotEmpty(),
                        onClick = {
                            selectedStudent?.let {
                                exportStudyLoadPdf(context, it, subjects)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download study load")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF7FAEF))
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                errorMessage != null && subjects.isEmpty() -> Text(
                    text = errorMessage ?: "",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
                selectedStudent == null -> Text(
                    text = "Select a student to view study load.",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> OfficialStudyLoadDocument(
                    student = selectedStudent,
                    subjects = subjects,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun OfficialStudyLoadDocument(
    student: Child,
    subjects: List<StudyLoadSubject>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var zoom by remember { mutableStateOf(0.86f) }
    val semester = subjects.firstOrNull()?.semester ?: "2nd Sem."
    val schoolYear = subjects.firstOrNull()?.schoolYear ?: "S.Y. 2025-2026"
    val dateEnrolled = subjects.firstOrNull()?.dateEnrolled ?: "--"

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StudyLoadZoomBar(
            zoom = zoom,
            onZoomOut = { zoom = (zoom - 0.06f).coerceAtLeast(0.70f) },
            onZoomIn = { zoom = (zoom + 0.06f).coerceAtMost(1.0f) },
            onFit = { zoom = 0.86f }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFE4E9D4), RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box {
                Image(
                    painter = painterResource(id = R.drawable.school_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(280.dp)
                        .alpha(0.08f),
                    contentScale = ContentScale.Fit
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StudyLoadHeader(semester = semester, schoolYear = schoolYear, zoom = zoom)
                    StudentInfoBlock(student = student, zoom = zoom)
                    StudyLoadTable(subjects = subjects, zoom = zoom)
                    StudyLoadFooter(subjects = subjects, dateEnrolled = dateEnrolled, zoom = zoom)
                }
            }
        }
    }
}

@Composable
private fun StudyLoadZoomBar(
    zoom: Float,
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit,
    onFit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(999.dp))
            .border(1.dp, Color(0xFFE4E9D4), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Document size",
            color = ColorsDefaultTheme.color_Primary_green_container,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onZoomOut, contentPadding = PaddingValues(horizontal = 10.dp)) {
                Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Text(
                text = "${(zoom * 100).toInt()}%",
                color = Color.DarkGray,
                fontSize = 12.sp,
                modifier = Modifier.width(44.dp),
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onZoomIn, contentPadding = PaddingValues(horizontal = 10.dp)) {
                Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            TextButton(onClick = onFit, contentPadding = PaddingValues(horizontal = 10.dp)) {
                Text("Fit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StudyLoadHeader(semester: String, schoolYear: String, zoom: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.school_logo),
            contentDescription = "Colegio De Alicia logo",
            modifier = Modifier.size((72 * zoom).dp),
            contentScale = ContentScale.Fit
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "COLEGIO DE ALICIA",
                color = ColorsDefaultTheme.color_Primary_green_container,
                fontSize = scaledSp(25f, zoom),
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Alicia, Isabela",
                color = Color.DarkGray,
                fontSize = scaledSp(12f, zoom),
                textAlign = TextAlign.Center
            )
            Text(
                text = "OFFICIAL STUDY LOAD",
                color = Color.Black,
                fontSize = scaledSp(15f, zoom),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$semester $schoolYear",
                color = ColorsDefaultTheme.color_Primary_green_container,
                fontWeight = FontWeight.Bold,
                fontSize = scaledSp(13f, zoom)
            )
            Text(text = "View only", color = Color.Gray, fontSize = scaledSp(10f, zoom))
        }
    }
}

@Composable
private fun StudentInfoBlock(student: Child, zoom: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF6FDE7), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        StudyLoadInfo(label = "ID NO.", value = student.rollNumber, zoom = zoom, modifier = Modifier.weight(0.9f))
        StudyLoadInfo(label = "STUDENT", value = student.name.uppercase(), zoom = zoom, modifier = Modifier.weight(1.45f))
        StudyLoadInfo(label = "PROGRAM", value = student.course.uppercase(), zoom = zoom, modifier = Modifier.weight(1.25f))
        StudyLoadInfo(label = "SECTION", value = student.section.uppercase(), zoom = zoom, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StudyLoadInfo(label: String, value: String, zoom: Float, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, color = Color.Gray, fontSize = scaledSp(8.5f, zoom), fontWeight = FontWeight.Bold)
        Text(
            text = value,
            color = Color.Black,
            fontSize = scaledSp(10f, zoom),
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StudyLoadTable(subjects: List<StudyLoadSubject>, zoom: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        StudyLoadTableRow(
            values = listOf("SCHED. NO.", "COURSE NO.", "TIME", "DAYS", "ROOM", "UNITS", "REMARKS"),
            isHeader = true,
            zoom = zoom
        )
        subjects.forEach { subject ->
            StudyLoadTableRow(
                values = listOf(
                    subject.scheduleNumber.ifBlank { "--" },
                    subject.courseNumber.ifBlank { subject.code },
                    subject.time.ifBlank { subject.schedule },
                    subject.days.ifBlank { "--" },
                    subject.room,
                    subject.units.toString(),
                    subject.remarks
                ),
                zoom = zoom
            )
        }
    }
}

@Composable
private fun StudyLoadTableRow(values: List<String>, isHeader: Boolean = false, zoom: Float) {
    val weights = listOf(0.95f, 1.15f, 1.45f, 0.7f, 0.85f, 0.65f, 1.15f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFF9EA58F))
            .background(if (isHeader) ColorsDefaultTheme.color_Primary_green_container else Color.White)
    ) {
        values.forEachIndexed { index, value ->
            Text(
                text = value,
                color = if (isHeader) Color.White else Color.Black,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                fontSize = if (isHeader) scaledSp(7.4f, zoom) else scaledSp(8.2f, zoom),
                textAlign = if (index in listOf(3, 4, 5)) TextAlign.Center else TextAlign.Start,
                maxLines = if (isHeader) 1 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(weights[index])
                    .heightIn(min = (27 * zoom).dp)
                    .border(0.5.dp, Color(0xFF9EA58F))
                    .padding(horizontal = 3.dp, vertical = (7 * zoom).dp)
            )
        }
    }
}

@Composable
private fun StudyLoadFooter(subjects: List<StudyLoadSubject>, dateEnrolled: String, zoom: Float) {
    val totalUnits = subjects.sumOf { it.units }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalDivider(color = Color(0xFF1B4D13), thickness = 1.dp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("DATE ENROLLED: $dateEnrolled", fontSize = scaledSp(10f, zoom), fontWeight = FontWeight.Bold)
            Text("TOTAL: $totalUnits", fontSize = scaledSp(10f, zoom), fontWeight = FontWeight.Bold)
        }
        Text("LEGEND: (W) = Withdrawn     ** = Dissolved Subject", fontSize = scaledSp(9f, zoom), fontFamily = FontFamily.Monospace)
        Text(
            text = "This official study load is generated from Colegio De Alicia parent portal records.",
            fontSize = scaledSp(9f, zoom),
            color = Color.DarkGray
        )
        Text(
            text = "Generated copy is for parent/student viewing and verification purposes.",
            color = ColorsDefaultTheme.color_Primary_green_container,
            fontWeight = FontWeight.Bold,
            fontSize = scaledSp(9f, zoom)
        )
    }
}

private fun scaledSp(base: Float, zoom: Float): TextUnit = (base * zoom).sp

@SuppressLint("NewApi")
private fun exportStudyLoadPdf(context: Context, student: Child, subjects: List<StudyLoadSubject>) {
    try {
        val safeName = student.name.replace(Regex("[^A-Za-z0-9]"), "_")
        val fileName = "StudyLoad_${safeName}_${student.rollNumber}.pdf"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IOException("Failed to create study load file")

        resolver.openOutputStream(uri)?.use { outputStream ->
            StudyLoadPdfGenerator().createPdfContent(context, outputStream, student, subjects)
        }

        contentValues.clear()
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)
        Toast.makeText(context, "Study load downloaded", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@Preview(showBackground = true)
@Composable
private fun StudyLoadScreenPreview() {
    ParentAppTheme {
        StudyLoadScreen(studentVM = StudentSharedViewModel(), onBackClick = {})
    }
}
