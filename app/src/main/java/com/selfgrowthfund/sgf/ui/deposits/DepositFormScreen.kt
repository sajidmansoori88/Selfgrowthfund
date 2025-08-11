package com.selfgrowthfund.sgf.ui.deposits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DepositFormScreen(
    viewModel: DepositViewModel = viewModel()
) {
    val dueMonth by viewModel.dueMonth.collectAsState()
    val paymentDate by viewModel.paymentDate.collectAsState()
    val shareNos by viewModel.shareNos.collectAsState()
    val additionalContribution by viewModel.additionalContribution.collectAsState()

    val penalty by viewModel.penalty.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val paymentStatus by viewModel.paymentStatus.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Deposit Form", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = dueMonth,
            onValueChange = { viewModel.dueMonth.value = it },
            label = { Text("Due Month (e.g., Aug-2025)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = paymentDate,
            onValueChange = { viewModel.paymentDate.value = it },
            label = { Text("Payment Date (ddMMyyyy)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = shareNos.toString(),
            onValueChange = {
                it.toIntOrNull()?.let { num -> viewModel.shareNos.value = num }
            },
            label = { Text("Number of Shares") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = additionalContribution.toString(),
            onValueChange = {
                it.toDoubleOrNull()?.let { amt -> viewModel.additionalContribution.value = amt }
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

        Button(
            onClick = { viewModel.submitDeposit() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Deposit")
        }
    }
}