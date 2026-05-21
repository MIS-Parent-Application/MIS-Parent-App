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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LiveHelp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mis.parentapp.R
import com.mis.parentapp.network.Child
import com.mis.parentapp.network.CreatePaymentRequest
import com.mis.parentapp.network.RetrofitInstance
import com.mis.parentapp.features.services.sections.SearchBarSection
import com.mis.parentapp.navigation.Documents
import com.mis.parentapp.navigation.FAQs
import com.mis.parentapp.navigation.FormsAndRequest
import com.mis.parentapp.navigation.PaymentOptions
import com.mis.parentapp.shared.StudentSharedViewModel
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ParentAppTheme
import com.mis.parentapp.utilities.modals.ServiceAccountSwitchModal
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

// ==========================================
// NAVIGATION ROUTES DEFINITION
// ==========================================
sealed class Screen(val route: String) {
    object Services : Screen("services")
    object Forms : Screen("forms")
    object Payments : Screen("payments")
    object Documents : Screen("documents")
    object FAQs : Screen("faqs")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Services.route
    ) {
        composable(Screen.Services.route) {
            ServicesScreen(navController = navController)
        }
        composable(Screen.Forms.route) {
            // Your separate file: FormsAndRequestScreen(navController)
            Text("Forms Screen Placeholder")
        }
        composable(Screen.Payments.route) {
            // Your separate file: PaymentOptionsScreen(navController)
            Text("Payments Screen Placeholder")
        }
        composable(Screen.Documents.route) {
            // Your separate file: DocumentsScreen(navController)
            Text("Documents Screen Placeholder")
        }
        composable(Screen.FAQs.route) {
            // Your separate file: FAQsScreen(navController)
            Text("FAQs Screen Placeholder")
        }
    }
}

