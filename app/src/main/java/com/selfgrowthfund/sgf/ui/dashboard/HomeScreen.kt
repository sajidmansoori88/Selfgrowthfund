package com.selfgrowthfund.sgf.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.model.reports.ShareholderSummary
import com.selfgrowthfund.sgf.model.reports.ShareholderSummaryViewModel
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.ui.theme.GradientBackground
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val userSessionViewModel: UserSessionViewModel = hiltViewModel()
    val user by userSessionViewModel.currentUser.collectAsState()

    val summaryViewModel: ShareholderSummaryViewModel = hiltViewModel()
    val summaries by summaryViewModel.summaries.collectAsState()

    var isLoading by remember { mutableStateOf(true) }
    var loadingError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            summaryViewModel.loadAllSummaries()
        } catch (e: Exception) {
            loadingError = "Error loading data: ${e.message}"
            isLoading = false
        }
    }

    LaunchedEffect(summaries) {
        isLoading = false
        loadingError = if (summaries.isEmpty()) "No summaries found in database" else null
    }

    val currentSummary = summaries.find { it.shareholderId == user.shareholderId }

    GradientBackground {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "DEBUG: User=${user.shareholderId}, Summaries=${summaries.size}, Loading=$isLoading",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )

            if (loadingError != null) {
                Text(
                    "ERROR: $loadingError",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                )
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Loading dashboard...")
                        }
                    }
                }

                currentSummary != null -> {
                    HomeScreenContent(
                        name = user.name,
                        role = user.role.name,
                        summary = currentSummary,
                        modifier = Modifier.padding(32.dp)
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No data found", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("User ID: ${user.shareholderId}")
                            Text("Total summaries loaded: ${summaries.size}")

                            Button(
                                onClick = {
                                    isLoading = true
                                    loadingError = null
                                    summaryViewModel.loadAllSummaries()
                                }
                            ) { Text("Retry Load") }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HomeScreenContent(
    name: String,
    role: String,
    summary: ShareholderSummary,
    modifier: Modifier = Modifier
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy") }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Welcome, $name", style = MaterialTheme.typography.headlineMedium)
        Text("Role: $role", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        Text("Total Share Contribution", style = MaterialTheme.typography.titleMedium)
        Text("₹${summary.shareAmount}", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Current Value", style = MaterialTheme.typography.titleMedium)
        Text("₹${summary.shareValue}", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = "Growth: ${"%.2f".format(summary.absoluteReturn)}%",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Last Contribution", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "₹${summary.lastContributionAmount} (${summary.lastContributionDate.format(formatter)})",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Next Due Contribution", style = MaterialTheme.typography.titleMedium)
        Text(summary.nextDue.format(formatter), style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(24.dp))

        if (summary.outstandingBorrowing > 0) {
            Text(
                text = "⚠️ Outstanding Borrowing: ₹${summary.outstandingBorrowing}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
