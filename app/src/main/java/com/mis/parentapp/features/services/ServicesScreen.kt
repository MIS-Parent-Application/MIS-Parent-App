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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itextpdf.text.*
import com.itextpdf.text.Image as PdfImage
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

// Data class for payment records
data class PaymentRecord(
    val invoiceNumber: String,
    val purchasedItem: String,
    val paymentOption: String,
    val paidDate: String,
    val totalAmount: Double,
    val pdfBreakdown: String = ""
)

// ================= SERVICES SCREEN =================

@Composable
fun ServicesScreen(modifier: Modifier = Modifier) {
    var showPaymentScreen by remember { mutableStateOf(false) }
    var paymentHistory by remember { mutableStateOf(listOf<PaymentRecord>()) }
    var invoiceCounter by remember { mutableIntStateOf(1) }

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
fun StatCard(label: String, value: String, iconRes: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ColorsDefaultTheme.color_Surface)
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.TopStart),
            colorFilter = ColorFilter.tint(ColorsDefaultTheme.color_Primary_green)
        )
        Text(
            text = label,
            style = AppTypes.type_Caption,
            color = Color(0xFF1C1B1F),
            modifier = Modifier.align(Alignment.TopEnd)
        )
        Text(
            text = value,
            color = Color(0xFF1B4D13),
            style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

// ================= PAYMENT HISTORY (WITH DOWNLOAD) =================

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
            createPdfContent(context, outputStream, record)
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

// ================= CONVERT NUMBER TO WORDS =================

// ================= CONVERT NUMBER TO WORDS (FIXED FOR PH PESOS) =================

private fun numberToWords(amount: Double): String {
    val ones = arrayOf("", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE",
        "TEN", "ELEVEN", "TWELVE", "THIRTEEN", "FOURTEEN", "FIFTEEN", "SIXTEEN",
        "SEVENTEEN", "EIGHTEEN", "NINETEEN")
    val tens = arrayOf("", "", "TWENTY", "THIRTY", "FORTY", "FIFTY", "SIXTY", "SEVENTY", "EIGHTY", "NINETY")

    val wholeAmount = amount.toInt()
    if (wholeAmount == 0) return "ZERO PESOS ONLY"

    var result = ""

    // Handle thousands
    if (wholeAmount >= 1000) {
        val thousands = wholeAmount / 1000
        result += convertHundreds(thousands, ones, tens) + "THOUSAND "
    }

    // Handle remaining hundreds/tens/ones
    val remainder = wholeAmount % 1000
    if (remainder > 0) {
        result += convertHundreds(remainder, ones, tens)
    }

    return result.trim() + " PESOS ONLY"
}

// Helper function to correctly convert 0-999
private fun convertHundreds(num: Int, ones: Array<String>, tens: Array<String>): String {
    var res = ""
    val hundreds = num / 100
    val remainder = num % 100

    if (hundreds > 0) {
        res += "${ones[hundreds]} HUNDRED "
        if (remainder > 0) res += "AND "
    }

    if (remainder >= 20) {
        res += "${tens[remainder / 10]} "
        if (remainder % 10 > 0) res += "${ones[remainder % 10]} "
    } else if (remainder > 0) {
        res += "${ones[remainder]} "
    }
    return res
}
// ================= UPDATED PDF GENERATION WITH STAMP =================

private fun createPdfContent(context: Context, outputStream: OutputStream, record: PaymentRecord) {
    val document = Document()
    val pdfWriter = PdfWriter.getInstance(document, outputStream)
    document.open()

    // 1. Add Logo
    try {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.school_logo)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val image = PdfImage.getInstance(stream.toByteArray())
        image.scaleToFit(100f, 100f)
        image.alignment = Element.ALIGN_CENTER
        document.add(image)
        document.add(Chunk.NEWLINE)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // 2. Title
    val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
    titleFont.color = BaseColor(27, 77, 19)
    val title = Paragraph("OFFICIAL SCHOOL RECEIPT", titleFont)
    title.alignment = Element.ALIGN_CENTER
    document.add(title)
    document.add(Chunk.NEWLINE)

// 3. Top Info
    val normalFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)
    val boldFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)

// Receipt Number & Date
    document.add(Paragraph("Receipt Number: ${record.invoiceNumber}          Date: ${record.paidDate}", boldFont))
    document.add(Chunk.NEWLINE) // ✅ Added extra spacing between date and student info

