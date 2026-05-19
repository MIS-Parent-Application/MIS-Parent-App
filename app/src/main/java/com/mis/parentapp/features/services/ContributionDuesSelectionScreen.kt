package com.mis.parentapp.features.services

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import com.mis.parentapp.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mis.parentapp.ui.theme.AppTypes
import com.mis.parentapp.ui.theme.ColorsDefaultTheme
import java.text.SimpleDateFormat
import java.util.*


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

    var selectedPayment by remember { mutableStateOf("Cash") }

    var showQrDialog by remember { mutableStateOf(false) }

    var showCashDialog by remember { mutableStateOf(false) }

    var showCardDialog by remember { mutableStateOf(false) }

    var showBankDialog by remember { mutableStateOf(false) }

    val totalItems = quantities.sum()

    val totalAmount = quantities
        .mapIndexed { i, qty -> qty * items[i].price }
        .sum()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onBack) {

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1B4D13)
                )
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

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = Color(0xFF1B4D13)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ITEMS
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {

            itemsIndexed(items) { index, item ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ITEM INFO
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.3f)
                                .background(
                                    Color(0xFFFFB74D),
                                    RoundedCornerShape(16.dp)
                                )
                        )

                        Text(
                            text = item.name,
                            style = AppTypes.type_Body_Small,
                            color = Color(0xFF1C1B1F)
                        )

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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorsDefaultTheme.color_Primary_green
                                ),
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(28.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 4.dp,
                                    vertical = 0.dp
                                )
                            ) {

                                Text(
                                    "View",
                                    style = AppTypes.type_M3_label_small,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // QUANTITY CARD
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.9f)
                            .background(
                                Color(0xFFF5F9F0),
                                RoundedCornerShape(16.dp)
                            ),
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

                                            val newQ =
                                                quantities.toMutableList()

                                            newQ[index] =
                                                quantities[index] - 1

                                            quantities = newQ
                                        }
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            ColorsDefaultTheme.color_Primary_green
                                        )
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = quantities[index].toString(),
                                    style = AppTypes.type_Body_Small,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = {

                                        val newQ =
                                            quantities.toMutableList()

                                        newQ[index] =
                                            quantities[index] + 1

                                        quantities = newQ
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            ColorsDefaultTheme.color_Primary_green
                                        )
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Text(
                                text = (
                                        quantities[index] * items[index].price
                                        ).formatPrice(),
                                style = AppTypes.type_Body_Small,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {

                Spacer(modifier = Modifier.height(8.dp))

                // TOTALS
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Row(
                        Modifier.fillMaxWidth(),
                        Arrangement.SpaceBetween
                    ) {

                        Text(
                            "No. of items",
                            style = AppTypes.type_Body_Small,
                            color = Color.Gray
                        )

                        Text(
                            "$totalItems pcs",
                            style = AppTypes.type_Body_Small,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        Arrangement.SpaceBetween
                    ) {

                        Text(
                            "Subtotal",
                            style = AppTypes.type_Body_Small,
                            color = Color.Gray
                        )

                        Text(
                            totalAmount.formatPrice(),
                            style = AppTypes.type_Body_Small,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        Arrangement.SpaceBetween
                    ) {

                        Text(
                            "Total",
                            style = AppTypes.type_H2,
                            color = Color(0xFF1B4D13),
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            totalAmount.formatPrice(),
                            style = AppTypes.type_H2,
                            color = Color(0xFF1B4D13),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // PAYMENT METHODS
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "Mode of Payment",
                        style = AppTypes.type_Body_Small,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B4D13)
                    )

                    PaymentMethodCard(
                        title = "Cash",
                        subtitle = "Pay directly to cashier",
                        icon = Icons.Default.Payments,
                        selected = selectedPayment == "Cash",
                        onClick = {
                            selectedPayment = "Cash"
                        }
                    )

                    PaymentMethodCard(
                        title = "GCash",
                        subtitle = "Scan QR to pay",
                        icon = Icons.Default.QrCode,
                        selected = selectedPayment == "GCash",
                        onClick = {
                            selectedPayment = "GCash"
                        }
                    )

                    PaymentMethodCard(
                        title = "Card",
                        subtitle = "Debit / Credit Card",
                        icon = Icons.Default.CreditCard,
                        selected = selectedPayment == "Card",
                        onClick = {
                            selectedPayment = "Card"
                        }
                    )

                    PaymentMethodCard(
                        title = "Bank Transfer",
                        subtitle = "Transfer using bank app",
                        icon = Icons.Default.AccountBalance,
                        selected = selectedPayment == "Bank Transfer",
                        onClick = {
                            selectedPayment = "Bank Transfer"
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // PAY BUTTON
                Button(
                    onClick = {

                        when (selectedPayment) {

                            "Cash" -> {

                                showCashDialog = true
                            }

                            "GCash" -> {

                                showQrDialog = true
                            }

                            "Card" -> {

                                showCardDialog = true
                            }

                            "Bank Transfer" -> {

                                showBankDialog = true
                            }
                        }
                    },
                    enabled = totalItems > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if (totalItems > 0)
                                ColorsDefaultTheme.color_Primary_green
                            else Color.Gray,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {

                    Text(
                        text = "Pay with $selectedPayment",
                        style = AppTypes.type_M3_label_small
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // CASH DIALOG
    if (showCashDialog) {

        AlertDialog(
            onDismissRequest = {
                showCashDialog = false
            },

            confirmButton = {

                Button(
                    onClick = {

                        showCashDialog = false

                        completePayment(
                            items = items,
                            quantities = quantities,
                            selectedPayment = selectedPayment,
                            currentInvoiceNumber = currentInvoiceNumber,
                            onPaymentSuccess = onPaymentSuccess
                        )
                    }
                ) {

                    Text("Confirm")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showCashDialog = false
                    }
                ) {

                    Text("Cancel")
                }
            },

            title = {
                Text("Cash Payment")
            },

            text = {

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "Proceed with cash payment?"
                    )

                    Text(
                        text = "Please pay directly to the cashier."
                    )

                    Text(
                        text = "Total: ${totalAmount.formatPrice()}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // QR DIALOG
    if (showQrDialog) {

        AlertDialog(
            onDismissRequest = {
                showQrDialog = false
            },

            confirmButton = {

                Button(
                    onClick = {

                        showQrDialog = false

                        completePayment(
                            items = items,
                            quantities = quantities,
                            selectedPayment = selectedPayment,
                            currentInvoiceNumber = currentInvoiceNumber,
                            onPaymentSuccess = onPaymentSuccess
                        )
                    }
                ) {

                    Text("Payment Completed")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showQrDialog = false
                    }
                ) {

                    Text("Cancel")
                }
            },

            title = {
                Text("Scan QR using GCash")
            },

            text = {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(
                                Color.LightGray,
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "QR",
                            modifier = Modifier.size(120.dp),
                            tint = Color.Black
                        )
                    }

                    Text(
                        text = "Use your GCash app to scan this QR code.",
                        style = AppTypes.type_Caption,
                        color = Color.Gray
                    )

                    Text(
                        text = "Total: ${totalAmount.formatPrice()}",
                        style = AppTypes.type_Body_Small,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // CARD DIALOG
    if (showCardDialog) {

        AlertDialog(
            onDismissRequest = {
                showCardDialog = false
            },

            confirmButton = {

                Button(
                    onClick = {

                        showCardDialog = false

                        completePayment(
                            items = items,
                            quantities = quantities,
                            selectedPayment = selectedPayment,
                            currentInvoiceNumber = currentInvoiceNumber,
                            onPaymentSuccess = onPaymentSuccess
                        )
                    }
                ) {

                    Text("Confirm")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showCardDialog = false
                    }
                ) {

                    Text("Cancel")
                }
            },

            title = {
                Text("Card Payment")
            },

            text = {

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "Proceed with card payment?"
                    )

                    Text(
                        text = "Debit/Credit card will be processed."
                    )

                    Text(
                        text = "Total: ${totalAmount.formatPrice()}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // BANK TRANSFER DIALOG
    if (showBankDialog) {

        AlertDialog(
            onDismissRequest = {
                showBankDialog = false
            },

            confirmButton = {

                Button(
                    onClick = {

                        showBankDialog = false

                        completePayment(
                            items = items,
                            quantities = quantities,
                            selectedPayment = selectedPayment,
                            currentInvoiceNumber = currentInvoiceNumber,
                            onPaymentSuccess = onPaymentSuccess
                        )
                    }
                ) {

                    Text("Confirm")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showBankDialog = false
                    }
                ) {

                    Text("Cancel")
                }
            },

            title = {
                Text("Bank Transfer")
            },

            text = {

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "Proceed with bank transfer?"
                    )

                    Text(
                        text = "Transfer payment using your banking app."
                    )

                    Text(
                        text = "Total: ${totalAmount.formatPrice()}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
fun PaymentMethodCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected)
                    Color(0xFFE8F5E9)
                else
                    Color(0xFFF5F9F0)
            )
            .clickable {
                onClick()
            }
            .padding(16.dp),

        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1B4D13)),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {

                Text(
                    text = title,
                    style = AppTypes.type_Body_Small,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = subtitle,
                    style = AppTypes.type_Caption,
                    color = Color.Gray
                )
            }
        }

        RadioButton(
            selected = selected,
            onClick = onClick
        )
    }
}

fun completePayment(
    items: List<DueItem>,
    quantities: List<Int>,
    selectedPayment: String,
    currentInvoiceNumber: Int,
    onPaymentSuccess: (List<PaymentRecord>) -> Unit
) {
    val records = mutableListOf<PaymentRecord>()
    val currentDate =
        SimpleDateFormat(
            "MM-dd-yy | h:mm a",
            Locale.getDefault()
        ).format(Date())
    val currentMonthYear =
        SimpleDateFormat(
            "MMyyyy",
            Locale.getDefault()
        ).format(Date())

    val purchasedItems = mutableListOf<String>()
    val pdfBreakdown = StringBuilder()
    var combinedTotalAmount = 0.0

    items.forEachIndexed { index, item ->
        val qty = quantities[index]

        if (qty > 0) {

            purchasedItems.add(item.name)

            if (pdfBreakdown.isNotEmpty()) {

                pdfBreakdown.append("\n")
            }

            pdfBreakdown.append(
                "${item.name}|$qty|${
                    "%.2f".format(item.price * qty)
                }"
            )

            combinedTotalAmount += item.price * qty
        }
    }

    if (purchasedItems.isNotEmpty()) {

        val invoiceNumber =
            "#${currentMonthYear}${
                String.format("%02d", currentInvoiceNumber)
            }"

        records.add(
            PaymentRecord(
                invoiceNumber = invoiceNumber,
                purchasedItem = purchasedItems.joinToString(", "),
                paymentOption = selectedPayment,
                paidDate = currentDate,
                totalAmount = combinedTotalAmount,
                pdfBreakdown = pdfBreakdown.toString()
            )
        )
        onPaymentSuccess(records)
    }
}

// PRICE FORMATTER
fun Double.formatPrice(): String {
    return if (this % 1 == 0.0) {
        "${this.toInt()}.00"

    } else {
        String.format("%.2f", this)
    }
}
