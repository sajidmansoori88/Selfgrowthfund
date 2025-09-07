package com.selfgrowthfund.sgf.ui.investments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.model.enums.*
import com.selfgrowthfund.sgf.ui.components.DatePickerField
import com.selfgrowthfund.sgf.ui.components.DropdownMenuBox
import java.time.LocalDate

// Add this utility function at the top of the file
inline fun <reified T : Enum<T>> safeValueOf(type: String?): T? {
    return try {
        type?.let { enumValueOf<T>(it) }
    } catch (e: IllegalArgumentException) {
        null
    }
}

@Composable
fun AddInvestmentScreen(
    viewModel: InvestmentViewModel,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier // ✅ Add modifier parameter
) {
    val nextId by viewModel.nextInvestmentId.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submissionResult by viewModel.submissionResult.collectAsState()
    val validationError = remember { mutableStateOf<String?>(null) }

    var investeeType by remember { mutableStateOf("") }
    var investeeName by remember { mutableStateOf("") }
    var partnerNames by remember { mutableStateOf("") }

    var investmentDate by remember { mutableStateOf(LocalDate.now()) }
    var investmentName by remember { mutableStateOf("") }

    var amount by remember { mutableStateOf("") }
    var expectedProfitPercent by remember { mutableStateOf("") }
    var expectedReturnPeriod by remember { mutableStateOf("") }
    var returnDueDate by remember { mutableStateOf(LocalDate.now().plusDays(30)) }

    var modeOfPayment by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    var investeeTypeExpanded by remember { mutableStateOf(false) }
    var paymentModeExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    var selectedOwnership by remember { mutableStateOf(OwnershipType.Individual) }
    var selectedInvestmentType by remember { mutableStateOf(InvestmentType.Trading) }
    var otherInvestmentType by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchNextInvestmentId()
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()) // ✅ Make it scrollable
            .padding(16.dp)
    ) {
        Text(
            "Next Investment ID: ${nextId ?: "Loading..."}",
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Investee Type Dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = investeeType,
                onValueChange = { investeeType = it },
                label = { Text("Investee Type") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { investeeTypeExpanded = !investeeTypeExpanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )
            DropdownMenu(
                expanded = investeeTypeExpanded,
                onDismissRequest = { investeeTypeExpanded = false }
            ) {
                InvesteeType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name) },
                        onClick = {
                            investeeType = type.name
                            investeeTypeExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = investeeName,
            onValueChange = { investeeName = it },
            label = { Text("Investee Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenuBox(
            "Ownership Type",
            OwnershipType.entries,
            selectedOwnership
        ) { selectedOwnership = it }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = partnerNames,
            onValueChange = { partnerNames = it },
            label = { Text("Partner Names (comma-separated)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        DatePickerField(
            label = "Investment Date",
            date = investmentDate,
            onDateChange = { investmentDate = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenuBox(
            "Investment Type",
            InvestmentType.entries,
            selectedInvestmentType
        ) { selectedInvestmentType = it }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedInvestmentType == InvestmentType.Other) {
            OutlinedTextField(
                value = otherInvestmentType,
                onValueChange = { otherInvestmentType = it },
                label = { Text("Specify Other Type") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = investmentName,
            onValueChange = { investmentName = it },
            label = { Text("Investment Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = expectedProfitPercent,
            onValueChange = { expectedProfitPercent = it },
            label = { Text("Expected Profit (%)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = expectedReturnPeriod,
            onValueChange = { expectedReturnPeriod = it },
            label = { Text("Return Period (days)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        DatePickerField(
            label = "Return Due Date",
            date = returnDueDate,
            onDateChange = { returnDueDate = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Payment Mode Dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = modeOfPayment,
                onValueChange = { modeOfPayment = it },
                label = { Text("Mode of Payment") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { paymentModeExpanded = !paymentModeExpanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )
            DropdownMenu(
                expanded = paymentModeExpanded,
                onDismissRequest = { paymentModeExpanded = false }
            ) {
                PaymentMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(mode.name) },
                        onClick = {
                            modeOfPayment = mode.name
                            paymentModeExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status Dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = status,
                onValueChange = { status = it },
                label = { Text("Status") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { statusExpanded = !statusExpanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )
            DropdownMenu(
                expanded = statusExpanded,
                onDismissRequest = { statusExpanded = false }
            ) {
                InvestmentStatus.entries.forEach { investmentStatus ->
                    DropdownMenuItem(
                        text = { Text(investmentStatus.name) },
                        onClick = {
                            status = investmentStatus.name
                            statusExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = remarks,
            onValueChange = { remarks = it },
            label = { Text("Remarks (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        validationError.value?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val principal = amount.toDoubleOrNull()
                val profitPercent = expectedProfitPercent.toDoubleOrNull()
                val returnDays = expectedReturnPeriod.toIntOrNull()

                if (principal == null || principal <= 0.0) {
                    validationError.value = "Amount must be a positive number"
                    return@Button
                }
                if (profitPercent == null || profitPercent < 0.0) {
                    validationError.value = "Profit percent must be zero or positive"
                    return@Button
                }
                if (returnDays == null || returnDays <= 0) {
                    validationError.value = "Return period must be a positive number"
                    return@Button
                }

                validationError.value = null

                // Calculate expected profit amount
                val expectedProfitAmount = principal * (profitPercent / 100)

                // Convert string values to enums using safe conversion
                val investeeTypeEnum =
                    safeValueOf<InvesteeType>(investeeType) ?: InvesteeType.External
                val paymentModeEnum =
                    safeValueOf<PaymentMode>(modeOfPayment) ?: PaymentMode.OTHER
                val statusEnum =
                    safeValueOf<InvestmentStatus>(status) ?: InvestmentStatus.Active

                val investment = Investment(
                    investmentId = nextId ?: "",
                    investeeType = investeeTypeEnum,
                    investeeName = investeeName.ifBlank { null },
                    ownershipType = selectedOwnership,
                    partnerNames = if (partnerNames.isNotBlank()) {
                        partnerNames.split(",").map { it.trim() }
                    } else {
                        null
                    },
                    investmentDate = investmentDate,
                    investmentType = if (selectedInvestmentType == InvestmentType.Other && otherInvestmentType.isNotBlank()) {
                        // Handle "Other" investment type
                        // You might need to adjust your entity to handle this case
                        selectedInvestmentType
                    } else {
                        selectedInvestmentType
                    },
                    investmentName = investmentName,
                    amount = principal,
                    expectedProfitPercent = profitPercent,
                    expectedProfitAmount = expectedProfitAmount,
                    expectedReturnPeriod = returnDays,
                    returnDueDate = returnDueDate,
                    modeOfPayment = paymentModeEnum,
                    status = statusEnum,
                    remarks = remarks.ifBlank { null }
                )

                viewModel.submitInvestment(investment, onSuccess) {
                    validationError.value = it
                }
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSubmitting) "Submitting..." else "Submit Investment")
        }

        Spacer(modifier = Modifier.height(8.dp))

        submissionResult?.let {
            when (it) {
                is com.selfgrowthfund.sgf.utils.Result.Success -> {
                    Text(
                        "Investment submitted successfully!",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is com.selfgrowthfund.sgf.utils.Result.Error -> {
                    Text(
                        "Error: ${it.exception.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {}
            }
        }

        // ✅ Add bottom spacer to ensure content isn't cut off
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun <T : Enum<T>> DropdownMenuBox(
    label: String,
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected.toString(),
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
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