// Student Info with default values
    document.add(Paragraph("Student Name: Nathaniel B. McClure", normalFont))
    document.add(Paragraph("Student ID: ____________________          Course/Level: BSIT-3", normalFont))
    document.add(Chunk.NEWLINE)

    // 4. Items Table
    val table = PdfPTable(3)
    table.widthPercentage = 100f
    table.setWidths(floatArrayOf(50f, 20f, 30f))

    val headerFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
    headerFont.color = BaseColor(27, 77, 19)

    val cell1 = PdfPCell(Phrase("Item Description", headerFont))
    cell1.horizontalAlignment = Element.ALIGN_CENTER
    table.addCell(cell1)

    val cell2 = PdfPCell(Phrase("Quantity", headerFont))
    cell2.horizontalAlignment = Element.ALIGN_CENTER
    table.addCell(cell2)

    val cell3 = PdfPCell(Phrase("Amount", headerFont))
    cell3.horizontalAlignment = Element.ALIGN_CENTER
    table.addCell(cell3)

    if (record.pdfBreakdown.isNotEmpty()) {
        val items = record.pdfBreakdown.split("\n")
        items.forEach { itemLine ->
            val parts = itemLine.split("|")
            if (parts.size == 3) {
                // Item Description (left-aligned)
                table.addCell(Phrase(parts[0], normalFont))

                // Quantity (centered)
                val qtyCell = PdfPCell(Phrase(parts[1], normalFont))
                qtyCell.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(qtyCell)

                // Amount (right-aligned for currency)
                val amountCell = PdfPCell(Phrase("PHP ${parts[2]}", normalFont))
                amountCell.horizontalAlignment = Element.ALIGN_RIGHT
                table.addCell(amountCell)
            }
        }
    }

    repeat(5) {
        table.addCell(Phrase("", normalFont))
        table.addCell(Phrase("", normalFont))
        table.addCell(Phrase("", normalFont))
    }

    val subTotalCell = PdfPCell(Phrase("Subtotal", boldFont))
    subTotalCell.colspan = 2
    subTotalCell.horizontalAlignment = Element.ALIGN_RIGHT
    subTotalCell.border = Rectangle.NO_BORDER
    table.addCell(subTotalCell)
    table.addCell(Phrase("PHP ${"%.2f".format(record.totalAmount)}", boldFont))

    val totalCell = PdfPCell(Phrase("Total Amount Paid", boldFont))
    totalCell.colspan = 2
    totalCell.horizontalAlignment = Element.ALIGN_RIGHT
    totalCell.border = Rectangle.NO_BORDER
    table.addCell(totalCell)
    table.addCell(Phrase("PHP ${"%.2f".format(record.totalAmount)}", boldFont))

    document.add(table)
    document.add(Chunk.NEWLINE)

    // 5. Amount in Words
    val amountInWords = numberToWords(record.totalAmount)
    document.add(Paragraph("Amount in Words: $amountInWords", boldFont))
    document.add(Chunk.NEWLINE)

    document.add(Paragraph("Payment Method: ${record.paymentOption}", normalFont))
    document.add(Chunk.NEWLINE)
    document.add(Chunk.NEWLINE)

    // Signature Line
    document.add(Paragraph("_________________________________", normalFont))
    document.add(Paragraph("Cashier/Authorized Representative", normalFont))


    document.close()
}

// ================= ADD SEMI-TRANSPARENT STAMP FUNCTION =================

// ================= ADD SEMI-TRANSPARENT STAMP FUNCTION =================

// ================= ADD STAMP FUNCTION (SIMPLIFIED) =================

// ================= ADD SEMI-TRANSPARENT STAMP (CORRECTED) =================

// ================= ADD STAMP FUNCTION (NO ERRORS) =================

// ================= ADD SEMI-TRANSPARENT STAMP (FINAL FIX) =================

// ================= ADD STAMP (WORKING - NO TRANSPARENCY) =================


// ================= FILTERING FUNCTION =================

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

// ================= FEECARD =================

@Composable
fun FeeCard(
    invoice: String,
    item: String,
    option: String,
    date: String,
    onDownload: () -> Unit = {}
) {
    val borderColor = Color(0xFF1B4D13)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorsDefaultTheme.color_Surface)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF1B4D13),
                    modifier = Modifier.size(24.dp)
                )
                Text(text = "PAID", style = AppTypes.type_H2, color = Color(0xFF1B4D13), fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Invoice receipt", style = AppTypes.type_Caption, color = Color.Gray)
                Text(text = invoice, style = AppTypes.type_Caption, color = Color(0xFF1B4D13))
            }
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download",
                tint = Color(0xFF1B4D13),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onDownload() }
            )
        }

        HorizontalDivider(color = Color(0xFFE0E0E0))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoColumn("Purchased item", item)
            InfoColumn("Payment option", option)
            InfoColumn("Paid date", date, Alignment.End)
        }
    }
}

