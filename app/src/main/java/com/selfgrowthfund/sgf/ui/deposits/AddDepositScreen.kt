package com.selfgrowthfund.sgf.ui.deposits

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.model.enums.MemberRole
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDepositScreen(
    currentUserRole: MemberRole,
    shareholderId: String,
    shareholderName: String,
    lastDepositId: String?,
    onSaveSuccess: () -> Unit,
    factory: DepositViewModelFactory,
    modifier: Modifier
) {
    val viewModel = remember {
        factory.create(
            role = currentUserRole,
            shareholderId = shareholderId,
            shareholderName = shareholderName,
            lastDepositId = lastDepositId
        )
    }

    val context = LocalContext.current

    // State collection
    val dueMonth by viewModel.dueMonth.collectAsState()
    val paymentDate by viewModel.paymentDate.collectAsState()
    val shareNos by viewModel.shareNos.collectAsState()
    val additionalContribution by viewModel.additionalContribution.collectAsState()
    val penalty by viewModel.penalty.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val paymentStatus by viewModel.paymentStatus.collectAsState()

    // UI state
    val monthOptions = remember { getSelectableMonths() }
    var selectedMonth by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var sharesText by remember { mutableStateOf("") }
    var additionalContributionText by remember { mutableStateOf("") }

    // Validation states
    var isMonthError by remember { mutableStateOf(false) }
    var isDateError by remember { mutableStateOf(false) }
    var isSharesError by remember { mutableStateOf(false) }

    // Form validation
    val isFormValid by derivedStateOf {
        val monthValid = selectedMonth.isNotBlank().also { isMonthError = !it }
        val dateValid = paymentDate.isNotBlank().also { isDateError = !it }
        val sharesValid = sharesText.toIntOrNull()?.let { it > 0 } ?: false.also { isSharesError = !it }
        monthValid && dateValid && sharesValid
    }

    // Initialize text fields
    LaunchedEffect(additionalContribution) {
        additionalContributionText = if (additionalContribution == 0.0) "" else "%.2f".format(additionalContribution)
    }

    // Calculate when inputs change
    LaunchedEffect(dueMonth, paymentDate, shareNos, additionalContribution) {
        viewModel.updateCalculations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Deposit") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Due Month Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedMonth.ifEmpty { "Select Month" },
                    onValueChange = {},
                    readOnly = true,
                    label = { 
                        Text(buildAnnotatedString {
                            append("Due Month ")
                            withStyle(SpanStyle(color = Color.Red)) { append("*") }
                        })
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    isError = isMonthError,
                    supportingText = {
                        if (isMonthError) {
                            Text("Required", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { expanded = true }
                )

ExposedDropdownMenu(
    expanded = expanded,
    onDismissRequest = { expanded = false },
    modifier = Modifier.width(IntrinsicSize.Max)  // Ensures proper width
) {
    monthOptions.forEach { month ->
        DropdownMenuItem(
            text = { 
                Text(
                    text = month,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                ) 
            },
            onClick = {
                selectedMonth = month
                viewModel.setDueMonth(month)
                expanded = false
                // Add any additional state updates here
            },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = MenuItemDefaults.ContentPadding  // Proper padding
        )
    }
}
            }

            // Payment Date Picker
            Surface(
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    PaymentDateField(
                        currentUserRole = currentUserRole,
                        currentValue = paymentDate,
                        isError = isDateError,
                        onDateSelected = { date ->
                            viewModel.setPaymentDate(date)
                        }
                    )
                    if (isDateError) {
                        Text(
                            text = "Required",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            }

            // Shares Field
            OutlinedTextField(
                value = sharesText,
                onValueChange = {
                    sharesText = it
                    viewModel.setShareNos(it.toIntOrNull() ?: 0)
                },
                label = { 
                    Text(buildAnnotatedString {
                        append("Shares ")
                        withStyle(SpanStyle(color = Color.Red)) { append("*") }
                    })
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isSharesError,
                supportingText = {
                    if (isSharesError) {
                        Text(
                            "At least 1 share required", 
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (it.isFocused && sharesText == "0") sharesText = ""
                    }
            )

            // Additional Contribution
            OutlinedTextField(
                value = additionalContributionText,
                onValueChange = {
                    additionalContributionText = it
                    viewModel.setAdditionalContribution(it.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Additional Contribution (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && additionalContributionText == "0.00") {
                            additionalContributionText = ""
                        }
                    }
            )

            // Summary Section
            Surface(
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Summary", style = MaterialTheme.typography.titleSmall)
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Penalty:")
                        Text("₹${"%.2f".format(penalty)}")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total:")
                        Text("₹${"%.2f".format(totalAmount)}")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Status:")
                        Text(
                            text = paymentStatus.ifBlank { "Pending" },
                            color = when (paymentStatus) {
                                "On-time" -> MaterialTheme.colorScheme.primary
                                "Late" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onSaveSuccess) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (!isFormValid) {
                            Toast.makeText(
                                context,
                                "Please fill all required fields",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        viewModel.submitDeposit()
                        Toast.makeText(context, "Deposit saved", Toast.LENGTH_SHORT).show()
                        onSaveSuccess()
                    },
                    enabled = isFormValid,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Deposit")
                }
            }
        }
    }
}

// Update PaymentDateField to support error state
@Composable
fun PaymentDateField(
    currentUserRole: MemberRole,
    currentValue: String,
    isError: Boolean,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val cal = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(currentValue) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val formatted = sdf.format(selectedCal.time)

            val now = Calendar.getInstance()

            if (currentUserRole != MemberRole.MEMBER_ADMIN) {
                val startOfMonth = now.clone() as Calendar
                startOfMonth.set(Calendar.DAY_OF_MONTH, 1)

                val endOfMonth = now.clone() as Calendar
                endOfMonth.set(Calendar.DAY_OF_MONTH, now.getActualMaximum(Calendar.DAY_OF_MONTH))

                if (selectedCal.before(startOfMonth) || selectedCal.after(endOfMonth)) {
                    Toast.makeText(context, "You can select only within current month", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }
            }

            selectedDate = formatted
            onDateSelected(formatted)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            buildAnnotatedString {
                append("Payment Date ")
                withStyle(SpanStyle(color = Color.Red)) { append("*") }
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = { datePickerDialog.show() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isError) MaterialTheme.colorScheme.errorContainer 
                else MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(if (selectedDate.isBlank()) "Select Date" else selectedDate)
        }
    }
}

// ... (keep getSelectableMonths() same)
