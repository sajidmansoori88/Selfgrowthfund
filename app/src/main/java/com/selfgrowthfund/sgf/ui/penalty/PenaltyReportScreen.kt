package com.selfgrowthfund.sgf.ui.penalty

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.model.User
import com.selfgrowthfund.sgf.model.enums.PenaltyType
import com.selfgrowthfund.sgf.ui.components.DropdownMenuBox
import com.selfgrowthfund.sgf.ui.components.reportingperiod.ReportPeriod
import com.selfgrowthfund.sgf.ui.components.reportingperiod.CustomPeriod
import com.selfgrowthfund.sgf.ui.components.reportingperiod.CustomPeriodPickerDialog

@Composable
fun PenaltyReportScreen(
    user: User
) {
    val viewModel: PenaltyReportViewModel = hiltViewModel()
    val penalties by viewModel.penalties.collectAsState()

    var selectedType by remember { mutableStateOf(PenaltyType.OTHER) }
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.CURRENT_MONTH) }
    var customPeriod by remember { mutableStateOf<CustomPeriod?>(null) }
    var showCustomDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Penalty Report", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(8.dp))

        // Report Period Dropdown
        DropdownMenuBox(
            label = "Report Period",
            options = ReportPeriod.entries.toList(),
            selected = selectedPeriod,
            onSelected = { period ->
                selectedPeriod = period
                if (period == ReportPeriod.CUSTOM) {
                    showCustomDialog = true
                } else {
                    customPeriod = null
                    viewModel.loadByPeriod(period)
                }
            }
        )

        // When custom is selected, show its full label
        customPeriod?.let {
            Text(
                text = it.label,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Penalty Type Dropdown
        DropdownMenuBox(
            label = "Filter by Type",
            options = PenaltyType.entries.toList(),
            selected = selectedType,
            onSelected = { type ->
                selectedType = type
                viewModel.filterByType(type)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Penalties list
        penalties.forEach { penalty ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = penalty.type.getDisplayColor().copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("₹${penalty.amount}", style = MaterialTheme.typography.titleMedium)
                    Text("Type: ${penalty.type.label}")
                    Text("Reason: ${penalty.reason}")
                    Text("Date: ${penalty.date}")
                    Text("Recorded by: ${penalty.recordedBy}")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Export button
        Button(
            onClick = {
                val headers = listOf("Date", "Amount", "Type", "Reason", "Recorded By")
                val rows = viewModel.getExportRows()
                // Pass headers + rows to export logic
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export Report")
        }
    }

    // Show Custom Period Picker
    if (showCustomDialog) {
        CustomPeriodPickerDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { period ->
                customPeriod = period
                viewModel.loadByCustomPeriod(period)
                showCustomDialog = false
            }
        )
    }
}
