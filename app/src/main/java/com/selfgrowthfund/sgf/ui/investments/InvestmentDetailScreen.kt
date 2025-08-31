package com.selfgrowthfund.sgf.ui.investments

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.ui.components.InvestmentCard
import com.selfgrowthfund.sgf.ui.components.InvestmentReturnSummaryCard

@Composable
fun InvestmentDetailScreen(
    investment: Investment,
    returns: List<InvestmentReturns>,
    currentUserRole: MemberRole,
    onAddReturn: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        InvestmentCard(
            investment = investment,
            currentUserRole = currentUserRole,
            onAddReturn = onAddReturn
        )

        Spacer(modifier = Modifier.height(16.dp))

        InvestmentReturnSummaryCard(returns = returns)

        // Optional: Show past returns
        if (returns.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Past Returns", style = MaterialTheme.typography.titleSmall)
            returns.forEach { ret ->
                Text("• ₹${ret.amountReceived} on ${ret.returnDate}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}