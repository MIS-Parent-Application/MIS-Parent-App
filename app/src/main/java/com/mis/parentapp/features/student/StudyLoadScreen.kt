package com.mis.parentapp.features.student

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (selectedStudent != null && subjects.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { exportStudyLoadPdf(context, selectedStudent, subjects) }
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download study load",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                errorMessage != null && subjects.isEmpty() -> Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                selectedStudent == null -> Text(
                    text = "Select a student to view study load.",
                    color = MaterialTheme.colorScheme.outline,
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
    val horizontalScroll = rememberScrollState()
    val semester = subjects.firstOrNull()?.semester ?: "2nd Sem."
    val schoolYear = subjects.firstOrNull()?.schoolYear ?: "S.Y. 2025-2026"
    val dateEnrolled = subjects.firstOrNull()?.dateEnrolled ?: "--"

    Column(
        modifier = modifier
            .padding(16.dp)
            .horizontalScroll(horizontalScroll)
    ) {
        Column(
            modifier = Modifier
                .width(720.dp)
                .wrapContentHeight()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(22.dp)
                .verticalScroll(scrollState),
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
                    StudyLoadHeader(semester = semester, schoolYear = schoolYear)
                    StudentInfoBlock(student = student)
                    StudyLoadTable(subjects = subjects)
                    StudyLoadFooter(subjects = subjects, dateEnrolled = dateEnrolled)
                }
            }
        }
    }
}

@Composable
private fun StudyLoadHeader(semester: String, schoolYear: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.school_logo),
            contentDescription = "Colegio De Alicia logo",
            modifier = Modifier.size(72.dp),
            contentScale = ContentScale.Fit
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "COLEGIO DE ALICIA",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 25.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Alicia, Bohol",
                color = MaterialTheme.colorScheme.outline,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "OFFICIAL STUDY LOAD",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$semester $schoolYear",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(text = "View only", color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
        }
    }
}

@Composable
private fun StudentInfoBlock(student: Child) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        StudyLoadInfo(label = "ID NO.", value = student.rollNumber, modifier = Modifier.weight(0.9f))
        StudyLoadInfo(label = "STUDENT", value = student.name.uppercase(), modifier = Modifier.weight(1.45f))
        StudyLoadInfo(label = "PROGRAM", value = student.course.uppercase(), modifier = Modifier.weight(1.25f))
        StudyLoadInfo(label = "SECTION", value = student.section.uppercase(), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StudyLoadInfo(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, color = MaterialTheme.colorScheme.outline, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StudyLoadTable(subjects: List<StudyLoadSubject>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        StudyLoadTableRow(
            values = listOf("SCHED. NO.", "COURSE NO.", "TIME", "DAYS", "ROOM", "UNITS", "REMARKS"),
            isHeader = true
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
                )
            )
        }
    }
}

@Composable
private fun StudyLoadTableRow(values: List<String>, isHeader: Boolean = false) {
    val widths = listOf(76.dp, 104.dp, 132.dp, 58.dp, 68.dp, 52.dp, 128.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, MaterialTheme.colorScheme.outline)
            .background(if (isHeader) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
    ) {
        values.forEachIndexed { index, value ->
            Text(
                text = value,
                color = if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                fontSize = if (isHeader) 9.sp else 10.sp,
                textAlign = if (index in listOf(3, 4, 5)) TextAlign.Center else TextAlign.Start,
                maxLines = if (isHeader) 1 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .width(widths[index])
                    .heightIn(min = 30.dp)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    .padding(horizontal = 6.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun StudyLoadFooter(subjects: List<StudyLoadSubject>, dateEnrolled: String) {
    val totalUnits = subjects.sumOf { it.units }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("DATE ENROLLED: $dateEnrolled", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("TOTAL: $totalUnits", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text("LEGEND: (W) = Withdrawn     ** = Dissolved Subject", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(
            text = "This official study load is generated from Colegio De Alicia parent portal records.",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = "Generated copy is for parent/student viewing and verification purposes.",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

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
        openPdf(context, uri)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun openPdf(context: Context, uri: android.net.Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Open study load"))
    } catch (_: Exception) {
        Toast.makeText(context, "Downloaded. No PDF viewer found on this device.", Toast.LENGTH_LONG).show()
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun StudyLoadScreenPreview() {
//    ParentAppTheme {
//        StudyLoadScreen(studentVM = StudentSharedViewModel(), onBackClick = {})
//    }
//}
