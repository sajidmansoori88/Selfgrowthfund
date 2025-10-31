package com.selfgrowthfund.sgf.ui.treasurer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.*

/**
 * Displays the top summary row in Treasurer Dashboard.
 */
@Composable
fun SummaryRow(
    deposits: Int,
    borrowings: Int,
    repayments: Int,
    investments: Int,
    returns: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SummaryChip("Deposits", deposits)
        SummaryChip("Borrowings", borrowings)
        SummaryChip("Repayments", repayments)
        SummaryChip("Investments", investments)
        SummaryChip("Returns", returns)
    }
}

@Composable
private fun SummaryChip(label: String, count: Int) {
    Surface(
        color = if (count > 0) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 📊 Treasurer Dashboard UI State
 *
 * Holds all data fetched by TreasurerDashboardViewModel.
 * Designed to be Compose-friendly and reactive.
 */
data class TreasurerDashboardUiState(
    // 🔹 Data lists
    val deposits: List<Deposit> = emptyList(),
    val borrowings: List<Borrowing> = emptyList(),
    val repayments: List<Repayment> = emptyList(),
    val investments: List<Investment> = emptyList(),
    val returns: List<InvestmentReturns> = emptyList(),

    // 🔹 UI state flags
    val isLoading: Boolean = false,
    val message: String? = null,

    // 🔹 Treasurer-specific metrics
    val totalActiveMembers: Int = 0,
    val quorumRequired: Int = 0,

    // 🔹 Approval tracking map
    val approvalProgressMap: Map<String, Int> = emptyMap() // borrowing.provisionalId -> approvals count
)
