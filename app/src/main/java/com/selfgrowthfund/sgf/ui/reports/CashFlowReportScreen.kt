package com.selfgrowthfund.sgf.ui.reports

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.model.reports.CashFlowReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashFlowReportScreen(viewModel: CashFlowReportViewModel) {
    val cashFlow by viewModel.cashFlow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cash Flow Report") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(cashFlow) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (entry.income - entry.expenses >= 0)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier
                            .padding(16.dp)
                            .animateContentSize()
                        ) {
                            Text(entry.month, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Opening Balance: ₹${entry.openingBalance.format()}")
                            Text("Income: ₹${entry.income.format()}")
                            Text("Expenses: ₹${entry.expenses.format()}")
                            Text(
                                "Net Cash Flow: ₹${(entry.income - entry.expenses).format()}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (entry.income - entry.expenses >= 0)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                            Text("Closing Balance: ₹${entry.closingBalance.format()}")
                        }
                    }
                }
            }
        }
    }
}

fun Double.format(): String = "%,.2f".format(this)