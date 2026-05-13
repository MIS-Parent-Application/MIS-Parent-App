package com.mis.parentapp.features.services

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mis.parentapp.R
import com.mis.parentapp.features.services.sections.SearchBarSection
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ParentAppTheme
import com.mis.parentapp.utilities.modals.ServiceAccountSwitchModal
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

// ================= SERVICES SCREEN =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    studentVM: StudentSharedViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val showPaymentScreen = remember { mutableStateOf(false) }
    val paymentHistory = remember { mutableStateOf(listOf<PaymentRecord>()) }
    val invoiceCounter = remember { mutableIntStateOf(1) }
    
    val selectedStudent = studentVM.selectedStudent
    val otherStudents = studentVM.students.filter { it.id != selectedStudent?.id }

    val sheetState = rememberModalBottomSheetState()
    var showAccountModal by remember { mutableStateOf(false) }

    // Camera Launcher for QR scanning placeholder
    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle scanning result here if using a real library
    }

    if (showPaymentScreen.value) {
        ContributionDuesSelectionScreen(
            onBack = { showPaymentScreen.value = false },
            onPaymentSuccess = { records ->
                paymentHistory.value += records
                invoiceCounter.intValue += records.size
            },
            currentInvoiceNumber = invoiceCounter.intValue
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Body(
                modifier = modifier,
                studentVM = studentVM,
                onPayClick = { showPaymentScreen.value = true },
                onProfileClick = { showAccountModal = true },
                onQrClick = {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (intent.resolveActivity(context.packageManager) != null) {
                        scannerLauncher.launch(intent)
                    } else {
                        Toast.makeText(context, "No camera app found", Toast.LENGTH_SHORT).show()
                    }
                },
                paymentHistory = paymentHistory.value
            )

            if (showAccountModal) {
                ModalBottomSheet(
                    onDismissRequest = { showAccountModal = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    ServiceAccountSwitchModal(
                        selectedStudent = selectedStudent,
                        otherStudents = otherStudents,
                        onStudentSelect = { studentVM.selectStudent(it) },
                        onSeeMoreClick = { /* Handle See More */ },
                        onDismiss = { showAccountModal = false }
                    )
                }
            }
        }
    }
}

// ================= BODY =================

@Composable
fun Body(
    modifier: Modifier = Modifier,
    studentVM: StudentSharedViewModel,
    onPayClick: () -> Unit,
    onProfileClick: () -> Unit,
    onQrClick: () -> Unit,
    paymentHistory: List<PaymentRecord>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item { Spacer(modifier = Modifier.height(60.dp)) } // Space for the floating top bar
        
        item {
            SearchBarSection(
                selectedStudent = studentVM.selectedStudent,
                onProfileClick = onProfileClick,
                onQrClick = onQrClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Image(
                painter = painterResource(id = R.drawable.program),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.FillWidth
            )
        }
        item { ContributionDuesSection(onPayClick = onPayClick) }
        item {
            PaymentHistorySection(
                modifier = Modifier.padding(horizontal = 16.dp),
                paymentHistory = paymentHistory
            )
        }
    }
}


@Composable
fun ContributionDuesSection(onPayClick: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = "Contribution dues", style = AppTypes.type_H2, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onPayClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Pay Now")
        }
    }
}

@Composable
fun PaymentHistorySection(
    modifier: Modifier = Modifier,
    paymentHistory: List<PaymentRecord>
) {
    val context = LocalContext.current
    val selectedFilter = remember { mutableStateOf("Recent") }

    val filteredHistory = remember(paymentHistory, selectedFilter.value) {
        filterPaymentHistory(paymentHistory, selectedFilter.value)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.Top),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Payment history",
            color = MaterialTheme.colorScheme.primary,
            style = AppTypes.type_H1,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        ) {
            listOf("Recent", "Last year", "Last month", "Last week").forEach { label ->
                val isSelected = label == selectedFilter.value
                Button(
                    onClick = { selectedFilter.value = label },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = label, style = AppTypes.type_M3_label_small)
                }
            }
        }

        val totalPaid = filteredHistory.sumOf { it.totalAmount }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = totalPaid.toInt().toString(),
                color = MaterialTheme.colorScheme.primary,
                style = TextStyle(fontSize = 64.sp, fontWeight = FontWeight.Black)
            )
            Text(
                text = "PHP",
                color = MaterialTheme.colorScheme.primary,
                style = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Light)
            )
            Text(
                text = "Overall total dues paid",
                color = MaterialTheme.colorScheme.onBackground,
                style = AppTypes.type_Caption
            )
        }

        Text(
            text = "Break down of fees",
            color = MaterialTheme.colorScheme.onBackground,
            style = AppTypes.type_Caption,
            fontWeight = FontWeight.Bold
        )

        if (paymentHistory.isEmpty()) {
            Text(
                text = "No receipts found",
                style = AppTypes.type_Caption,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else if (filteredHistory.isEmpty()) {
            Text(
                text = "No receipts found for this period",
                style = AppTypes.type_Caption,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            filteredHistory.forEach { record ->
                FeeCard(
                    invoice = record.invoiceNumber,
                    item = record.purchasedItem,
                    option = record.paymentOption,
                    date = record.paidDate,
                    onDownload = { generateReceiptPDF(context, record) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ================= PDF GENERATION =================

@SuppressLint("NewApi")
private fun generateReceiptPDF(context: Context, record: PaymentRecord) {
    try {
        val fileName = "Receipt_${record.invoiceNumber.replace("#", "")}.pdf"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IOException("Failed to create MediaStore entry")

        resolver.openOutputStream(uri)?.use { outputStream ->
            // Use the PDF generator from ReceiptPdfGenerator.kt
            ReceiptPdfGenerator().createPdfContent(context, outputStream, record)
        }

        contentValues.clear()
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)

        Toast.makeText(context, "✅ Receipt Successfully Downloaded", Toast.LENGTH_LONG).show()

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "❌ Download Failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// ================= UTILS =================

private fun filterPaymentHistory(
    paymentHistory: List<PaymentRecord>,
    filter: String
): List<PaymentRecord> {
    if (paymentHistory.isEmpty()) return emptyList()
    val dateFormat = SimpleDateFormat("MM-dd-yy | h:mm a", Locale.getDefault())
    val now = Calendar.getInstance()

    return paymentHistory.filter { record ->
        val recordDate = try {
            dateFormat.parse(record.paidDate)?.let { date ->
                Calendar.getInstance().apply { time = date }
            }
        } catch (_: Exception) {
            null
        } ?: return@filter false

        when (filter) {
            "Recent" -> {
                recordDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                        recordDate.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                        recordDate.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)
            }
            "Last week" -> {
                val diffInMillis = now.timeInMillis - recordDate.timeInMillis
                val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
                diffInDays in 1..7
            }
            "Last month" -> {
                val lastMonth = Calendar.getInstance().apply {
                    time = now.time
                    add(Calendar.MONTH, -1)
                }
                recordDate.get(Calendar.YEAR) == lastMonth.get(Calendar.YEAR) &&
                        recordDate.get(Calendar.MONTH) == lastMonth.get(Calendar.MONTH)
            }
            "Last year" -> {
                val lastYear = Calendar.getInstance().apply {
                    time = now.time
                    add(Calendar.YEAR, -1)
                }
                recordDate.get(Calendar.YEAR) == lastYear.get(Calendar.YEAR)
            }
            else -> true
        }
    }.sortedByDescending {
        try { dateFormat.parse(it.paidDate)?.time ?: 0L } catch (_: Exception) { 0L }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BodyPreview() {
    ParentAppTheme {
        ServicesScreen()
    }
}
