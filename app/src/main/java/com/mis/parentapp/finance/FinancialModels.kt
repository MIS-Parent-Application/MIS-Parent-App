package com.mis.parentapp.finance

import java.util.UUID

// Defines the type of fee
enum class FeeType { TUITION, MISCELLANEOUS, LAB_FEE }

// Represents a single fee item
data class FeeItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: FeeType,
    val amount: Double,
    val dueDate: String,
    var isPaid: Boolean = false
)

// Represents a discount applied to the student's account
data class Discount(
    val title: String,
    val percentageOff: Double // e.g., 0.10 for 10%
)

// Represents a successful payment record
data class PaymentRecord(
    val receiptId: String = UUID.randomUUID().toString(),
    val amountPaid: Double,
    val date: String,
    val method: String // e.g., "Credit Card", "Bank Transfer"
)