package com.selfgrowthfund.sgf.ui.deposits

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.data.local.dto.DepositEntrySummaryDTO
import com.selfgrowthfund.sgf.ui.components.DepositSummaryCard
import com.selfgrowthfund.sgf.utils.ExportUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositHistoryScreen(viewModel: DepositViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val summaries by viewModel.depositSummaries.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deposit History") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExportActions(context = context, summaries = summaries)
        }
    ) { padding ->
        if (summaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No deposit history available.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(summaries) { summary ->
                    DepositSummaryCard(summary)
                }
            }
        }
    }
}

@Composable
fun ExportActions(context: Context, summaries: List<DepositEntrySummaryDTO>) {
    Row(modifier = Modifier.padding(16.dp)) {
        Button(onClick = {
            try {
                val headers = listOf("Shareholder", "Due Month", "Payment Date", "Amount", "Status")
                val rows = summaries.map {
                    listOf(
                        it.shareholderName,
                        it.dueMonth,
                        formatDate(it.paymentDate),
                        "₹%.2f".format(it.totalAmount),
                        it.paymentStatus
                    )
                }
                val file = ExportUtils.exportToCsv(context, headers, rows, "DepositHistory.csv")
                ExportUtils.shareFile(context, file, "text/csv")
                Toast.makeText(context, "CSV exported successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "CSV export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Export CSV")
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(onClick = {
            try {
                val lines = summaries.map {
                    "${it.shareholderName} | ${it.dueMonth} | ${formatDate(it.paymentDate)} | ₹%.2f | ${it.paymentStatus}"
                        .format(it.totalAmount)
                }
                val file = ExportUtils.exportToPdf(context, "Deposit History", lines, "DepositHistory.pdf")
                ExportUtils.shareFile(context, file, "application/pdf")
                Toast.makeText(context, "PDF exported successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "PDF export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Export PDF")
        }
    }
}

fun formatDate(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
}
