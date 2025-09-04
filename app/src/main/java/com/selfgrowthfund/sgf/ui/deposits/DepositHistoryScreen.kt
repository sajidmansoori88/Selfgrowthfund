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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.selfgrowthfund.sgf.data.local.dto.DepositEntrySummaryDTO
import com.selfgrowthfund.sgf.ui.components.DepositSummaryCard
import com.selfgrowthfund.sgf.ui.components.SGFScaffoldWrapper
import com.selfgrowthfund.sgf.ui.navigation.DrawerContent
import com.selfgrowthfund.sgf.ui.theme.SGFTheme
import com.selfgrowthfund.sgf.utils.ExportUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositHistoryScreen(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val viewModel: DepositViewModel = hiltViewModel()
    val summaries by viewModel.depositSummaries.collectAsState(initial = emptyList())

    SGFScaffoldWrapper(
        title = "Deposit History",
        drawerState = drawerState,
        scope = scope,
        drawerContent = {
            DrawerContent(
                navController = navController,
                onItemClick = { scope.launch { drawerState.close() } }
            )
        },
        content = { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (summaries.isEmpty()) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("No deposit history available.")
                    }
                } else {
                    LazyColumn {
                        items(summaries) { summary ->
                            DepositSummaryCard(summary)
                        }
                    }
                }
            }
        }
    )
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

@Preview(showBackground = true)
@Composable
fun DepositHistoryPreview() {
    val mockSummaries = listOf(
        DepositEntrySummaryDTO(
            shareholderName = "John Doe",
            dueMonth = "August 2025",
            paymentDate = LocalDate.of(2025, 8, 10),
            totalAmount = 6000.0,
            paymentStatus = "Paid"
        )
    )

    SGFTheme {
        Scaffold {
            LazyColumn(modifier = Modifier.padding(it)) {
                items(mockSummaries) { summary ->
                    DepositSummaryCard(summary)
                }
            }
        }
    }
}