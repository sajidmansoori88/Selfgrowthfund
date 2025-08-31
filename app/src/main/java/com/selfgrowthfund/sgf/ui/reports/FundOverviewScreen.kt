package com.selfgrowthfund.sgf.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FundOverviewScreen(viewModel: FundOverviewViewModel) {
    val metrics by viewModel.metrics.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Fund Overview", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader("Incomes")
        MetricGrid {
            MetricCard("Total Share Amount", value = metrics.totalShareAmount)
            MetricCard("Penalties from Share Deposits", value = metrics.penaltiesFromShareDeposits)
            MetricCard("Additional Contributions", value = metrics.additionalContributions)
            MetricCard("Penalties from Borrowings", value = metrics.penaltiesFromBorrowings)
            MetricCard("Penalties from Investments", value = metrics.penaltiesFromInvestments)
            MetricCard("Other Incomes", value = metrics.totalOtherIncomes)
            MetricCard("Total Earnings", value = metrics.totalEarnings, bold = true)
        }

        SectionHeader("Investments")
        MetricGrid {
            MetricCard("Total Investments Made", value = metrics.totalInvestments)
            MetricCard("Active Investments", count = metrics.activeInvestments)
            MetricCard("Closed Investments", count = metrics.closedInvestments)
            MetricCard("Overdue Investments", count = metrics.overdueInvestments)
            MetricCard("Returns from Closed", value = metrics.returnsFromClosedInvestments)
            MetricCard("Written-Off Investments", value = metrics.writtenOffInvestments)
            MetricCard("Investment Profit (%)", value = metrics.investmentProfitPercent, isPercentage = true)
            MetricCard("Investment Profit (₹)", value = metrics.investmentProfitAmount)
        }

        SectionHeader("Borrowings & Expenses")
        MetricGrid {
            MetricCard("Total Borrow Issued", value = metrics.totalBorrowIssued)
            MetricCard("Active Borrowings", count = metrics.activeBorrowings)
            MetricCard("Closed Borrowings", count = metrics.closedBorrowings)
            MetricCard("Repayments Received", value = metrics.repaymentsReceived)
            MetricCard("Outstanding Borrowings", value = metrics.outstandingBorrowings)
            MetricCard("Overdue Borrowings", count = metrics.overdueBorrowings)
            MetricCard("Other Expenses", value = metrics.otherExpenses)
            MetricCard("Net Profit/Loss", value = metrics.netProfitOrLoss, highlight = true)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun MetricGrid(content: @Composable RowScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ✅ RowScope extension so weight() works safely
@Composable
fun RowScope.MetricCard(
    label: String,
    count: Int? = null,
    value: Double? = null,
    isPercentage: Boolean = false,
    bold: Boolean = false,
    highlight: Boolean = false
) {
    val formattedValue = value?.let {
        if (isPercentage) "${"%.2f".format(it)}%" else "₹${"%.2f".format(it)}"
    }

    val valueColor = when {
        highlight && (value ?: 0.0) < 0 -> MaterialTheme.colorScheme.error
        highlight -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(modifier = Modifier.weight(1f)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            if (count != null) {
                Text("Nos.: $count", style = MaterialTheme.typography.labelSmall)
            }
            if (formattedValue != null) {
                Text(
                    formattedValue,
                    style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                    color = valueColor
                )
            }
        }
    }
}