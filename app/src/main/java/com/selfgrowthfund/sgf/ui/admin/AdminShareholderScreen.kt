package com.selfgrowthfund.sgf.ui.admin

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.ui.admin.components.ShareholdersTable
import com.selfgrowthfund.sgf.ui.theme.GradientBackground
import com.selfgrowthfund.sgf.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminShareholderScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AdminDashboardViewModel = hiltViewModel(),
    onAddClick: () -> Unit = {},
    onModifyClick: (Shareholder) -> Unit = {},
    onDeleteClick: (Shareholder) -> Unit = {}
) {
    val shareholders by viewModel.shareholders.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 🔔 Snackbar for success/error
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 16.dp,
                        top = 8.dp,
                        end = 16.dp,
                        bottom = innerPadding.calculateBottomPadding() + 16.dp
                    )

            ) {
                // ───── Add Button ─────
                Button(
                    onClick = {
                        navController.navigate(Screen.AddShareholder.route)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add a New Shareholder")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ───── Data Section ─────
                AnimatedContent(
                    targetState = shareholders,
                    label = "shareholdersTransition"
                ) { list ->
                    if (list.isEmpty()) {
                        // Centered loader or empty state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        ShareholdersTable(
                            shareholders = list,
                            onModify = { shareholder ->
                                navController.navigate("edit_shareholder_screen/${shareholder.shareholderId}")
                            },
                            onDelete = { shareholder ->
                                viewModel.deleteShareholder(shareholder)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                        )
                    }
                }
            }
        }
    }
}
