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
 * 📊 Holds all Treasurer dashboard data across tabs.
 * Updated for quorum-based borrowing & dynamic approval tracking.
 */
data class TreasurerDashboardUiState(
    val deposits: List<Deposit> = emptyList(),
    val borrowings: List<Borrowing> = emptyList(),
    val repayments: List<Repayment> = emptyList(),
    val investments: List<Investment> = emptyList(),
    val returns: List<InvestmentReturns> = emptyList(),

    // 🔹 Metadata / State Control
    val isLoading: Boolean = false,
    val message: String? = null,

    // 🔹 New optional summary fields (used in future)
    val totalActiveMembers: Int = 0,
    val quorumRequired: Int = 0,
    val approvalProgressMap: Map<String, Int> = emptyMap() // borrowingId -> approvals count
)
