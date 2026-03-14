package com.mis.parentapp.finance
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FinancialViewModel : ViewModel() {

    // Mock Data: This would normally come from your API/Database
    private val _fees = MutableStateFlow(
        listOf(
            FeeItem(title = "First Semester Tuition", type = FeeType.TUITION, amount = 1500.0, dueDate = "2026-04-01"),
            FeeItem(title = "Library Fee", type = FeeType.MISCELLANEOUS, amount = 50.0, dueDate = "2026-04-01"),
            FeeItem(title = "Science Lab Fee", type = FeeType.LAB_FEE, amount = 100.0, dueDate = "2026-04-01")
        )
    )
    val fees: StateFlow<List<FeeItem>> = _fees.asStateFlow()

    private val _discounts = MutableStateFlow(listOf(Discount("Academic Scholar", 0.10))) // 10% off

    private val _paymentHistory = MutableStateFlow<List<PaymentRecord>>(emptyList())
    val paymentHistory: StateFlow<List<PaymentRecord>> = _paymentHistory.asStateFlow()

    // Calculate total due after discounts
    fun getTotalDue(): Double {
        val totalFees = _fees.value.filter { !it.isPaid }.sumOf { it.amount }
        val totalDiscountMultiplier = _discounts.value.sumOf { it.percentageOff }
        val discountAmount = totalFees * totalDiscountMultiplier
        return totalFees - discountAmount
    }

    // Simulate Payment Processing
    fun processPayment(amount: Double, method: String) {
        // In a real app, this calls a Payment Gateway API (like Stripe)
        val success = true

        if (success) {
            // Log the payment
            val newPayment = PaymentRecord(amountPaid = amount, date = "Today", method = method)
            _paymentHistory.value = _paymentHistory.value + newPayment

            // Mark fees as paid (simplified logic: marks all as paid for this example)
            _fees.value = _fees.value.map { it.copy(isPaid = true) }
        }
    }
}