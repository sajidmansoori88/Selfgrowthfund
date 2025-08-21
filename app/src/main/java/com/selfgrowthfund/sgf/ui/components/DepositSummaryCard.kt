package com.selfgrowthfund.sgf.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.dto.DepositEntrySummaryDTO

@Composable
fun DepositSummaryCard(summary: DepositEntrySummaryDTO) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Shareholder: ${summary.shareholderName}",
                style = MaterialTheme.typography.titleMedium
            )
            Text("Due Month: ${summary.dueMonth}", style = MaterialTheme.typography.bodyMedium)
            Text("Payment Date: ${summary.paymentDate}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Total Amount: ₹${summary.totalAmount}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text("Payment Status: ${summary.paymentStatus}", style = MaterialTheme.typography.bodyMedium)
            Text("Mode: ${summary.modeOfPayment}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}