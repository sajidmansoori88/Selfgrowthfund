package com.selfgrowthfund.sgf.ui.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.ui.components.DropdownMenuBox
import com.selfgrowthfund.sgf.ui.components.reportingperiod.*
import com.selfgrowthfund.sgf.ui.components.EmptyStateCard
import com.selfgrowthfund.sgf.ui.theme.GradientBackground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// ---------- Screen ----------
@Composable
fun AdminApprovalHistoryScreen(
    viewModel: AdminDashboardViewModel,
    snackbarHostState: SnackbarHostState, // injected from SGFScaffoldWrapper
    modifier: Modifier = Modifier
) {
    val summary by viewModel.approvalSummary.collectAsState(initial = emptyList())
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val customPeriod by viewModel.customPeriod.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val periodCalculator = remember { ReportPeriodCalculator() }
    var showCustomDialog by remember { mutableStateOf(false) }

    GradientBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // ===== Header Row (only this row is horizontally scrollable) =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DropdownMenuBox(
                    label = "Approval Period",
                    options = ReportPeriod.entries.toList(),
                    selected = selectedPeriod,
                    onSelected = { period ->
                        viewModel.selectedPeriod.value = period
                        if (period == ReportPeriod.CUSTOM) {
                            showCustomDialog = true
                        } else {
                            viewModel.customPeriod.value = null
                            val dateRange = periodCalculator.getDateRange(period)
                            viewModel.loadApprovalSummary(period, dateRange.first, dateRange.second)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                Button(onClick = {
                    exportData(
                        type = "CSV",
                        selectedPeriod = selectedPeriod,
                        customPeriod = customPeriod,
                        periodCalculator = periodCalculator,
                        viewModel = viewModel,
                        snackbarHostState = snackbarHostState,
                        coroutineScope = coroutineScope
                    )
                }) {
                    Text("CSV")
                }

                Button(onClick = {
                    exportData(
                        type = "PDF",
                        selectedPeriod = selectedPeriod,
                        customPeriod = customPeriod,
                        periodCalculator = periodCalculator,
                        viewModel = viewModel,
                        snackbarHostState = snackbarHostState,
                        coroutineScope = coroutineScope
                    )
                }) {
                    Text("PDF")
                }
            }

            // Show custom period label when applicable
            if (selectedPeriod == ReportPeriod.CUSTOM && customPeriod != null) {
                Text(
                    text = customPeriod!!.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp, start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ===== Summary Table (compact / vertical-small) =====
            if (summary.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateCard(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.History,
                        title = "No data available",
                        message = "No approvals have been recorded for the selected period."
                    )
                }
            } else {
                ApprovalSummaryTable(
                    summary = summary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Custom Period Picker Dialog
    if (showCustomDialog) {
        CustomPeriodPickerDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { period ->
                viewModel.customPeriod.value = period
                val (start, end) = period.startDate to period.endDate
                viewModel.loadApprovalSummary(ReportPeriod.CUSTOM, start, end)
                showCustomDialog = false
            }
        )
    }
}

// ---------- Export helper ----------
private fun exportData(
    type: String,
    selectedPeriod: ReportPeriod,
    customPeriod: CustomPeriod?,
    periodCalculator: ReportPeriodCalculator,
    viewModel: AdminDashboardViewModel,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    val dateRange = if (selectedPeriod == ReportPeriod.CUSTOM && customPeriod != null) {
        customPeriod.startDate to customPeriod.endDate
    } else {
        periodCalculator.getDateRange(selectedPeriod)
    }

    when (type) {
        "CSV" -> viewModel.exportCSV(selectedPeriod, dateRange.first, dateRange.second)
        "PDF" -> viewModel.exportPDF(selectedPeriod, dateRange.first, dateRange.second)
    }

    coroutineScope.launch {
        snackbarHostState.showSnackbar("$type exported for ${selectedPeriod.label}")
    }
}

// ---------- Compact table composable ----------
@Composable
fun ApprovalSummaryTable(summary: List<ApprovalSummaryRow>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Approval Type", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(2f))
                Text("Approved", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), maxLines = 1)
                Text("Rejected", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), maxLines = 1)
                Text("Pending", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), maxLines = 1)
                Text("Total", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), maxLines = 1)
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Rows (compact vertical spacing)
            summary.forEachIndexed { idx, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(row.type, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(2f))
                    Text(row.approved.toString(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(row.rejected.toString(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(row.pending.toString(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(row.total.toString(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
                if (idx < summary.lastIndex) {
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                }
            }
        }
    }
}

// ---------- Data model ----------
data class ApprovalSummaryRow(
    val type: String,
    val approved: Int,
    val rejected: Int,
    val pending: Int
) {
    val total: Int get() = approved + rejected + pending
}
