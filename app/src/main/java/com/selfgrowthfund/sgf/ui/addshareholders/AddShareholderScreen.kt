package com.selfgrowthfund.sgf.ui.addshareholders

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.selfgrowthfund.sgf.data.local.entities.ShareholderEntry
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.ui.components.DatePickerField
import org.threeten.bp.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddShareholderScreen(
    onBack: () -> Unit,
    viewModel: AddShareholderViewModel = hiltViewModel()
) {
    // Form state
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var shareBalance by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf<LocalDate?>(null) }
    var joiningDate by remember { mutableStateOf<LocalDate?>(null) }
    var role by remember { mutableStateOf<MemberRole?>(null) }

    // Dropdown state
    var expanded by remember { mutableStateOf(false) }

    // Validation errors
    var errors by remember { mutableStateOf(mapOf<String, String>()) }

    // ViewModel state
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    // Handle success navigation
    LaunchedEffect(saveSuccess) {
        if (saveSuccess == true) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add Shareholder") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---- Full Name ----
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = {
                    Row {
                        Text("Full Name")
                        Text("*", color = Color.Red)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = errors["fullName"] != null
            )
            errors["fullName"]?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

            // ---- Email ----
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = {
                    Row {
                        Text("Email")
                        Text("*", color = Color.Red)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = errors["email"] != null
            )
            errors["email"]?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

            // ---- Mobile ----
            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = {
                    Row {
                        Text("Mobile Number")
                        Text("*", color = Color.Red)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = errors["mobileNumber"] != null
            )
            errors["mobileNumber"]?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

            // ---- Address ----
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = {
                    Row {
                        Text("Address")
                        Text("*", color = Color.Red)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = errors["address"] != null
            )
            errors["address"]?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

            // ---- Share Balance ----
            OutlinedTextField(
                value = shareBalance,
                onValueChange = { shareBalance = it },
                label = {
                    Row {
                        Text("Share Balance")
                        Text("*", color = Color.Red)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = errors["shareBalance"] != null
            )
            errors["shareBalance"]?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

            // ---- DOB ----
            DatePickerField(
                label = "Date of Birth *",
                date = dob,
                onDateChange = { dob = it },
                modifier = Modifier.fillMaxWidth()
            )
            errors["dob"]?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

            // ---- Joining Date ----
            DatePickerField(
                label = "Joining Date *",
                date = joiningDate,
                onDateChange = { joiningDate = it },
                modifier = Modifier.fillMaxWidth()
            )
            errors["joiningDate"]?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

            // ---- Role ----
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = role?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Row {
                            Text("Role")
                            Text("*", color = Color.Red)
                        }
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    isError = errors["role"] != null
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    MemberRole.entries.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name) },
                            onClick = {
                                role = item
                                expanded = false
                            }
                        )
                    }
                }
            }
            errors["role"]?.let { Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall) }

            // ---- Global error ----
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // ---- Save Button ----
            Button(
                onClick = {
                    // Validation
                    val newErrors = mutableMapOf<String, String>()
                    if (fullName.isBlank()) newErrors["fullName"] = "Full Name is required"
                    if (email.isBlank()) newErrors["email"] = "Email is required"
                    if (mobileNumber.isBlank()) newErrors["mobileNumber"] = "Mobile Number is required"
                    if (address.isBlank()) newErrors["address"] = "Address is required"
                    if (shareBalance.isBlank()) newErrors["shareBalance"] = "Share Balance is required"
                    if (dob == null) newErrors["dob"] = "Date of Birth is required"
                    if (joiningDate == null) newErrors["joiningDate"] = "Joining Date is required"
                    if (role == null) newErrors["role"] = "Role is required"

                    errors = newErrors

                    if (errors.isEmpty()) {
                        val entry = ShareholderEntry(
                            fullName = fullName,
                            email = email,
                            mobileNumber = mobileNumber,
                            address = address,
                            shareBalance = shareBalance.toDoubleOrNull() ?: 0.0,
                            dob = dob,
                            joiningDate = joiningDate,
                            role = role?.name ?: ""
                        )
                        viewModel.addShareholder(entry)
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                    Text("Saving...")
                } else {
                    Text("Save")
                }
            }
        }
    }
}
