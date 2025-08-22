package com.selfgrowthfund.sgf.ui.editshareholders

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.utils.DateUtils
import java.time.LocalDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShareholderScreen(
    shareholderId: String,
    onNavigateBack: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: EditShareholderViewModel = hiltViewModel()
) {
    val actualOnNavigateBack = remember(onNavigateBack, onBack) {
        if (onNavigateBack != {}) onNavigateBack else onBack
    }

    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(shareholderId) {
        viewModel.load(shareholderId)
    }

    // ✅ Handle success navigation
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            actualOnNavigateBack()
        }
    }

    // For date pickers
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Shareholder") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Full Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Mobile Number
            OutlinedTextField(
                value = uiState.mobile,
                onValueChange = { input ->
                    if (input.all { it.isDigit() } && input.length <= 10) {
                        viewModel.updateMobile(input)
                    }
                },
                label = { Text("Mobile Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Email
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::updateEmail,
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            // ─────────────── Date of Birth ───────────────
            val dobText = uiState.dob.format(DateUtils.formatterPaymentDate)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dobText,
                    onValueChange = {},
                    label = { Text("Date of Birth") },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Transparent clickable overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            val dialog = DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                    if (selectedDate <= LocalDate.now()) {
                                        viewModel.updateDob(selectedDate)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Cannot select future date",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                currentYear,
                                currentMonth,
                                currentDay
                            )
                            dialog.datePicker.maxDate = System.currentTimeMillis()
                            dialog.show()
                        }
                )
            }


            // Address
            OutlinedTextField(
                value = uiState.address,
                onValueChange = viewModel::updateAddress,
                label = { Text("Address") },
                singleLine = false,
                modifier = Modifier.fillMaxWidth()
            )

            // Share Balance
            OutlinedTextField(
                value = uiState.shareBalance,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        viewModel.updateShareBalance(input)
                    }
                },
                label = { Text("Share Balance") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // ─────────────── Joining Date (NEW FIELD) ───────────────
            val joiningText = uiState.joinDate.format(DateUtils.formatterPaymentDate)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = joiningText,
                    onValueChange = {},
                    label = { Text("Joining Date") },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Transparent clickable overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            val dialog = DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedDate = java.time.LocalDate.of(year, month + 1, dayOfMonth)
                                    if (selectedDate <= java.time.LocalDate.now()) {
                                        viewModel.updateJoinDate(selectedDate)
                                    } else {
                                        Toast.makeText(context, "Cannot select future date", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                currentYear,
                                currentMonth,
                                currentDay
                            )
                            dialog.datePicker.maxDate = System.currentTimeMillis()
                            dialog.show()
                        }
                )
            }

            // ─────────────── Role Dropdown with Transparent Box ───────────────
            var dropdownExpanded by remember { mutableStateOf(false) }
            val roles = MemberRole.entries

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.role.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor() // This is required for ExposedDropdownMenuBox
                    )

                    // Transparent clickable overlay
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { dropdownExpanded = !dropdownExpanded }
                    )
                }

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    roles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role.label) },
                            onClick = {
                                viewModel.updateRole(role)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.save(shareholderId) },
                enabled = uiState.name.isNotBlank() &&
                        uiState.mobile.length == 10 &&
                        uiState.email.isNotBlank() &&
                        uiState.shareBalance.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (uiState.success) {
                Text(
                    text = "Shareholder updated successfully",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}