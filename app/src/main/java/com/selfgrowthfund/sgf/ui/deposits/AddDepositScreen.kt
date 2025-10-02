package com.selfgrowthfund.sgf.ui.deposits

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.model.enums.PaymentStatus
import com.selfgrowthfund.sgf.ui.theme.GradientBackground
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDepositScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onSaveSuccess: () -> Unit
) {
    val viewModel: DepositViewModel = hiltViewModel()
    val context = LocalContext.current

    val isSubmitting by viewModel.isSubmitting.collectAsState()

    // States from ViewModel
    val dueMonth by viewModel.dueMonth.collectAsState()
    val paymentDate by viewModel.paymentDate.collectAsState()
    val shareNos by viewModel.shareNos.collectAsState()
    val additionalContribution by viewModel.additionalContribution.collectAsState()
    val penalty by viewModel.penalty.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val paymentStatus by viewModel.paymentStatus.collectAsState()

    // UI-only states
    val monthOptions = remember { getSelectableMonths() }
    var selectedMonth by remember { mutableStateOf("") }
    var expandedMonth by remember { mutableStateOf(false) }
    var sharesText by remember { mutableStateOf("") }
    var additionalContributionText by remember { mutableStateOf("") }

    // Mode of Payment using Enum
    val paymentModes = remember { PaymentMode.getAllLabels() }
    var selectedPaymentMode by remember { mutableStateOf("") }
    var expandedPayment by remember { mutableStateOf(false) }

    // Validation states
    var isMonthError by remember { mutableStateOf(false) }
    var isDateError by remember { mutableStateOf(false) }
    var isSharesError by remember { mutableStateOf(false) }
    var isModeError by remember { mutableStateOf(false) }

    // Date Picker Dialog
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val formatted = sdf.format(selectedCal.time)

            // Only current month for non-admins
            if (viewModel.role != MemberRole.MEMBER_ADMIN) {
                val now = Calendar.getInstance()
                val startOfMonth = now.clone() as Calendar
                startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
                val endOfMonth = now.clone() as Calendar
                endOfMonth.set(Calendar.DAY_OF_MONTH, now.getActualMaximum(Calendar.DAY_OF_MONTH))
                if (selectedCal.before(startOfMonth) || selectedCal.after(endOfMonth)) {
                    Toast.makeText(
                        context,
                        "You can select only within current month",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@DatePickerDialog
                }
            }
            viewModel.setPaymentDate(formatted)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Form validation
    val isFormValid by remember {
        derivedStateOf {
            val monthValid = selectedMonth.isNotBlank()
            val dateValid = paymentDate.isNotBlank()
            val sharesValid = sharesText.toIntOrNull()?.let { it > 0 } ?: false
            val modeValid = selectedPaymentMode.isNotBlank()

            // update error flags
            isMonthError = !monthValid
            isDateError = !dateValid
            isSharesError = !sharesValid
            isModeError = !modeValid

            monthValid && dateValid && sharesValid && modeValid
        }
    }

    LaunchedEffect(additionalContribution) {
        additionalContributionText =
            if (additionalContribution == 0.0) "" else "%.2f".format(additionalContribution)
    }

    LaunchedEffect(dueMonth, paymentDate, shareNos, additionalContribution) {
        viewModel.updateCalculations()
    }
    GradientBackground {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState()) // ✅ Make it scrollable
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Row: Due Month & Payment Date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Due Month
                Column(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedMonth,
                        onExpandedChange = { expandedMonth = it }
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
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedMonth) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMonth,
                            onDismissRequest = { expandedMonth = false }
                        ) {
                            monthOptions.forEach { month ->
                                DropdownMenuItem(
                                    text = { Text(month) },
                                    onClick = {
                                        selectedMonth = month
                                        viewModel.setDueMonth(month)
                                        expandedMonth = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Payment Date
                Column(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = paymentDate,
                            onValueChange = {},
                            readOnly = true,
                            enabled = true,
                            label = {
                                Text(buildAnnotatedString {
                                    append("Payment Date ")
                                    withStyle(SpanStyle(color = Color.Red)) { append("*") }
                                })
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = MaterialTheme.shapes.medium,
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { datePickerDialog.show() }
                        )
                    }
                }
            }

            // Share Numbers
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = MaterialTheme.shapes.medium
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
                        if (!focusState.isFocused) {
                            additionalContributionText =
                                additionalContributionText.toDoubleOrNull()
                                    ?.let { "%.2f".format(it) } ?: ""
                        }
                    }
                    .padding(bottom = 12.dp),
                shape = MaterialTheme.shapes.medium
            )

            // Mode of Payment dropdown
            Column(modifier = Modifier.padding(bottom = 20.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedPayment,
                    onExpandedChange = { expandedPayment = it }
                ) {
                    OutlinedTextField(
                        value = selectedPaymentMode,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(buildAnnotatedString {
                                append("Mode of Payment ")
                                withStyle(SpanStyle(color = Color.Red)) { append("*") }
                            })
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedPayment) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPayment,
                        onDismissRequest = { expandedPayment = false }
                    ) {
                        PaymentMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.label) },
                                onClick = {
                                    selectedPaymentMode = mode.label
                                    viewModel.setModeOfPayment(mode)
                                    expandedPayment = false
                                    isModeError = false
                                }
                            )
                        }
                    }
                }
            }

            // Payment Summary Card
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondary,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
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
                        val statusEnum = paymentStatus
                        Text(
                            text = statusEnum.label,
                            color = when (statusEnum) {
                                PaymentStatus.ON_TIME -> MaterialTheme.colorScheme.primary
                                PaymentStatus.EARLY -> MaterialTheme.colorScheme.tertiary
                                PaymentStatus.LATE -> MaterialTheme.colorScheme.error
                                PaymentStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
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
                        viewModel.submitDeposit(
                            notes = null,
                            onSuccess = {
                                Toast.makeText(context, "Deposit saved", Toast.LENGTH_SHORT).show()
                                onSaveSuccess()
                                navController.popBackStack() // ✅ go back after save
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = isFormValid && !isSubmitting,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormValid) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Deposit")
                    }
                }
            }

            // ✅ Add bottom spacer to ensure content isn't cut off
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

    fun getSelectableMonths(): List<String> {
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