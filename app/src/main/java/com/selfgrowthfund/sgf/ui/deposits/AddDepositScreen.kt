package com.selfgrowthfund.sgf.ui.deposits

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
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
    var selectedMonth by remember { mutableStateOf("") } // Start with no selection
    var expanded by remember { mutableStateOf(false) }
    var sharesText by remember { mutableStateOf("") }
    var additionalContributionText by remember { mutableStateOf("") }

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
                    label = { Text("Due Month") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
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
                    onDismissRequest = { expanded = false }
                ) {
                    monthOptions.forEach { month ->
                        DropdownMenuItem(
                            text = { Text(month) },
                            onClick = {
                                selectedMonth = month
                                viewModel.setDueMonth(month)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Payment Date Picker
            PaymentDateField(
                currentUserRole = currentUserRole,
                currentValue = paymentDate,
                onDateSelected = { date ->
                    viewModel.setPaymentDate(date)
                }
            )

            // Shares Field
            OutlinedTextField(
                value = sharesText,
                onValueChange = {
                    sharesText = it
                    viewModel.setShareNos(it.toIntOrNull() ?: 0)
                },
                label = { Text("Shares") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Penalty: ₹${"%.2f".format(penalty)}")
                Text("Total: ₹${"%.2f".format(totalAmount)}")
                Text(
                    text = "Status: ${paymentStatus.ifBlank { "Pending" }}",
                    color = when (paymentStatus) {
                        "On-time" -> MaterialTheme.colorScheme.primary
                        "Late" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
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
                        if (selectedMonth.isBlank()) {
                            Toast.makeText(context, "Please select a due month", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.submitDeposit()
                        Toast.makeText(context, "Deposit saved", Toast.LENGTH_SHORT).show()
                        onSaveSuccess()
                    }
                ) {
                    Text("Save Deposit")
                }
            }
        }
    }
}

fun getSelectableMonths(): List<String> {
    val formatter = SimpleDateFormat("MMM-yyyy", Locale.getDefault())
    val cal = Calendar.getInstance()
    val months = mutableListOf<String>()

    cal.add(Calendar.MONTH, -3)
    repeat(5) {
        months.add(formatter.format(cal.time))
        cal.add(Calendar.MONTH, 1)
    }

    return months
}

@Composable
fun PaymentDateField(
    currentUserRole: MemberRole,
    currentValue: String,
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text("Payment Date", style = MaterialTheme.typography.labelMedium)
        Button(
            onClick = { datePickerDialog.show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (selectedDate.isBlank()) "Select Date" else selectedDate)
        }
    }
}