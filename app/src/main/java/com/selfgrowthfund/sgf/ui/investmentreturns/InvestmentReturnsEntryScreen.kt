package com.selfgrowthfund.sgf.ui.investmentreturns

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturnEntry
import com.selfgrowthfund.sgf.model.enums.EntrySource
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.ui.components.LoadingIndicator
import com.selfgrowthfund.sgf.ui.investments.DropdownMenuBox
import com.selfgrowthfund.sgf.utils.Result
import java.text.DecimalFormat

@Composable
fun InvestmentReturnsEntryScreen(
    investment: Investment,
    viewModel: InvestmentReturnsViewModel,
    onReturnAdded: () -> Unit,
    currentUserName: String
) {
    var amountReceived by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.CASH) }

    val addReturnState by viewModel.addReturnState.collectAsState()
    val isValidAmount = amountReceived.toDoubleOrNull()?.let { it > 0.0 } == true
    val amountError = amountReceived.isNotBlank() && amountReceived.toDoubleOrNull() == null

    val preview = remember(amountReceived, remarks, selectedPaymentMode) {
        val amount = amountReceived.toDoubleOrNull() ?: 0.0
        val entry = InvestmentReturnEntry(
            investment = investment,
            amountReceived = amount,
            modeOfPayment = selectedPaymentMode,
            remarks = remarks.ifBlank { null },
            entrySource = EntrySource.ADMIN,
            enteredBy = currentUserName
        )
        viewModel.previewReturn(entry)
    }

    LaunchedEffect(addReturnState) {
        if (addReturnState is Result.Success) {
            viewModel.clearState()
            onReturnAdded()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Enter Return for ${investment.investmentName}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amountReceived,
            onValueChange = { amountReceived = it },
            label = { Text("Amount Received") },
            isError = amountError,
            supportingText = {
                if (amountError) Text("Enter a valid number", color = MaterialTheme.colorScheme.error)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenuBox(
            label = "Mode of Payment",
            options = PaymentMode.entries,
            selected = selectedPaymentMode,
            onSelected = { selectedPaymentMode = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = remarks,
            onValueChange = { remarks = it },
            label = { Text("Remarks (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Preview:", style = MaterialTheme.typography.titleSmall)
        Text("Return Period: ${preview.actualReturnPeriod} days", style = MaterialTheme.typography.bodyMedium)
        Text("Profit Percent: ${formatPercent(preview.actualProfitPercent)}", style = MaterialTheme.typography.bodyMedium)
        Text("Profit Variance: ₹${formatAmount(preview.profitAmountVariance)}", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val amount = amountReceived.toDoubleOrNull()
                if (amount != null && amount > 0.0) {
                    val entry = InvestmentReturnEntry(
                        investment = investment,
                        amountReceived = amount,
                        modeOfPayment = selectedPaymentMode,
                        remarks = remarks.ifBlank { null },
                        entrySource = EntrySource.ADMIN,
                        enteredBy = currentUserName
                    )
                    viewModel.submitReturn(entry, lastReturnId = null, onSuccess = onReturnAdded) {
                        // Optional: show error toast/snackbar
                    }
                }
            },
            enabled = isValidAmount && addReturnState !is Result.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Return")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (addReturnState) {
            is Result.Loading -> LoadingIndicator()
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