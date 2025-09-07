package com.selfgrowthfund.sgf.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.model.reports.ShareholderSummary
import com.selfgrowthfund.sgf.model.reports.ShareholderSummaryViewModel
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.ui.theme.SGFTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val userSessionViewModel: UserSessionViewModel = hiltViewModel()
    val user by userSessionViewModel.currentUser.collectAsState()

    val summaryViewModel: ShareholderSummaryViewModel = hiltViewModel()
    val summaries by summaryViewModel.summaries.collectAsState()

    // Track loading + error
    var isLoading by remember { mutableStateOf(true) }
    var loadingError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        println("DEBUG: HomeScreen launched - loading summaries")
        try {
            summaryViewModel.loadAllSummaries()
            // ✅ don’t collect inside LaunchedEffect — collectAsState is already observing
        } catch (e: Exception) {
            loadingError = "Error loading data: ${e.message}"
            isLoading = false
        }
    }

    // ✅ update loading state automatically when summaries flow updates
    LaunchedEffect(summaries) {
        isLoading = false
        if (summaries.isEmpty()) {
            loadingError = "No summaries found in database"
        } else {
            loadingError = null
        }
    }

    val currentSummary = summaries.find { it.shareholderId == user.shareholderId }

    Column(modifier = Modifier.fillMaxSize()) {
        // Debug info
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

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                        summary = currentSummary
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No data found", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("User ID: ${user.shareholderId}")
                            Text("Total summaries loaded: ${summaries.size}")
                            Text("Available IDs: ${summaries.map { it.shareholderId }}")

                            Button(
                                onClick = {
                                    isLoading = true
                                    loadingError = null
                                    summaryViewModel.loadAllSummaries()
                                }
                            ) {
                                Text("Retry Load")
                            }
                        }
                    }
                }
            }
        }
    }
}
    @Composable
    fun HomeScreenContent(
        name: String,
        role: String,
        summary: ShareholderSummary
    ) {
        val formatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
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
                text = "₹${summary.lastContributionAmount} (${
                    summary.lastContributionDate.format(
                        formatter
                    )
                })",
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview(showBackground = true, name = "Home Screen Preview")
    @Composable
    fun HomeScreenPreview() {
        SGFTheme {
            val fakeSummary = ShareholderSummary(
                shareholderId = "SH001",
                name = "Sajid Mansoori",
                shares = 15,
                shareAmount = 30000.0,
                shareValue = 250000.0,
                percentContribution = 12.5,
                netProfit = 220000.0,
                absoluteReturn = 733.33,
                annualizedReturn = 120.0,
                lastContributionAmount = 6000.0,
                lastContributionDate = LocalDate.of(2025, 8, 10),
                nextDue = LocalDate.of(2025, 9, 10),
                outstandingBorrowing = 7000.0
            )

            Box(modifier = Modifier) {
                HomeScreenContent(
                    name = "Sajid",
                    role = MemberRole.MEMBER.name,
                    summary = fakeSummary
                )
            }
        }
    }