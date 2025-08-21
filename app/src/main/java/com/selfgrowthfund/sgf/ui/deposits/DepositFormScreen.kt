package com.selfgrowthfund.sgf.ui.deposits

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.utils.Result

@Composable
fun DepositFormScreen(
    viewModel: DepositViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // State bindings
    val dueMonth by viewModel.dueMonth.collectAsState()
    val paymentDate by viewModel.paymentDate.collectAsState()
    val shareNos by viewModel.shareNos.collectAsState()
    val additionalContribution by viewModel.additionalContribution.collectAsState()
    val penalty by viewModel.penalty.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val paymentStatus by viewModel.paymentStatus.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val isFormValid by viewModel.isFormValid.collectAsState()

    // Validation errors
    val dueMonthError by viewModel.dueMonthError.collectAsState()
    val paymentDateError by viewModel.paymentDateError.collectAsState()

    // Submission result feedback
    val submissionResult by viewModel.submissionResult.collectAsState()
    LaunchedEffect(submissionResult) {
        if (submissionResult is Result.Success) {
            Toast.makeText(context, "Deposit submitted!", Toast.LENGTH_SHORT).show()
            viewModel.clearForm()
        }
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {

        Text("Deposit Form", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = dueMonth,
            onValueChange = { viewModel.setDueMonth(it) },
            label = { Text("Due Month (e.g., Aug-2025)") },
            isError = dueMonthError != null,
            supportingText = {
                dueMonthError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = paymentDate,
            onValueChange = { viewModel.setPaymentDate(it) },
            label = { Text("Payment Date (dd-MM-yyyy)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = paymentDateError != null,
            supportingText = {
                paymentDateError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = shareNos.toString(),
            onValueChange = {
                it.toIntOrNull()?.let { num -> viewModel.setShareNos(num) }
            },
            label = { Text("Number of Shares") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = additionalContribution.toString(),
            onValueChange = {
                it.toDoubleOrNull()?.let { amt -> viewModel.setAdditionalContribution(amt) }
            },
            label = { Text("Additional Contribution") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.updateCalculations() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate")
        }

        Spacer(Modifier.height(16.dp))

        Text("Penalty: ₹%.2f".format(penalty))
        Text("Total Amount: ₹%.2f".format(totalAmount))
        Text("Payment Status: $paymentStatus")

        Spacer(Modifier.height(24.dp))

        if (isSubmitting) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Button(
                onClick = { viewModel.submitDeposit() },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Deposit")
            }
        }
    }
}