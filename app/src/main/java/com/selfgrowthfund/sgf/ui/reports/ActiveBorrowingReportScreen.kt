package com.selfgrowthfund.sgf.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.model.reports.BorrowingSummaryViewModel

@Composable
fun ActiveBorrowingReportScreen(
    viewModel: BorrowingSummaryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier // ✅ Add modifier parameter
) {
    val borrowings by viewModel.activeBorrowings.collectAsState()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()) // ✅ Make it scrollable
            .padding(16.dp)
    ) {
        Text(
            "Active / Outstanding Borrowings",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (borrowings.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No active borrowings found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Name", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("ID", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("Amount", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("Repaid", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("Penalty", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("Outstanding", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("Due", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("Days", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn {
                items(borrowings) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.shareholderName, modifier = Modifier.weight(1f))
                        Text(item.borrowId, modifier = Modifier.weight(1f))
                        Text("₹${item.borrowAmount}", modifier = Modifier.weight(1f))
                        Text("₹${item.principalRepaid}", modifier = Modifier.weight(1f))
                        Text("₹${item.penaltyPaid}", modifier = Modifier.weight(1f))
                        Text("₹${item.outstanding}", modifier = Modifier.weight(1f))
                        Text("₹${item.penaltyDue}", modifier = Modifier.weight(1f))
                        Text("${item.overdueDays} days", modifier = Modifier.weight(1f))
                    }
                    Divider()
                }
            }
        }

        // ✅ Add bottom spacer to ensure content isn't cut off
        Spacer(modifier = Modifier.height(32.dp))
    }
}