package com.selfgrowthfund.sgf.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.model.reports.ShareholderSummaryViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareholdersSummaryScreen(viewModel: ShareholderSummaryViewModel = hiltViewModel()) {
    val summaries by viewModel.summaries.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllSummaries()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shareholders Summary") },
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
                        Text("Shares: ${summary.shares}")
                        Text("Share Amount: ₹${format(summary.shareAmount)}")
                        Text("Share Value: ₹${format(summary.shareValue)}")
                        Text("Contribution: ${format(summary.percentContribution)}%")
                        Text("Net Profit: ₹${format(summary.netProfit)}")
                        Text("Absolute Return: ${format(summary.absoluteReturn)}%")
                        Text("Annualized Return: ${format(summary.annualizedReturn)}%")
                    }
                }
            }
        }
    }
}

private fun format(value: Double): String = String.format(Locale.US, "%,.2f", value)