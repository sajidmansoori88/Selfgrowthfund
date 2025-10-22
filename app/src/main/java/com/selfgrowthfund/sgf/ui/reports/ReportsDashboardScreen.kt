package com.selfgrowthfund.sgf.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.ui.components.ReportCard
import com.selfgrowthfund.sgf.ui.components.SectionHeader
import com.selfgrowthfund.sgf.ui.components.MetricCard
import com.selfgrowthfund.sgf.ui.components.CashFlowChart
import com.selfgrowthfund.sgf.ui.components.MemberBorrowingStatusCard
import com.selfgrowthfund.sgf.ui.borrowing.BorrowingViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.ui.theme.GradientBackground

@Composable
fun ReportsDashboardScreen(
    viewModel: ReportsDashboardViewModel,
    borrowingViewModel: BorrowingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onFundOverview: () -> Unit = {},
    onShareholderSummary: () -> Unit = {},
    onBorrowingLedger: () -> Unit = {},
    onInvestmentPerformance: () -> Unit = {},
    onCashFlowReport: () -> Unit = {}
) {
    val fundOverview by viewModel.fundOverview.collectAsState()
    val monthlyCashFlow by viewModel.monthlyCashFlow.collectAsState()

    // 🟢 Collect borrowing summary data
    val memberStatuses by borrowingViewModel.memberStatuses.collectAsState()

    // Load statuses when the report screen opens
    LaunchedEffect(Unit) {
        borrowingViewModel.loadMemberBorrowingStatuses()
    }

    GradientBackground {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Reports Dashboard",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(title = "Key Metrics")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Total Shares",
                    count = (fundOverview.totalShareAmount / 2000).toInt(),
                    value = fundOverview.totalShareAmount,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Net Profit / Loss",
                    value = fundOverview.netProfitOrLoss,
                    highlight = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Active Investments",
                    count = fundOverview.activeInvestments,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Outstanding Borrowings",
                    value = fundOverview.outstandingBorrowings,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(title = "Monthly Cash Flow")
            CashFlowChart(data = monthlyCashFlow)

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(title = "Detailed Reports")

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportCard(title = "Fund Overview", onClick = onFundOverview)
                ReportCard(title = "Shareholder Summary", onClick = onShareholderSummary)
                ReportCard(title = "Borrowing Ledger", onClick = onBorrowingLedger)
                ReportCard(title = "Investment Performance", onClick = onInvestmentPerformance)
                ReportCard(title = "Cash Flow Report", onClick = onCashFlowReport)
            }

            // 🟢 NEW SECTION — Member Borrowing Status Summary
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(title = "Member Borrowing Status")

            if (memberStatuses.isEmpty()) {
                Text(
                    text = "No active borrowings found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(12.dp)
                )
            } else {
                Column {
                    memberStatuses.forEach { status ->
                        MemberBorrowingStatusCard(status)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
