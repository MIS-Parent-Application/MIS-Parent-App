package com.mis.parentapp.features.services

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                                text = (quantities[index] * items[index].price).formatPrice(),
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
                Text(totalAmount.formatPrice(), style = AppTypes.type_Body_Small, fontWeight = FontWeight.Bold)
            }

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Total", style = AppTypes.type_H2, color = Color(0xFF1B4D13), fontWeight = FontWeight.Bold)
                Text(totalAmount.formatPrice(), style = AppTypes.type_H2, color = Color(0xFF1B4D13), fontWeight = FontWeight.Bold)
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
                            val invoiceNumber = "#${currentMonthYear}${String.format("%02d", currentInvoiceNumber)}"

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

// Helper extension function for price formatting
fun Double.formatPrice(): String {
    return if (this % 1 == 0.0) "${this.toInt()}.00" else String.format("%.2f", this)
}
