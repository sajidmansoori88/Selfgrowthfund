package com.selfgrowthfund.sgf.ui.investments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.selfgrowthfund.sgf.model.enums.*
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.utils.Result

@Composable
fun AddInvestmentScreen(
    viewModel: InvestmentViewModel,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 🔹 Get the user session (needed for submitInvestmentWithUser)
    val userSessionViewModel: UserSessionViewModel = hiltViewModel()
    val currentUser by userSessionViewModel.currentUser.collectAsState()

    val state by viewModel.uiState.collectAsState()
    val submissionResult by viewModel.submissionResult.collectAsState()
    val validationError = remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // ----------------- Provisional ID -----------------
        Text(
            "Provisional ID: ${state.provisionalId}",
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ----------------- Application Date (read-only) -----------------
        Text(
            "Application Date: ${state.createdAt}",
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ----------------- Investee Type -----------------
        DropdownMenuBox(
            label = "Investee Type",
            options = InvesteeType.entries,
            selected = state.investeeType,
            onSelected = { newType ->
                viewModel.onInvesteeTypeSelected(newType)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state.investeeType == InvesteeType.Shareholder) {
            // Dropdown of active shareholders
            DropdownMenuBox(
                label = "Shareholder",
                options = state.shareholderList.map { it.second },
                selected = state.investeeName.ifBlank { null },
                onSelected = { name ->
                    val id = state.shareholderList.firstOrNull { it.second == name }?.first ?: ""
                    viewModel.onShareholderSelected(id, name)
                }
            )
        } else {
            OutlinedTextField(
                value = state.investeeName,
                onValueChange = { newValue ->
                    viewModel.updateField { s -> s.copy(investeeName = newValue) }
                },
                label = { Text("Investee Name") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ----------------- Ownership Type -----------------
        DropdownMenuBox(
            label = "Ownership Type",
            options = OwnershipType.entries,
            selected = state.ownershipType,
            onSelected = { newType ->
                viewModel.updateField { s -> s.copy(ownershipType = newType) }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.partnerNames,
            onValueChange = { newValue ->
                viewModel.updateField { s -> s.copy(partnerNames = newValue) }
            },
            label = { Text("Partner Names (comma-separated)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ----------------- Investment Type -----------------
        DropdownMenuBox(
            label = "Investment Type",
            options = InvestmentType.entries,
            selected = state.investmentType,
            onSelected = { newType ->
                viewModel.updateField { s -> s.copy(investmentType = newType) }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.investmentName,
            onValueChange = { newValue ->
                viewModel.updateField { s -> s.copy(investmentName = newValue) }
            },
            label = { Text("Investment Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.amount.toString(),
            onValueChange = { newValue ->
                viewModel.updateField { s -> s.copy(amount = newValue.toDoubleOrNull() ?: 0.0) }
            },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.expectedProfitPercent.toString(),
            onValueChange = { newValue ->
                viewModel.updateField { s -> s.copy(expectedProfitPercent = newValue.toDoubleOrNull() ?: 0.0) }
            },
            label = { Text("Expected Profit (%)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.expectedReturnPeriod.toString(),
            onValueChange = { newValue ->
                viewModel.updateField { s -> s.copy(expectedReturnPeriod = newValue.toIntOrNull() ?: 0) }
            },
            label = { Text("Return Period (days)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.remarks,
            onValueChange = { newValue ->
                viewModel.updateField { s -> s.copy(remarks = newValue) }
            },
            label = { Text("Remarks (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ----------------- Validation Errors -----------------
        validationError.value?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ----------------- Submit Button -----------------
        Button(
            onClick = {
                if (state.amount <= 0.0) {
                    validationError.value = "Amount must be positive"
                    return@Button
                }
                if (state.expectedReturnPeriod <= 0) {
                    validationError.value = "Return period must be positive"
                    return@Button
                }

                validationError.value = null
                viewModel.submitInvestmentWithUser(
                    currentUser = currentUser,
                    onSuccess = onSuccess,
                    onError = { errorMsg -> validationError.value = errorMsg }
                )
            },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isSubmitting) "Submitting..." else "Submit Investment")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ----------------- Submission Result -----------------
        submissionResult?.let {
            when (it) {
                is Result.Success -> {
                    Text(
                        "Investment submitted successfully!",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is Result.Error -> {
                    Text(
                        "Error: ${it.exception.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {}
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun <T> DropdownMenuBox(
    label: String,
    options: List<T>,
    selected: T?,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected?.toString() ?: "",
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // ✅ Full-box clickable overlay (no menuAnchor dependency)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString()) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

