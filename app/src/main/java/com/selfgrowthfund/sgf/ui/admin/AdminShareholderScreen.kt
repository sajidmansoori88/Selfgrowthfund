package com.selfgrowthfund.sgf.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.model.User
import com.selfgrowthfund.sgf.ui.admin.components.ShareholdersTable
import com.selfgrowthfund.sgf.ui.theme.GradientBackground

@Composable
fun AdminShareholderScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminDashboardViewModel = hiltViewModel(),
    onAddClick: () -> Unit = {},
    onModifyClick: (User) -> Unit = {}, // Keep original parameter name
    onDeleteClick: (User) -> Unit = {}  // Keep original parameter name
) {
    val shareholders by viewModel.shareholders.collectAsState()

    GradientBackground {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "Shareholder Management",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Shareholder")
            }

            Spacer(modifier = Modifier.height(16.dp))

            ShareholdersTable(
                shareholders = shareholders,
                onModify = onModifyClick,    // Pass to correct parameter
                onDelete = onDeleteClick,    // Pass to correct parameter
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
            )
        }
    }
}