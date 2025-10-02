package com.selfgrowthfund.sgf.ui.investments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.Investment

@Composable
fun InvestmentListScreen(
    investments: List<Investment>,
    onSelectInvestment: (Investment) -> Unit,
    onApplyInvestment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onApplyInvestment, modifier = Modifier.fillMaxWidth()) {
            Text("Apply New Investment")
        }

        Spacer(Modifier.height(16.dp))

        if (investments.isEmpty()) {
            Text("No investments yet.")
        } else {
            LazyColumn {
                items(investments) { inv ->
                    Card(
                        onClick = { onSelectInvestment(inv) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
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
