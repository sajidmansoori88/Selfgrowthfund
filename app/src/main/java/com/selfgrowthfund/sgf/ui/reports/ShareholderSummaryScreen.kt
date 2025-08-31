package com.selfgrowthfund.sgf.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.model.reports.ShareholderSummaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareholderSummaryScreen(viewModel: ShareholderSummaryViewModel) {
    val summaries by viewModel.summary.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Shareholder Summary") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(summaries) { summary ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = summary.name, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Shares: ${summary.shares}")
                        Text(text = "Share Amount: ₹${format(summary.shareAmount)}")
                        Text(text = "Share Value: ₹${format(summary.shareValue)}")
                        Text(text = "Contribution: ${format(summary.percentContribution)}%")
                        Text(text = "Net Profit: ₹${format(summary.netProfit)}")
                        Text(text = "Absolute Return: ${format(summary.absoluteReturn)}%")
                        Text(text = "Annualized Return: ${format(summary.annualizedReturn)}%")
                    }
                }
            }
        }
    }
}

private fun format(value: Double): String = String.format("%,.2f", value)