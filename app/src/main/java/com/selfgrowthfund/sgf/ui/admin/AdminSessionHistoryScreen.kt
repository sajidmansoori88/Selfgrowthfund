package com.selfgrowthfund.sgf.ui.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.ui.admin.components.SessionHeaderRow
import com.selfgrowthfund.sgf.ui.admin.components.SessionRow
import com.selfgrowthfund.sgf.ui.theme.GradientBackground
import kotlinx.coroutines.launch

@Composable
fun AdminSessionHistoryScreen(
    viewModel: AdminDashboardViewModel,
    modifier: Modifier = Modifier
) {
    val sessionEntries by viewModel.sessionHistory.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    GradientBackground {
        Column(
            modifier = modifier
                .horizontalScroll(rememberScrollState())
                .padding(16.dp)) {
            Button(onClick = {
                viewModel.exportSessionCSV()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Session history CSV exported")
                }
            }) {
                Text("Export CSV")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    SessionHeaderRow()
                }
                itemsIndexed(sessionEntries) { index, entry ->
                    SessionRow(index, entry)
                }
            }
        }
    }
}