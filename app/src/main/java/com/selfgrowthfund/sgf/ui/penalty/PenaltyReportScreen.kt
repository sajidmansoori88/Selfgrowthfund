package com.selfgrowthfund.sgf.ui.penalty

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.selfgrowthfund.sgf.model.User
import com.selfgrowthfund.sgf.model.enums.PenaltyType
import com.selfgrowthfund.sgf.model.enums.ReportPeriod
import com.selfgrowthfund.sgf.ui.components.DropdownMenuBox
import com.selfgrowthfund.sgf.ui.components.SGFScaffoldWrapper
import com.selfgrowthfund.sgf.ui.navigation.DrawerContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun PenaltyReportScreen(
    navController: NavHostController,
    user: User,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val viewModel: PenaltyReportViewModel = hiltViewModel()
    val penalties by viewModel.penalties.collectAsState()

    var selectedType by remember { mutableStateOf(PenaltyType.OTHER) }
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.CURRENT_MONTH) }

    LaunchedEffect(selectedPeriod) {
        viewModel.loadByPeriod(selectedPeriod)
    }

    SGFScaffoldWrapper(
        title = "Penalty Report",
        drawerState = drawerState,
        scope = scope,
        drawerContent = {
            DrawerContent(
                navController = navController,
                onItemClick = { scope.launch { drawerState.close() } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Penalty Report", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(8.dp))

            DropdownMenuBox(
                label = "Report Period",
                options = ReportPeriod.entries.toList(),
                selected = selectedPeriod,
                onSelected = {
                    selectedPeriod = it
                    viewModel.loadByPeriod(it)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            DropdownMenuBox(
                label = "Filter by Type",
                options = PenaltyType.entries.toList(),
                selected = selectedType,
                onSelected = {
                    selectedType = it
                    viewModel.filterByType(it)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

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
    }
}