@Composable
fun InfoColumn(label: String, value: String, alignment: Alignment.Horizontal = Alignment.Start) {
    Column(horizontalAlignment = alignment) {
        Text(text = label, style = AppTypes.type_Caption, color = Color.Gray)
        Text(text = value, style = AppTypes.type_Body_Small, color = Color.Black)
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BodyPreview() {
    ParentAppTheme {
        ServicesScreen()
    }
}

// ================= PAYMENT SCREEN =================

data class DueItem(val name: String, val price: Double)

@Composable
fun ContributionDuesSelectionScreen(
    onBack: () -> Unit,
    onPaymentSuccess: (List<PaymentRecord>) -> Unit,
    currentInvoiceNumber: Int
) {
    val items = listOf(
        DueItem("School uniform", 200.00),
        DueItem("P.E. uniform", 200.00)
    )

    var quantities by remember { mutableStateOf(listOf(0, 0)) }

    val totalItems = quantities.sum()
    val totalAmount = quantities.mapIndexed { i, qty -> qty * items[i].price }.sum()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1B4D13))
            }
            Text(
                text = "Contribution dues",
                style = AppTypes.type_H2,
                color = Color(0xFF1B4D13),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu", tint = Color(0xFF1B4D13))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(items) { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.3f)
                                .background(Color(0xFFFFB74D), RoundedCornerShape(16.dp))
                        )

                        Text(text = item.name, style = AppTypes.type_Body_Small, color = Color(0xFF1C1B1F))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "PHP ${"%.2f".format(item.price)}",
                                style = AppTypes.type_Caption,
                                fontWeight = FontWeight.Bold
                            )

                            Button(
                                onClick = { },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorsDefaultTheme.color_Primary_green),
                                modifier = Modifier.width(70.dp).height(28.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("View", style = AppTypes.type_M3_label_small, color = Color.White)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.9f)
                            .background(Color(0xFFF5F9F0), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (quantities[index] > 0) {
                                            val newQ = quantities.toMutableList()
                                            newQ[index] = quantities[index] - 1
                                            quantities = newQ
                                        }
                                    },
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(ColorsDefaultTheme.color_Primary_green)
                                ) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White, modifier = Modifier.size(16.dp))
                                }

                                Text(
                                    text = quantities[index].toString(),
                                    style = AppTypes.type_Body_Small,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = {
                                        val newQ = quantities.toMutableList()
                                        newQ[index] = quantities[index] + 1
                                        quantities = newQ
                                    },
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(ColorsDefaultTheme.color_Primary_green)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }

                            Text(
                                text = "${(quantities[index] * items[index].price).formatPrice()}",
                                style = AppTypes.type_Body_Small,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("No. of items", style = AppTypes.type_Body_Small, color = Color.Gray)
                Text("$totalItems pcs", style = AppTypes.type_Body_Small, fontWeight = FontWeight.Bold)
            }

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Subtotal", style = AppTypes.type_Body_Small, color = Color.Gray)
                Text("${totalAmount.formatPrice()}", style = AppTypes.type_Body_Small, fontWeight = FontWeight.Bold)
            }

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Total", style = AppTypes.type_H2, color = Color(0xFF1B4D13), fontWeight = FontWeight.Bold)
                Text("${totalAmount.formatPrice()}", style = AppTypes.type_H2, color = Color(0xFF1B4D13), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val records = mutableListOf<PaymentRecord>()
                        val currentDate = SimpleDateFormat("MM-dd-yy | h:mm a", Locale.getDefault()).format(Date())
                        val currentMonthYear = SimpleDateFormat("MMyyyy", Locale.getDefault()).format(Date())

                        val purchasedItems = mutableListOf<String>()
                        val pdfBreakdown = StringBuilder()
                        var combinedTotalAmount = 0.0

                        var hasSchoolUniform = false
                        var hasPEUniform = false

                        items.forEachIndexed { index, item ->
                            val qty = quantities[index]
                            if (qty > 0) {
                                purchasedItems.add(item.name)

                                if (item.name == "School uniform") hasSchoolUniform = true
                                if (item.name == "P.E. uniform") hasPEUniform = true

                                if (pdfBreakdown.isNotEmpty()) pdfBreakdown.append("\n")
                                pdfBreakdown.append("${item.name}|$qty|${"%.2f".format(item.price * qty)}")

                                combinedTotalAmount += item.price * qty
                            }
                        }

                        if (purchasedItems.isNotEmpty()) {
                            val invoiceNumber = "#${currentMonthYear}${String.format(Locale.getDefault(), "%02d", currentInvoiceNumber)}"

                            val combinedDescription = if (hasSchoolUniform && hasPEUniform) {
                                "School uniform"
                            } else {
                                purchasedItems.joinToString(", ")
                            }

                            records.add(
                                PaymentRecord(
                                    invoiceNumber = invoiceNumber,
                                    purchasedItem = combinedDescription,
                                    paymentOption = "G-Cash",
                                    paidDate = currentDate,
                                    totalAmount = combinedTotalAmount,
                                    pdfBreakdown = pdfBreakdown.toString()
                                )
                            )

                            onPaymentSuccess(records)
                        }
                    },
                    enabled = totalItems > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (totalItems > 0) ColorsDefaultTheme.color_Primary_green else Color.Gray,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text("Pay", style = AppTypes.type_M3_label_small)
                }
                Text(
                    text = "Add option to pay",
                    style = AppTypes.type_Caption,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

fun Double.formatPrice(): String {
    return if (this % 1 == 0.0) "${this.toInt()}.00" else String.format("%.2f", this)
}