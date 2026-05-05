package com.mis.parentapp.features.services

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.mis.parentapp.R
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import com.mis.parentapp.ui.theme.ParentAppTheme
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.lazy.items  // ← Add this line

// Data class for payment records


// ================= SERVICES SCREEN =================

@Composable
fun ServicesScreen(modifier: Modifier = Modifier) {
    var showPaymentScreen by remember { mutableStateOf(false) }
    var paymentHistory by remember { mutableStateOf(listOf<PaymentRecord>()) }
    var invoiceCounter by remember { mutableStateOf(1) }

    if (showPaymentScreen) {
        ContributionDuesSelectionScreen(
            onBack = { showPaymentScreen = false },
            onPaymentSuccess = { records ->
                paymentHistory = paymentHistory + records
                invoiceCounter += records.size
                showPaymentScreen = false
            },
            currentInvoiceNumber = invoiceCounter
        )
    } else {
        Body(
            modifier = modifier,
            onPayClick = { showPaymentScreen = true },
            paymentHistory = paymentHistory
        )
    }
}

// ================= BODY =================

@Composable
fun Body(
    modifier: Modifier = Modifier,
    onPayClick: () -> Unit,
    paymentHistory: List<PaymentRecord>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item { HeaderSection() }
        item { FilterButtonsSection() }
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

// ================= ORIGINAL UI COMPONENTS (UNCHANGED) =================

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.school_logo),
            contentDescription = "School Logo",
            modifier = Modifier
                .size(56.dp)
                .clickable { }
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.formkit_date),
                contentDescription = "Date",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { }
            )
            Image(
                painter = painterResource(id = R.drawable.ph_bell),
                contentDescription = "Notifications",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { }
            )
            Image(
                painter = painterResource(id = R.drawable.studentswitcher),
                contentDescription = "Student Switcher",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { },
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun FilterButtonsSection() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        listOf("Accounting", "Forms & documents", "Payment options").forEach { label ->
            val isSelected = label == "Accounting"
            Button(
                onClick = { },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) ColorsDefaultTheme.color_Primary_green else Color(0xFFF5F5F5),
                    contentColor = if (isSelected) Color.White else ColorsDefaultTheme.color_Surface_on_surface
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = label, style = AppTypes.type_M3_label_small)
            }
        }
    }
}

@Composable
fun ContributionDuesSection(onPayClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Contribution dues",
            color = Color(0xFF1B4D13),
            style = AppTypes.type_H2,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                label = "School uniform",
                value = "200",
                iconRes = R.drawable.hugeicons_school,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "P.E. uniform",
                value = "200",
                iconRes = R.drawable.material_symbols_light_physical_therapy_outline,
                modifier = Modifier.weight(1f)
            )
        }
        Button(
            onClick = onPayClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorsDefaultTheme.color_Primary_green,
                contentColor = ColorsDefaultTheme.color_Yellow
            ),
            modifier = Modifier.fillMaxWidth().height(40.dp)
        ) {
            Text(text = "Pay all", style = AppTypes.type_M3_label_small)
        }
    }
}

@Composable
fun PaymentHistorySection(
    modifier: Modifier = Modifier,
    paymentHistory: List<PaymentRecord>
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("Recent") }

    val filteredHistory = remember(paymentHistory, selectedFilter) {
        filterPaymentHistory(paymentHistory, selectedFilter)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.Top),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Payment history",
            color = Color(0xFF1B4D13),
            style = AppTypes.type_H1,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("Recent", "Last year", "Last month", "Last week").forEach { label ->
                val isSelected = label == selectedFilter
                Button(
                    onClick = { selectedFilter = label },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) ColorsDefaultTheme.color_Primary_green else Color(0xFFF5F5F5),
                        contentColor = if (isSelected) Color.White else ColorsDefaultTheme.color_Surface_on_surface
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
                color = Color(0xFF1B4D13),
                style = TextStyle(fontSize = 64.sp, fontWeight = FontWeight.Black)
            )
            Text(
                text = "PHP",
                color = Color(0xFF1B4D13),
                style = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Light)
            )
            Text(
                text = "Overall total dues paid",
                color = Color(0xFF1B4D13),
                style = AppTypes.type_Caption
            )
        }

        Text(
            text = "Break down of fees",
            color = Color(0xFF1B4D13),
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
            // ✅ Use regular Column instead (outer LazyColumn already handles scrolling)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
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
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ================= OFFICIAL ANDROID DOWNLOAD LOGIC =================

@SuppressLint("NewApi")
private fun generateReceiptPDF(context: Context, record: PaymentRecord) {
    try {
        val fileName = "Receipt_${record.invoiceNumber}.pdf"

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
        } catch (e: Exception) {
            null
        }

        if (recordDate == null) return@filter false

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
        try { dateFormat.parse(it.paidDate)?.time ?: 0L } catch (e: Exception) { 0L }
    }
}


@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BodyPreview() {
    ParentAppTheme {
        ServicesScreen()
    }
}

