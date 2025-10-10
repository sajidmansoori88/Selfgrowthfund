package com.selfgrowthfund.sgf.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.ui.theme.GradientBackground

@Composable
fun AdminDashboardScreen(
    role: MemberRole,
    modifier: Modifier = Modifier,
    onManageShareholders: () -> Unit = {},
    onViewApprovals: () -> Unit = {},
    onViewApprovalHistory: () -> Unit = {},
    onViewSessionHistory: () -> Unit = {},
    onViewReports: () -> Unit = {}
) {
    GradientBackground {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState()) // ✅ Make it scrollable
                .padding(24.dp)
        ) {

            // ✅ Use the provided callbacks
            Button(
                onClick = onManageShareholders,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage Shareholders")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onViewApprovals,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pending Approvals")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onViewApprovalHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Approval History")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onViewSessionHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("User Session History")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onViewReports,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Reports")
            }
            Spacer(modifier = Modifier.height(8.dp))


            // ✅ Add bottom spacer to ensure content isn't cut off
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}