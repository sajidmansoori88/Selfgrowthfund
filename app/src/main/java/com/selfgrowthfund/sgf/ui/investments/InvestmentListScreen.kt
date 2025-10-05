package com.selfgrowthfund.sgf.ui.investments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.ui.components.EmptyStateCard

@Composable
fun InvestmentListScreen(
    investments: List<Investment>,
    onSelectInvestment: (Investment) -> Unit,
    onApplyInvestment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ✅ Primary Action Button
        Button(
            onClick = onApplyInvestment,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apply New Investment")
        }

        Spacer(Modifier.height(16.dp))

        if (investments.isEmpty()) {
            // ✅ Empty State
            EmptyStateCard(
                icon = Icons.Default.Info,
                title = "No investments yet",
                message = "You have not applied for any investments. Tap \"Apply New Investment\" to start.",
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // ✅ Investment List
            LazyColumn {
                items(investments) { inv ->
                    Card(
                        onClick = { onSelectInvestment(inv) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(inv.investmentName, style = MaterialTheme.typography.titleMedium)
                            Text("Amount: ₹${inv.amount}")
                            Text("Status: ${inv.approvalStatus}")
                        }
                    }
                }
            }
        }
    }
}
