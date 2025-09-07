package com.selfgrowthfund.sgf.ui.repayments

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.selfgrowthfund.sgf.data.local.entities.Repayment
import com.selfgrowthfund.sgf.data.local.entities.RepaymentEntry
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.ui.components.SGFScaffoldWrapper
import com.selfgrowthfund.sgf.ui.navigation.DrawerContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun AddRepaymentScreen(
    borrowId: String,
    shareholderName: String,
    outstandingBefore: Double,
    borrowStartDate: LocalDate,
    dueDate: LocalDate,
    previousRepayments: List<Repayment>,
    viewModel: RepaymentViewModel,
    onSuccess: () -> Unit
) {
    val nextId by viewModel.nextRepaymentId.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submissionResult by viewModel.submissionResult.collectAsState()

    var repaymentDate by remember { mutableStateOf(LocalDate.now()) }
    var principalRepaid by remember { mutableStateOf("") }
    var penaltyPaid by remember { mutableStateOf("") }
    var modeOfPayment by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchNextRepaymentId()
    }

    Column(modifier = Modifier.padding(16.dp)) {
            Text("Next Repayment ID: ${nextId ?: "Loading..."}", style = MaterialTheme.typography.labelMedium)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = principalRepaid,
                onValueChange = { principalRepaid = it },
                label = { Text("Principal Repaid") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = penaltyPaid,
                onValueChange = { penaltyPaid = it },
                label = { Text("Penalty Paid") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = modeOfPayment,
                onValueChange = { modeOfPayment = it },
                label = { Text("Mode of Payment") },
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
                    val entry = RepaymentEntry(
                        borrowId = borrowId,
                        shareholderName = shareholderName,
                        repaymentDate = repaymentDate,
                        principalRepaid = principalRepaid.toDoubleOrNull() ?: 0.0,
                        penaltyPaid = penaltyPaid.toDoubleOrNull() ?: 0.0,
                        modeOfPayment = PaymentMode.valueOf(modeOfPayment),
                        notes = notes.ifBlank { null }
                    )

                    viewModel.submitRepayment(
                        entry = entry,
                        outstandingBefore = outstandingBefore,
                        borrowStartDate = borrowStartDate,
                        dueDate = dueDate,
                        previousRepayments = previousRepayments,
                        onSuccess = onSuccess,
                        onError = { /* Show error snackbar or toast */ }
                    )
                },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSubmitting) "Submitting..." else "Submit Repayment")
            }

            submissionResult?.let {
                when (it) {
                    is com.selfgrowthfund.sgf.utils.Result.Success -> {
                        Text("Repayment submitted successfully!", color = MaterialTheme.colorScheme.primary)
                    }
                    is com.selfgrowthfund.sgf.utils.Result.Error -> {
                        Text("Error: ${it.exception.message}", color = MaterialTheme.colorScheme.error)
                    }
                    else -> {}
                }
            }
        }
    }