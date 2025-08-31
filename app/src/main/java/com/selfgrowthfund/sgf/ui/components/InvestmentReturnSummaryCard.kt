package com.selfgrowthfund.sgf.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
import java.text.DecimalFormat

@Composable
fun InvestmentReturnSummaryCard(
    returns: List<InvestmentReturns>,
    modifier: Modifier = Modifier
) {
    val totalReturned = returns.sumOf { it.amountReceived }
    val totalProfit = returns.sumOf { it.actualProfitAmount }
    val totalVariance = returns.sumOf { it.profitAmountVariance }
    val avgReturnPeriod = returns.map { it.actualReturnPeriod }.average().toInt()

    Card(modifier = modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Return Summary", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Total Returned: ₹${formatAmount(totalReturned)}", style = MaterialTheme.typography.bodyMedium)
            Text("Total Profit: ₹${formatAmount(totalProfit)}", style = MaterialTheme.typography.bodyMedium)
            Text("Profit Variance: ₹${formatAmount(totalVariance)}", style = MaterialTheme.typography.bodyMedium)
            Text("Avg Return Period: $avgReturnPeriod days", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun formatAmount(value: Double): String {
    return DecimalFormat("#,##0.00").format(value)
}