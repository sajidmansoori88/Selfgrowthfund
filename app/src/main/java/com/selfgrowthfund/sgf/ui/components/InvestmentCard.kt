package com.selfgrowthfund.sgf.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.model.enums.MemberRole
import java.time.format.DateTimeFormatter

@Composable
fun InvestmentCard(
    investment: Investment,
    currentUserRole: MemberRole,
    onAddReturn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    Card(modifier = modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(investment.investmentName, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))

            Text("Amount: ₹${investment.amount}", style = MaterialTheme.typography.bodyMedium)
            Text("Expected Profit: ₹${investment.expectedProfitAmount}", style = MaterialTheme.typography.bodyMedium)
            Text("Return Due: ${investment.returnDueDate.format(formatter)}", style = MaterialTheme.typography.bodyMedium)
            Text("Status: ${investment.status}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(12.dp))

            if (currentUserRole == MemberRole.MEMBER_ADMIN || currentUserRole == MemberRole.MEMBER_TREASURER) {
                Button(onClick = onAddReturn, modifier = Modifier.fillMaxWidth()) {
                    Text("Record Return")
                }
            }
        }
    }
}