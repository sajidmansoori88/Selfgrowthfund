package com.selfgrowthfund.sgf.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.model.enums.MemberRole

@Composable
fun AdminDashboardScreen(
    role: MemberRole,
    modifier: Modifier = Modifier, // ✅ Add modifier parameter
    onApproveMembers: () -> Unit = {}, // ✅ Add callback for navigation
    onManageRoles: () -> Unit = {},
    onViewReports: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()) // ✅ Make it scrollable
            .padding(24.dp)
    ) {
        Text("Welcome, Admin", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Role: ${role.name}", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(32.dp))

        Text("Admin Actions", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Use the provided callbacks
        Button(
            onClick = onApproveMembers,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Approve New Members")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onManageRoles,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Manage Member Roles")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onViewReports,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Reports")
        }

        // ✅ Add bottom spacer to ensure content isn't cut off
        Spacer(modifier = Modifier.height(32.dp))
    }
}