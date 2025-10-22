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
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyBorrowingScreen(
    shareholderId: String,
    shareholderName: String,
    createdBy: String,
    viewModel: BorrowingViewModel,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    // --- State Observers ---
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submissionResult by viewModel.submissionResult.collectAsState()
    val eligibility by viewModel.eligibility.collectAsState()

    // --- Local UI State ---
    var amountRequested by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }

    // --- Snackbar + Coroutine Scope ---
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // --- Snackbar Feedback Effect ---
    LaunchedEffect(submissionResult) {
        when (val result = submissionResult) {
            is Result.Success -> {
                snackbarHostState.showSnackbar("Borrowing submitted successfully ✅")
                onSuccess()  // ✅ triggers popBackStack() in NavGraph
                viewModel.clearSubmissionState()
            }
            is Result.Error -> {
                snackbarHostState.showSnackbar(
                    result.exception.message ?: "Error submitting borrowing"
                )
                viewModel.clearSubmissionState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Eligibility (read-only) — optional static display
            OutlinedTextField(
                value = if (eligibility > 0) "₹${String.format("%.2f", eligibility)}" else "Calculating...",
                onValueChange = {},
                label = { Text("Eligibility (90% of Share money)") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                        borrowEligibility = eligibility,
                        approvedAmount = 0.0,
                        borrowStartDate = LocalDate.now(),
                        createdBy = createdBy,
                        notes = notes.ifBlank { null }
                    )

                    viewModel.applyBorrowing(
                        entry = entry,
                        onSuccess = {},
                        onError = { errorMessage ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(errorMessage)
                            }
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
                    Text("Submit Application")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Optional "Loading" text
            if (submissionResult is Result.Loading) {
                Text(
                    "Submitting application...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
