package com.selfgrowthfund.sgf.ui.investments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.ui.components.InvestmentCard
import com.selfgrowthfund.sgf.ui.components.InvestmentReturnSummaryCard
import com.selfgrowthfund.sgf.ui.components.SGFScaffoldWrapper
import com.selfgrowthfund.sgf.ui.theme.GradientBackground

@Composable
fun InvestmentDetailScreen(
    investment: Investment,
    returns: List<InvestmentReturns>,
    currentUserRole: MemberRole,
    onAddReturn: () -> Unit,
    onApplyInvestment: () -> Unit,
    onDrawerClick: () -> Unit
) {
    SGFScaffoldWrapper(
        title = "Investments",
        onDrawerClick = onDrawerClick
    ) { innerPadding ->
        GradientBackground {
            Column(
                modifier = Modifier
                    .padding(innerPadding) // ✅ Apply the scaffold padding
                    .padding(16.dp) // ✅ Add additional content padding
                    .verticalScroll(rememberScrollState()) // ✅ Make it scrollable
                    .fillMaxSize()
            ) {
                InvestmentCard(
                    investment = investment,
                    currentUserRole = currentUserRole,
                    onAddReturn = onAddReturn
                )

                Spacer(modifier = Modifier.height(16.dp))

                InvestmentReturnSummaryCard(returns = returns)

                if (returns.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Past Returns",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    returns.forEach { ret ->
                        Text(
                            "• ₹${ret.amountReceived} on ${ret.returnDate}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                // ✅ Floating Action Buttons with proper spacing
                if (currentUserRole == MemberRole.MEMBER_TREASURER || currentUserRole == MemberRole.MEMBER_ADMIN) {
                    Spacer(modifier = Modifier.height(24.dp))
                    ExtendedFloatingActionButton(
                        text = { Text("Add Return") },
                        icon = { Icon(Icons.Default.Add, contentDescription = "Add Return") },
                        onClick = onAddReturn,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ExtendedFloatingActionButton(
                        text = { Text("Apply Investment") },
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Apply Investment") },
                        onClick = onApplyInvestment,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ✅ Add bottom spacer to ensure content isn't cut off
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}