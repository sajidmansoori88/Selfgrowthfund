package com.selfgrowthfund.sgf.ui.repayments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.data.local.entities.RepaymentEntry
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.ui.components.DatePickerField
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRepaymentScreen(
    borrowId: String,
    viewModel: RepaymentViewModel = hiltViewModel(),
    onSuccess: () -> Unit,
    modifier: Modifier
) {
    val nextId by viewModel.nextRepaymentId.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submissionResult by viewModel.submissionResult.collectAsState()
    val borrowingDetails by viewModel.borrowingDetails.collectAsState()

    var repaymentDate by remember { mutableStateOf(LocalDate.now()) }
    var principalRepaid by remember { mutableStateOf("") }
    var penaltyPaid by remember { mutableStateOf("") }
    var modeOfPayment by remember { mutableStateOf(PaymentMode.CASH) } // Changed to enum type
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(borrowId) {
        viewModel.fetchNextRepaymentId()
        viewModel.loadBorrowingDetails(borrowId)
    }

    val outstandingBefore = borrowingDetails?.outstandingBefore ?: 0.0
    val shareholderName = borrowingDetails?.shareholderName ?: "Loading..."

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Repayment for: $shareholderName", style = MaterialTheme.typography.titleMedium)
        Text(
            "Outstanding Amount: ₹${"%.2f".format(outstandingBefore)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Next Repayment ID: ${nextId ?: "Loading..."}",
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Use DatePickerField component
        DatePickerField(
            label = "Repayment Date",
            date = repaymentDate,
            onDateChange = { repaymentDate = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = principalRepaid,
            onValueChange = { principalRepaid = it },
            label = { Text("Principal Repaid (₹)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = penaltyPaid,
            onValueChange = { penaltyPaid = it },
            label = { Text("Penalty Paid (₹)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Payment Mode Dropdown - Using enum labels
        var expanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = modeOfPayment.label, // Use enum label instead of name
                    onValueChange = { },
                    label = { Text("Mode of Payment") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    PaymentMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.label) }, // Show user-friendly label
                            onClick = {
                                modeOfPayment = mode
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val principal = principalRepaid.toDoubleOrNull() ?: 0.0
                val penalty = penaltyPaid.toDoubleOrNull() ?: 0.0

                if (principal <= 0) {
                    // Show error - principal must be positive
                    return@Button
                }

                val entry = RepaymentEntry(
                    borrowId = borrowId,
                    shareholderName = shareholderName,
                    repaymentDate = repaymentDate,
                    principalRepaid = principal,
                    penaltyPaid = penalty,
                    modeOfPayment = modeOfPayment, // Use enum directly
                    notes = notes.ifBlank { null },
                    createdBy = "admin"
                )

                viewModel.submitRepayment(
                    entry = entry,
                    onSuccess = onSuccess,
                    onError = { errorMessage ->
                        // Show error message to user (you can add a snackbar here)
                    }
                )
            },
            enabled = !isSubmitting && principalRepaid.isNotBlank() && borrowingDetails != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSubmitting) "Submitting..." else "Submit Repayment")
        }

        submissionResult?.let { result ->
            Spacer(modifier = Modifier.height(8.dp))
            when (result) {
                is com.selfgrowthfund.sgf.utils.Result.Success -> {
                    Text("Repayment submitted successfully!", color = MaterialTheme.colorScheme.primary)
                }
                is com.selfgrowthfund.sgf.utils.Result.Error -> {
                    Text("Error: ${result.exception.message}", color = MaterialTheme.colorScheme.error)
                }
                else -> {}
            }
        }
    }
}