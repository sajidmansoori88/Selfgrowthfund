package com.selfgrowthfund.sgf.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.model.reports.BorrowingSummaryViewModel

@Composable
fun ClosedBorrowingReportScreen(
    modifier: Modifier = Modifier,
    viewModel: BorrowingSummaryViewModel = hiltViewModel()

) {
    val borrowings by viewModel.closedBorrowings.collectAsState()
    val selectedYear by viewModel.selectedFiscalYear.collectAsState()
    val fiscalYears = remember { viewModel.getFiscalYears() }

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()) // ✅ Make it scrollable
            .padding(16.dp)
    ) {
        Text("Closed Borrowings", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Year selector
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = selectedYear ?: "Select Fiscal Year")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
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

        if (borrowings.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No closed borrowings found for selected year",
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
                Text("Penalty", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("Total Paid", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("Days", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

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

        // ✅ Add bottom spacer to ensure content isn't cut off
        Spacer(modifier = Modifier.height(32.dp))
    }
}