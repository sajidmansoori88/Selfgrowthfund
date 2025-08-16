package com.selfgrowthfund.sgf.ui.deposits

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.model.enums.MemberRole

@Composable
fun AddDepositScreen(
    role: MemberRole,
    shareholderId: String,
    shareholderName: String,
    lastDepositId: String?,
    onBack: () -> Unit,
    factory: DepositViewModelFactory,
    modifier: Modifier = Modifier
) {
    val viewModel = remember {
        factory.create(role, shareholderId, shareholderName, lastDepositId)
    }

    val dueMonth by viewModel.dueMonth.collectAsState()
    val paymentDate by viewModel.paymentDate.collectAsState()
    val shareNos by viewModel.shareNos.collectAsState()
    val additionalContribution by viewModel.additionalContribution.collectAsState()
    val penalty by viewModel.penalty.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val paymentStatus by viewModel.paymentStatus.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        Text("Add Deposit", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Due Month Dropdown
        DueMonthDropdown(
            selectedMonth = dueMonth,
            onMonthSelected = { viewModel.dueMonth.value = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Payment Date Input
        OutlinedTextField(
            value = paymentDate,
            onValueChange = { viewModel.paymentDate.value = it },
            label = { Text("Payment Date (dd-MM-yyyy)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Share Nos
        OutlinedTextField(
            value = shareNos.toString(),
            onValueChange = {
                viewModel.shareNos.value = it.toIntOrNull() ?: 1
            },
            label = { Text("Number of Shares") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Additional Contribution
        OutlinedTextField(
            value = additionalContribution.toString(),
            onValueChange = {
                viewModel.additionalContribution.value = it.toDoubleOrNull() ?: 0.0
            },
            label = { Text("Additional Contribution") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Calculated Fields
        Text("Penalty: ₹%.2f".format(penalty), fontWeight = FontWeight.Medium)
        Text("Total Amount: ₹%.2f".format(totalAmount), fontWeight = FontWeight.Medium)
        Text("Status: $paymentStatus", fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = {
                viewModel.updateCalculations()
                viewModel.submitDeposit()
                onBack()
            }) {
                Text("Submit")
            }

            OutlinedButton(onClick = onBack) {
                Text("Cancel")
            }
        }
    }
}