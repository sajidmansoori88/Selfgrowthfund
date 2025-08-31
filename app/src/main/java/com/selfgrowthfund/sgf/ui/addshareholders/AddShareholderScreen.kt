package com.selfgrowthfund.sgf.ui.addshareholders

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.model.enums.MemberRole
import java.util.*
import com.selfgrowthfund.sgf.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShareholderScreen(
    viewModel: AddShareholderViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val actualOnNavigateBack = remember(onNavigateBack, onBack) {
        if (onNavigateBack != {}) onNavigateBack else onBack
    }

    // State Flows
    val fullName by viewModel.fullName.collectAsState()
    val mobileNumber by viewModel.mobileNumber.collectAsState()
    val email by viewModel.email.collectAsState()
    val dob by viewModel.dob.collectAsState()
    val address by viewModel.address.collectAsState()
    val joiningDate by viewModel.joiningDate.collectAsState()
    val role by viewModel.role.collectAsState()
    val shareBalance by viewModel.shareBalance.collectAsState()

    val canSave by viewModel.canSave.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

    val nextId by viewModel.nextShareholderId.collectAsState()
    Text("Next Shareholder ID: ${nextId ?: "Loading..."}")

    // Handle success navigation
    LaunchedEffect(saveSuccess) {
        saveSuccess?.let { success ->
            if (success) {
                Toast.makeText(context, "Shareholder added successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetSaveSuccess()
                // Navigate somewhere after success
                onNavigateBack() // ← Go back to previous screen
                // OR navigate to list screen:
                // onNavigateToList() // If you implement this
            }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.resetErrorMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.testFirestoreConnection()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Shareholder",
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // ─────────────── Full Name ───────────────
            OutlinedTextField(
                value = fullName,
                onValueChange = { input ->
                    // Auto-capitalize first letter of each word
                    viewModel.fullName.value = input.split(" ").joinToString(" ") { word ->
                        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    }
                },
                label = {
                    Text(
                        buildAnnotatedString {
                            append("Full Name")
                            withStyle(style = SpanStyle(color = Color.Red)) {
                                append("*")
                            }
                        }
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ─────────────── Mobile Number ───────────────
            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { input ->
                    if (input.all { it.isDigit() } && input.length <= 10) {
                        viewModel.mobileNumber.value = input
                    }
                },
                label = {
                    Text(
                        buildAnnotatedString {
                            append("Mobile Number")
                            withStyle(style = SpanStyle(color = Color.Red)) {
                                append("*")
                            }
                        }
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ─────────────── Email ───────────────
            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.email.value = it },
                label = {
                    Text(
                        buildAnnotatedString {
                            append("Email")
                            withStyle(style = SpanStyle(color = Color.Red)) {
                                append("*")
                            }
                        }
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ─────────────── Date of Birth ───────────────
            val dobText = dob?.format(DateUtils.formatterPaymentDate) ?: "Select Date of Birth"
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dobText,
                    onValueChange = {},
                    label = {
                        Text(
                            buildAnnotatedString {
                                append("Date of Birth")
                                withStyle(style = SpanStyle(color = Color.Red)) {
                                    append("*")
                                }
                            }
                        )
                    },
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
                                        viewModel.dob.value = selectedDate
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

            Spacer(modifier = Modifier.height(8.dp))


            // ─────────────── Address ───────────────
            OutlinedTextField(
                value = address,
                onValueChange = { viewModel.address.value = it },
                label = {
                    Text(
                        buildAnnotatedString {
                            append("Address")
                            withStyle(style = SpanStyle(color = Color.Red)) {
                                append("*")
                            }
                        }
                    )
                },
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ─────────────── Date of Joining ───────────────
            val joiningText = joiningDate?.format(DateUtils.formatterPaymentDate) ?: "Select Joining Date"
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = joiningText,
                    onValueChange = {},
                    label = {
                        Text(
                            buildAnnotatedString {
                                append("Joining Date")
                                withStyle(style = SpanStyle(color = Color.Red)) {
                                    append("*")
                                }
                            }
                        )
                    },
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
                                        viewModel.joiningDate.value = selectedDate
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
            Spacer(modifier = Modifier.height(8.dp))

// ─────────────── Role Dropdown ───────────────
            val roles = MemberRole.entries
            val selectedRoleText = role?.label ?: "Select Role"
            var dropdownExpanded by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedRoleText,
                    onValueChange = {},
                    label = {
                        Text(
                            buildAnnotatedString {
                                append("Role")
                                withStyle(style = SpanStyle(color = Color.Red)) {
                                    append("*")
                                }
                            }
                        )
                    },
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Transparent clickable overlay for the entire dropdown field
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { dropdownExpanded = true }
                )

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    roles.forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r.label) },
                            onClick = {
                                viewModel.role.value = r
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ─────────────── Share Balance ───────────────
            OutlinedTextField(
                value = shareBalance,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) {
                        viewModel.shareBalance.value = input
                    }
                },
                label = { Text("Share Balance") }, // Removed * to make it optional
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ─────────────── Save Button ───────────────
            Button(
                onClick = { viewModel.addShareholder() },
                enabled = canSave && !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save")
                }
            }
        }
        // ADD THESE TEMPORARY BUTTONS AT THE BOTTOM OF YOUR COLUMN:
        Spacer(modifier = Modifier.height(16.dp))

        // Test button to navigate to EditScreen
        Button(
            onClick = { onNavigateToEdit("SH001") }, // Test with hardcoded ID
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("TEST: Go to Edit Screen (SH001)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Test button to navigate back
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
        ) {
            Text("TEST: Go Back")
        }
    }
    }

