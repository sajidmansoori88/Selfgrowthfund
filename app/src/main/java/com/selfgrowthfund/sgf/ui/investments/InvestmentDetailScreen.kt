package com.selfgrowthfund.sgf.ui.investments

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.ui.components.InvestmentCard
import com.selfgrowthfund.sgf.ui.components.InvestmentReturnSummaryCard
import com.selfgrowthfund.sgf.ui.theme.GradientBackground

@Composable
fun InvestmentDetailScreen(
    modifier: Modifier = Modifier,
    investment: Investment,
    returns: List<InvestmentReturns>,
    currentUserRole: MemberRole,
    onAddReturn: () -> Unit,
    onApplyInvestment: () -> Unit,
    onDrawerClick: () -> Unit
) {

    GradientBackground {
        Column(
            modifier = modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            InvestmentCard(
                investment = investment,
                currentUserRole = currentUserRole,
                onAddReturn = onAddReturn
            )

            Spacer(modifier = Modifier.height(16.dp))

            InvestmentReturnSummaryCard(returns = returns)
            Spacer(modifier = Modifier.height(16.dp))

            if (returns.isEmpty()) {
                EmptyReturnsState()
            } else {
                PastReturnsList(returns = returns)
            }

            if (currentUserRole.isAdminOrTreasurer()) {
                Spacer(modifier = Modifier.height(24.dp))
                AdminActions(
                    onAddReturn = onAddReturn,
                    onApplyInvestment = onApplyInvestment,
                    investment = investment
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PastReturnsList(returns: List<InvestmentReturns>) {
    Column {
        Text(
            "Past Returns",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        returns.forEach { ret ->
            PastReturnItem(returnItem = ret)
        }
    }
}

@Composable
private fun PastReturnItem(returnItem: InvestmentReturns) {
    Text(
        "• ₹${returnItem.amountReceived} on ${returnItem.returnDate}",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun EmptyReturnsState() {
    Text(
        "No returns recorded yet",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
private fun AdminActions(
    modifier: Modifier = Modifier,
    onAddReturn: () -> Unit,
    onApplyInvestment: () -> Unit,
    investment: Investment? = null
) {
    // ✅ Explicitly log what’s happening
    Log.d("INVESTMENT_UI", "AdminActions check: investmentDate=${investment?.investmentDate}, approval=${investment?.approvalStatus}")

    // ✅ Use string-safe comparison (works whether approvalStatus is enum or string)
    val canAddReturn = investment?.let {
        it.investmentDate != null &&
                (
                        it.approvalStatus.toString().equals("APPROVED", ignoreCase = true) ||
                                it.approvalStatus.toString().equals("ADMIN_APPROVED", ignoreCase = true)
                        )
    } ?: false

    Log.d("INVESTMENT_UI", "canAddReturn = $canAddReturn")

    Column(modifier = modifier) {
        ExtendedFloatingActionButton(
            text = { Text("Add Return") },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add Return") },
            onClick = {
                // ✅ Always log before navigation
                Log.d("INVESTMENT_UI", "Add Return button clicked, canAddReturn=$canAddReturn")
                if (canAddReturn) {
                    onAddReturn()
                } else {
                    Log.w("INVESTMENT_UI", "Add Return disabled — status=${investment?.approvalStatus}")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            containerColor = if (canAddReturn)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (canAddReturn)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!canAddReturn) {
            Text(
                text = "Add Return available after Admin approval",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
        }

        ExtendedFloatingActionButton(
            text = { Text("Apply Investment") },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = "Apply Investment"
                )
            },
            onClick = onApplyInvestment,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
// Optional: Loading state composable if you need it later
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        CircularProgressIndicator()
    }
}

// Optional: Error state composable if you need it later
@Composable
private fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

fun MemberRole.isAdminOrTreasurer(): Boolean {
    return this == MemberRole.MEMBER_TREASURER || this == MemberRole.MEMBER_ADMIN
}