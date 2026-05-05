package com.mis.parentapp.features.services

data class PaymentRecord(
    val invoiceNumber: String,
    val purchasedItem: String,
    val paymentOption: String,
    val paidDate: String,
    val totalAmount: Double,
    val pdfBreakdown: String = ""
)

data class DueItem(
    val name: String,
    val price: Double
)