package com.selfgrowthfund.sgf.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.model.reports.InvestmentTrackerViewModel

@Composable
fun InvestmentTrackerReportScreen(viewModel: InvestmentTrackerViewModel = hiltViewModel()) {
    val investments by viewModel.investments.collectAsState()
    val selectedYear by viewModel.selectedFiscalYear.collectAsState()
    val fiscalYears = remember { viewModel.getFiscalYears() }

    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Investments Tracker", style = MaterialTheme.typography.titleLarge)

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
            items(investments) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.investmentId, modifier = Modifier.weight(1f))
                    Text(item.investmentName, modifier = Modifier.weight(1f))
                    Text(item.investorName, modifier = Modifier.weight(1f))
                    Text(item.expectedReturnDate.toString(), modifier = Modifier.weight(1f))
                    Text(item.actualReturnDate?.toString() ?: "—", modifier = Modifier.weight(1f))
                    Text(item.status, modifier = Modifier.weight(1f))
                    Text("${item.expectedProfitPercent}%", modifier = Modifier.weight(1f))
                    Text("${item.actualProfitPercent}%", modifier = Modifier.weight(1f))
                }
                HorizontalDivider()
            }
        }
    }
}