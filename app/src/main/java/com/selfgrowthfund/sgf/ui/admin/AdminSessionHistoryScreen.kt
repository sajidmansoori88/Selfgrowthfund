package com.selfgrowthfund.sgf.ui.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.session.SessionEntry
import com.selfgrowthfund.sgf.ui.theme.GradientBackground

@Composable
fun AdminSessionHistoryScreen(
    viewModel: AdminDashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val sessionEntries by viewModel.sessionHistory.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle snackbar messages from ViewModel
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    GradientBackground {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // Loading indicator
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Export Button
                Button(
                    onClick = { viewModel.exportSessionCSV() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Export CSV")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (sessionEntries.isEmpty() && !uiState.isLoading) {
                    Text(
                        "No session history available",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    // Horizontal scroll for the table
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item { SessionHeaderRow() }
                            itemsIndexed(sessionEntries) { index, entry ->
                                SessionRow(index = index, entry = entry)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Sr", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("Name", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
        Text("This Month", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("Lifetime", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    }
}

@Composable
fun SessionRow(index: Int, entry: SessionEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = entry.sr.toString(),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = entry.name,
                modifier = Modifier.weight(2f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = entry.currentMonthSessions.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = entry.lifetimeSessions.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
