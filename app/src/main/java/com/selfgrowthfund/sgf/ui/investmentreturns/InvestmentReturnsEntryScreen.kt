package com.selfgrowthfund.sgf.ui.investmentreturns

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturnEntry
import com.selfgrowthfund.sgf.model.enums.EntrySource
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.ui.components.LoadingIndicator
import com.selfgrowthfund.sgf.ui.components.DropdownMenuBox
import com.selfgrowthfund.sgf.utils.Result
import java.text.DecimalFormat

@Composable
fun InvestmentReturnsEntryScreen(
    investment: Investment,
    viewModel: InvestmentReturnsViewModel,
    onReturnAdded: () -> Unit,
    currentUserName: String,
    modifier: Modifier = Modifier
) {
    var amountReceived by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.CASH) }
    var lastReturnId by remember { mutableStateOf<String?>(null) }

    // Collect states from ViewModel (matching your ViewModel structure)
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submissionResult by viewModel.submissionResult.collectAsState()

    val isValidAmount = amountReceived.toDoubleOrNull()?.let { it > 0.0 } == true
    val amountError = amountReceived.isNotBlank() && amountReceived.toDoubleOrNull() == null

    val preview = remember(amountReceived, remarks, selectedPaymentMode) {
        val amount = amountReceived.toDoubleOrNull() ?: 0.0
        val entry = InvestmentReturnEntry(
            investment = investment,
            amountReceived = amount,
            modeOfPayment = selectedPaymentMode,
            remarks = remarks.ifBlank { null },
            entrySource = EntrySource.MemberAdmin,
            enteredBy = currentUserName
        )
        viewModel.previewReturn(entry)
    }

    LaunchedEffect(submissionResult) {
        when (submissionResult) {
            is Result.Success -> {
                viewModel.clearState()
                onReturnAdded()
            }
            else -> {}
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Enter Return for ${investment.investmentName}",
            style = MaterialTheme.typography.titleMedium
        )
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

        Spacer(modifier = Modifier.height(12.dp))

        DropdownMenuBox(
            label = "Mode of Payment",
            options = PaymentMode.entries,
            selected = selectedPaymentMode,
            onSelected = { selectedPaymentMode = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = remarks,
            onValueChange = { remarks = it },
            label = { Text("Remarks (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Preview Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Preview:", style = MaterialTheme.typography.titleSmall)
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Return Period:", style = MaterialTheme.typography.bodyMedium)
                    Text("${preview.actualReturnPeriod} days", style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Profit Percent:", style = MaterialTheme.typography.bodyMedium)
                    Text(formatPercent(preview.actualProfitPercent), style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Profit Variance:", style = MaterialTheme.typography.bodyMedium)
                    Text("₹${formatAmount(preview.profitAmountVariance)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

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
                        entrySource = EntrySource.MemberAdmin,
                        enteredBy = currentUserName
                    )
                    viewModel.submitReturn(
                        entry = entry,
                        lastReturnId = lastReturnId,
                        onSuccess = onReturnAdded,
                        onError = { errorMessage ->
                            // You could show a snackbar here
                        }
                    )
                }
            },
            enabled = isValidAmount && !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSubmitting) {
                LoadingIndicator()
            } else {
                Text("Submit Return")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val result = submissionResult) {
            is Result.Error -> {
                Text(
                    text = "Error: ${result.exception.message ?: "Submission failed"}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            is Result.Success -> {
                Text(
                    text = "Return submitted successfully!",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun formatPercent(value: Double): String {
    return DecimalFormat("#.##").format(value) + "%"
}

private fun formatAmount(value: Double): String {
    return DecimalFormat("#,##0.00").format(value)
}