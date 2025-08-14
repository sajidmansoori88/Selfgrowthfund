package com.selfgrowthfund.sgf.ui.investmentreturns

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.ui.components.LoadingIndicator
import com.selfgrowthfund.sgf.utils.Result
import java.text.DecimalFormat

@Composable
fun InvestmentReturnsEntryScreen(
    investment: Investment,
    viewModel: InvestmentReturnsViewModel,
    onReturnAdded: () -> Unit
) {
    var amountReceived by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    val addReturnState by viewModel.addReturnState.collectAsState()

    val preview = remember(amountReceived) {
        val amount = amountReceived.toDoubleOrNull() ?: 0.0
        viewModel.previewReturn(investment, amount, remarks.ifBlank { null })
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Enter Return for ${investment.investmentName}", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amountReceived,
            onValueChange = { amountReceived = it },
            label = { Text("Amount Received") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = remarks,
            onValueChange = { remarks = it },
            label = { Text("Remarks (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Preview:")
        Text("Return Period: ${preview.actualReturnPeriod} days")
        Text("Profit Percent: ${formatPercent(preview.actualProfitPercent)}")
        Text("Profit Variance: ₹${formatAmount(preview.profitAmountVariance)}")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val amount = amountReceived.toDoubleOrNull()
                if (amount != null && amount > 0.0) {
                    viewModel.addReturn(
                        investmentId = investment.investmentId,
                        amountReceived = amount,
                        remarks = remarks.ifBlank { null }
                    )
                }
            },
            enabled = addReturnState !is Result.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Return")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (addReturnState) {
            is Result.Loading -> LoadingIndicator()
            is Result.Success -> {
                LaunchedEffect(Unit) {
                    viewModel.clearState()
                    onReturnAdded()
                }
            }
            is Result.Error -> {
                Text(
                    text = "Error: ${(addReturnState as Result.Error).exception.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {}
        }
    }
}

private fun formatPercent(value: Double): String {
    return DecimalFormat("#.##").format(value) + "%"
}

private fun formatAmount(value: Double): String {
    return DecimalFormat("#,##0.00").format(value)
}