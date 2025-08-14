package com.selfgrowthfund.sgf.ui.repayment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RepaymentListScreen(
    viewModel: RepaymentViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {
            CircularProgressIndicator(modifier = modifier)
        }
        uiState.error != null -> {
            Text(
                text = uiState.error?.message ?: "Unknown error",
                color = Color.Red,
                modifier = modifier
            )
        }
        uiState.repayments.isEmpty() -> {
            Text("No repayments found", modifier = modifier)
        }
        else -> {
            LazyColumn(modifier = modifier.padding(16.dp)) {
                items(uiState.repayments) { repayment ->
                    RepaymentItem(repayment)
                }
            }
        }
    }
}
