package com.selfgrowthfund.sgf.ui.borrowing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.BorrowingEntry
import com.selfgrowthfund.sgf.model.enums.BorrowingStatus
import com.selfgrowthfund.sgf.ui.components.DatePickerField
import java.time.LocalDate

@Composable
fun AddBorrowingScreen(
    shareholderId: String,
    shareholderName: String,
    createdBy: String,
    viewModel: BorrowingViewModel,
    onSuccess: () -> Unit
) {
    val nextId by viewModel.nextBorrowId.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submissionResult by viewModel.submissionResult.collectAsState()

    var amountRequested by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(LocalDate.now()) }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchNextBorrowId()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Next Borrowing ID: ${nextId ?: "Loading..."}", style = MaterialTheme.typography.labelMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amountRequested,
            onValueChange = { amountRequested = it },
            label = { Text("Amount Requested") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        DatePickerField(
            label = "Due Date",
            date = dueDate,
            onDateChange = { dueDate = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val entry = BorrowingEntry(
                    shareholderId = shareholderId,
                    shareholderName = shareholderName,
                    amountRequested = amountRequested.toDoubleOrNull() ?: 0.0,
                    dueDate = dueDate,
                    notes = notes.ifBlank { null },
                    createdBy = createdBy
                )

                viewModel.submitBorrowing(
                    entry = entry,
                    onSuccess = onSuccess,
                    onError = { /* Show error snackbar or toast */ }
                )
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSubmitting) "Submitting..." else "Submit Borrowing")
        }

        submissionResult?.let {
            when (it) {
                is com.selfgrowthfund.sgf.utils.Result.Success -> {
                    Text("Borrowing submitted successfully!", color = MaterialTheme.colorScheme.primary)
                }
                is com.selfgrowthfund.sgf.utils.Result.Error -> {
                    Text("Error: ${it.exception.message}", color = MaterialTheme.colorScheme.error)
                }
                else -> {}
            }
        }
    }
}