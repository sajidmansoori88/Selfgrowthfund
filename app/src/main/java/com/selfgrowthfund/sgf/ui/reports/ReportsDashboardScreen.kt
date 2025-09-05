package com.selfgrowthfund.sgf.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.selfgrowthfund.sgf.ui.components.ReportCard
import com.selfgrowthfund.sgf.ui.navigation.Screen
import com.selfgrowthfund.sgf.ui.components.SectionHeader
import com.selfgrowthfund.sgf.ui.components.MetricCard
import com.selfgrowthfund.sgf.ui.components.CashFlowChart
import com.selfgrowthfund.sgf.ui.components.SGFScaffoldWrapper
import com.selfgrowthfund.sgf.ui.navigation.DrawerContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ReportsDashboardScreen(
    navController: NavHostController,
    viewModel: ReportsDashboardViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val fundOverview by viewModel.fundOverview.collectAsState()
    val monthlyCashFlow by viewModel.monthlyCashFlow.collectAsState()

    SGFScaffoldWrapper(
        title = "Reports Dashboard",
        drawerState = drawerState,
        scope = scope,
        drawerContent = {
            DrawerContent(
                navController = navController,
                onItemClick = { scope.launch { drawerState.close() } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    value = fundOverview.totalShareAmount
                )
                MetricCard(
                    title = "Net Profit / Loss",
                    value = fundOverview.netProfitOrLoss,
                    highlight = true
                )
                MetricCard(
                    title = "Active Investments",
                    count = fundOverview.activeInvestments
                )
                MetricCard(
                    title = "Outstanding Borrowings",
                    value = fundOverview.outstandingBorrowings
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(title = "Monthly Cash Flow")
            CashFlowChart(data = monthlyCashFlow)

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(title = "Detailed Reports")

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ReportCard(title = "Fund Overview") {
                    navController.navigate(Screen.FundOverviewReport.route)
                }
                ReportCard(title = "Shareholder Summary") {
                    navController.navigate(Screen.ShareholderSummaryReport.route)
                }
                ReportCard(title = "Borrowing Ledger") {
                    navController.navigate(Screen.BorrowingReport.route)
                }
                ReportCard(title = "Investment Performance") {
                    navController.navigate(Screen.InvestmentReport.route)
                }
                ReportCard(title = "Cash Flow Report") {
                    navController.navigate(Screen.CashFlowReport.route)
                }
            }
        }
    }
}