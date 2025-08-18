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

    // Date Picker Dialog
    val datePickerDialog = remember {
        DatePickerDialog(
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
                viewModel.setPaymentDate(formatted)
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
    }

    // Form validation
    val isFormValid by remember {
        derivedStateOf {
            val monthValid = selectedMonth.isNotBlank().also { isMonthError = !it }
            val dateValid = paymentDate.isNotBlank().also { isDateError = !it }
            val sharesValid = sharesText.toIntOrNull()?.let { it > 0 } ?: false.also { isSharesError = !it }
            monthValid && dateValid && sharesValid
        }
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
            // Month selection dropdown
            var expanded by remember { mutableStateOf(false) }
            val monthOptions = remember { getSelectableMonths() }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedMonth,
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text(buildAnnotatedString {
                            append("Due Month ")
                            withStyle(SpanStyle(color = Color.Red)) { append("*") }
                        })
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .clickable { expanded = !expanded },
                    shape = MaterialTheme.shapes.medium
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
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        buildAnnotatedString {
                            append("Payment Date ")
                            withStyle(SpanStyle(color = Color.Red)) { append("*") }
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.wrapContentWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (paymentDate.isBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (paymentDate.isBlank())
                                MaterialTheme.colorScheme.outline
                            else
                                Color.Transparent
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(paymentDate.ifBlank { "Select Date" })
                    }
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

            // Share Numbers Field
            OutlinedTextField(
                value = sharesText,
                onValueChange = {
                    sharesText = it
                    viewModel.setShareNos(it.toIntOrNull() ?: 0)
                },
                label = {
                    Text(buildAnnotatedString {
                        append("Share Numbers ")
                        withStyle(SpanStyle(color = Color.Red)) { append("*") }
                    })
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
                supportingText = {
                    if (isSharesError) {
                        Text(
                            "At least 1 share required",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium // ✅ consistent rounding
            )
/// Additional Contribution
            OutlinedTextField(
                value = additionalContributionText,
                onValueChange = {
                    additionalContributionText = it
                    viewModel.setAdditionalContribution(it.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Additional Contribution (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && additionalContributionText == "0.00") {
                            additionalContributionText = ""
                        }
                    },
                shape = MaterialTheme.shapes.medium // ✅ consistent rounding
            )
            // Summary Section
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Payment Summary:", style = MaterialTheme.typography.titleSmall)
                    HorizontalDivider()
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
                Button(
                    onClick = onSaveSuccess,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(16.dp))
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
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormValid)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Text("Save Deposit")
                }
            }
        }
    }
}

private fun getSelectableMonths(): List<String> {
    val formatter = SimpleDateFormat("MMM-yyyy", Locale.getDefault())
    val cal = Calendar.getInstance()
    val months = mutableListOf<String>()

    cal.add(Calendar.MONTH, -3)
    repeat(6) {
        months.add(formatter.format(cal.time))
        cal.add(Calendar.MONTH, 1)
    }

    return months
}