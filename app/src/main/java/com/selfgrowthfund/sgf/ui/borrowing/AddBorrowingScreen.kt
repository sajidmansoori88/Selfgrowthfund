package com.selfgrowthfund.sgf.ui.borrowing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.BorrowingEntry
import com.selfgrowthfund.sgf.ui.components.DatePickerField
import java.time.LocalDate

@Composable
fun AddBorrowingScreen(
    shareholderId: String,
    shareholderName: String,
    createdBy: String,
    viewModel: BorrowingViewModel,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nextId by viewModel.nextBorrowId.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submissionResult by viewModel.submissionResult.collectAsState()

    var amountRequested by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(LocalDate.now()) }
    var notes by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchNextBorrowId()
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Next Borrowing ID: ${nextId ?: "Loading..."}",
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Amount Requested Field
        OutlinedTextField(
            value = amountRequested,
            onValueChange = {
                amountRequested = it
                amountError = it.isNotBlank() && it.toDoubleOrNull() == null
            },
            label = { Text("Amount Requested") },
            isError = amountError,
            supportingText = {
                if (amountError) {
                    Text("Enter a valid number", color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // ✅ Fixed import
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Due Date Field
        DatePickerField(
            label = "Due Date",
            date = dueDate,
            onDateChange = { dueDate = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Notes Field
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        Button(
            onClick = {
                val amount = amountRequested.toDoubleOrNull()
                if (amount == null || amount <= 0.0) {
                    amountError = true
                    return@Button
                }

                amountError = false

                val entry = BorrowingEntry(
                    shareholderId = shareholderId,
                    shareholderName = shareholderName,
                    amountRequested = amount,
                    dueDate = dueDate,
                    notes = notes.ifBlank { null },
                    createdBy = createdBy
                )

                viewModel.submitBorrowing(
                    entry = entry,
                    onSuccess = onSuccess,
                    onError = { errorMessage ->
                        // Handle error - you might want to show a snackbar
                    }
                )
            },
            enabled = !isSubmitting && amountRequested.toDoubleOrNull()?.let { it > 0.0 } == true,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Submit Borrowing")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Handle submission results
        submissionResult?.let { result ->
            when (result) {
                is com.selfgrowthfund.sgf.utils.Result.Success -> {
                    Text(
                        "Borrowing submitted successfully!",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is com.selfgrowthfund.sgf.utils.Result.Error -> {
                    Text(
                        "Error: ${result.exception.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {}
            }
        }

        // Bottom spacer
        Spacer(modifier = Modifier.height(32.dp))
    }
}