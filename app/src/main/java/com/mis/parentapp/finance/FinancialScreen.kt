package com.mis.parentapp.finance
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FinancialDashboardScreen(viewModel: FinancialViewModel = viewModel()) {
    val fees by viewModel.fees.collectAsState()
    val totalDue = viewModel.getTotalDue()

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Financial Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Balance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Balance Due", fontSize = 16.sp)
                Text("$${String.format(java.util.Locale.US, "%.2f", totalDue)}", fontSize = 32.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.processPayment(totalDue, "Credit Card") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = totalDue > 0.0
                ) {
                    Text(if (totalDue > 0.0) "Pay Now" else "All Settled")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Fee Breakdown", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        // List of Fees
        LazyColumn {
            items(fees) { fee ->
                FeeItemRow(fee)
            }
        }
    }
}

@Composable
fun FeeItemRow(fee: FeeItem) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(fee.title, fontWeight = FontWeight.Bold)
                Text("Due: ${fee.dueDate}", fontSize = 12.sp)
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text("$${fee.amount}", fontWeight = FontWeight.Bold)
                Text(
                    text = if (fee.isPaid) "Paid" else "Pending",
                    color = if (fee.isPaid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
        }
    }
}