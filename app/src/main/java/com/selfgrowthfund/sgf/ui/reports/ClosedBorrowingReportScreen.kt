package com.selfgrowthfund.sgf.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.model.reports.BorrowingSummaryViewModel

@Composable
fun ClosedBorrowingReportScreen(viewModel: BorrowingSummaryViewModel = hiltViewModel()) {
    val borrowings by viewModel.closedBorrowings.collectAsState()
    val selectedYear by viewModel.selectedFiscalYear.collectAsState()
    val fiscalYears = remember { viewModel.getFiscalYears() }

    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Closed Borrowings", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(text = selectedYear ?: "Select Fiscal Year")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                fiscalYears.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year) },
                        onClick = {
                            viewModel.setFiscalYear(year)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    Text("₹${item.penaltyPaid}", modifier = Modifier.weight(1f))
                    Text("₹${item.totalPaid}", modifier = Modifier.weight(1f))
                    Text("${item.returnPeriodDays} days", modifier = Modifier.weight(1f))
                }
                HorizontalDivider()
            }
        }
    }
}