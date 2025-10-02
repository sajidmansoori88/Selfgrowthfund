package com.selfgrowthfund.sgf.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.ui.admin.components.ShareholdersTable
import com.selfgrowthfund.sgf.ui.theme.GradientBackground
import com.selfgrowthfund.sgf.data.local.entities.Shareholder

@Composable
fun AdminShareholderScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminDashboardViewModel = hiltViewModel(),
    onAddClick: () -> Unit = {},
    onModifyClick: (Shareholder) -> Unit = {},
    onDeleteClick: (Shareholder) -> Unit = {}
) {
    val shareholders by viewModel.shareholders.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // 🔔 Show Snackbars when success/error messages change
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    GradientBackground {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .padding(innerPadding)
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
                    onModify = { shareholder -> viewModel.modifyShareholder(shareholder) },
                    onDelete = { shareholder -> viewModel.deleteShareholder(shareholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                )

            }
        }
    }
}