// ================= SERVICES SCREEN =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    navController: NavController, // Integrated NavController
    studentVM: StudentSharedViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val showPaymentScreen = remember { mutableStateOf(false) }
    val paymentHistory = remember { mutableStateOf(listOf<PaymentRecord>()) }
    val invoiceCounter = remember { mutableIntStateOf(1) }

    val selectedStudent = studentVM.selectedStudent
    val otherStudents = studentVM.students.filter { it.id != selectedStudent?.id }

    LaunchedEffect(selectedStudent?.id) {
        val studentId = selectedStudent?.id ?: return@LaunchedEffect
        runCatching {
            RetrofitInstance.api.getStudentPayments(studentId).map {
                PaymentRecord(
                    invoiceNumber = it.invoiceNumber,
                    purchasedItem = it.purchasedItem,
                    paymentOption = it.paymentOption,
                    paidDate = it.paidDate,
                    totalAmount = it.totalAmount,
                    pdfBreakdown = it.pdfBreakdown
                )
            }
        }.onSuccess {
            paymentHistory.value = it
            invoiceCounter.intValue = it.size + 1
        }
    }

    // Separate sheet states to avoid conflict properties
    val accountSheetState = rememberModalBottomSheetState()
    val menuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showAccountModal by remember { mutableStateOf(false) }
    var showMenuBottomSheet by remember { mutableStateOf(false) }

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
                val studentId = selectedStudent?.id
                if (studentId == null) {
                    paymentHistory.value += records
                    invoiceCounter.intValue += records.size
                } else {
                    scope.launch {
                        val savedRecords = records.map { record ->
                            runCatching {
                                RetrofitInstance.api.createStudentPayment(
                                    studentId,
                                    CreatePaymentRequest(
                                        invoiceNumber = record.invoiceNumber,
                                        purchasedItem = record.purchasedItem,
                                        paymentOption = record.paymentOption,
                                        paidDate = record.paidDate,
                                        totalAmount = record.totalAmount,
                                        pdfBreakdown = record.pdfBreakdown
                                    )
                                ).let {
                                    PaymentRecord(
                                        invoiceNumber = it.invoiceNumber,
                                        purchasedItem = it.purchasedItem,
                                        paymentOption = it.paymentOption,
                                        paidDate = it.paidDate,
                                        totalAmount = it.totalAmount,
                                        pdfBreakdown = it.pdfBreakdown
                                    )
                                }
                            }.getOrElse { record }
                        }
                        paymentHistory.value = savedRecords + paymentHistory.value
                        invoiceCounter.intValue += savedRecords.size
                    }
                }
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
                onMenuClick = { showMenuBottomSheet = true }, // Wired up burger menu action
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

            // ACCOUNT SWITCH SHEET
            if (showAccountModal) {
                ModalBottomSheet(
                    onDismissRequest = { showAccountModal = false },
                    sheetState = accountSheetState,
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

            // NAVIGATION BURGER MENU SHEET
            if (showMenuBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showMenuBottomSheet = false },
                    sheetState = menuSheetState,
                    containerColor = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    dragHandle = {
                        BottomSheetDefaults.DragHandle(color = Color.Gray.copy(alpha = 0.5f))
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        // Safe navigation handler that closes the sheet first
                        val navigateTo: (Any) -> Unit = { route ->
                            scope.launch { menuSheetState.hide() }.invokeOnCompletion {
                                if (!menuSheetState.isVisible) {
                                    showMenuBottomSheet = false
                                    navController.navigate(route)
                                }
                            }
                        }

                        MenuItem(
                            icon = Icons.Default.Article,
                            title = "Forms and request",
                            subtitle = "Be updated to your student attendance.",
                            onClick = { navigateTo(FormsAndRequest) }
                        )

                        MenuItem(
                            icon = Icons.Default.Payment,
                            title = "Payment options",
                            subtitle = "Be updated to your student attendance.",
                            onClick = { navigateTo(PaymentOptions) }
                        )

                        MenuItem(
                            icon = Icons.Default.Description,
                            title = "Documents",
                            subtitle = "Be updated to your student attendance.",
                            onClick = { navigateTo(Documents) }
                        )

                        MenuItem(
                            icon = Icons.Default.LiveHelp,
                            title = "FAQs",
                            subtitle = "Be updated to your student attendance.",
                            onClick = { navigateTo(FAQs) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
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
    onMenuClick: () -> Unit,
    onQrClick: () -> Unit,
    paymentHistory: List<PaymentRecord>
) {

    // GET STUDENTS FROM VIEWMODEL
    val students = studentVM.students

    // DYNAMIC SELECTED STUDENT
    val selectedStudent = studentVM.selectedStudent

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxSize()
        ) {

            item {

                Spacer(modifier = Modifier.height(4.dp))

                SearchBarSection(
                    selectedStudent = selectedStudent,
                    onProfileClick = {

                        if (students.isNotEmpty()) {

                            val currentIndex =
                                students.indexOf(selectedStudent)

                            val nextIndex =
                                if (currentIndex == students.lastIndex) {
                                    0
                                } else {
                                    currentIndex + 1
                                }

                            val nextStudent = students[nextIndex]
                            studentVM.selectStudent(nextStudent)
                        }

                        onProfileClick()
                    },

                    onQrClick = {
                        onQrClick()
                    }
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

            item {

                ContributionDuesSection(
                    onPayClick = onPayClick
                )
            }

            item {

                PaymentHistorySection(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    paymentHistory = paymentHistory
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// ================= REUSABLE MENU ITEM =================

@Composable
fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit // onClick listener setup added
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8EDD8)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF1F1F1F),
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) { // Handles dynamic text wrapping safely
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color(0xFF5D8A5A),
                lineHeight = 18.sp
            )
        }
    }
}

// ================= CONTRIBUTION DUES SECTION =================

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

// ================= PAYMENT HISTORY SECTION =================

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
        val mockNavController = rememberNavController()
        ServicesScreen(navController = mockNavController)
    }
}
