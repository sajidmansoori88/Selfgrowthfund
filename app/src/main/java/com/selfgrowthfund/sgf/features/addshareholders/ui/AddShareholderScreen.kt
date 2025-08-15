package com.selfgrowthfund.sgf.features.addshareholders.ui

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.features.addshareholders.ui.presentation.AddShareholderViewModel
import com.selfgrowthfund.sgf.features.addshareholders.ui.domain.ShareholderInput
import com.selfgrowthfund.sgf.model.enums.MemberRole
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddShareholderScreen(
    viewModel: AddShareholderViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf<LocalDate?>(null) }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var joinDate by remember { mutableStateOf<LocalDate?>(null) }
    var role by remember { mutableStateOf(MemberRole.MEMBER) }

    val roles = MemberRole.entries
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val isValid = name.isNotBlank() && email.contains("@") && mobile.length >= 10

    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add Shareholder", style = MaterialTheme.typography.titleLarge)

                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Shareholder Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))
                    DatePickerField("Date of Birth", dob) { dob = it }

                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("Mobile Number") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))
                    DatePickerField("Join Date", joinDate) { joinDate = it }

                    Spacer(Modifier.height(8.dp))
                    RoleDropdown(selected = role, options = roles) { selectedRole ->
                        role = selectedRole
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (!isValid || dob == null || joinDate == null) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please fill all fields correctly.")
                                }
                                return@Button
                            }

                            val input = ShareholderInput(
                                name = name,
                                dateOfBirth = dob!!,         // ✅ LocalDate
                                mobileNumber = mobile,
                                email = email,
                                joiningDate = joinDate!!,    // ✅ LocalDate
                                role = role.name
                            )


                            viewModel.addShareholder(input) { success, error ->
                                coroutineScope.launch {
                                    val msg = if (success) {
                                        name = ""
                                        dob = null
                                        mobile = ""
                                        email = ""
                                        joinDate = null
                                        role = MemberRole.MEMBER
                                        "Shareholder added!"
                                    } else {
                                        "Error: $error"
                                    }
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        },
                        enabled = isValid && dob != null && joinDate != null
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

@Composable
fun RoleDropdown(
    selected: MemberRole,
    options: List<MemberRole>,
    onSelectedChange: (MemberRole) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            label = { Text("Role") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role.label) },
                    onClick = {
                        onSelectedChange(role)
                        expanded = false
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePickerField(label: String, date: LocalDate?, onDateChange: (LocalDate) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateChange(LocalDate.of(year, month + 1, dayOfMonth))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    OutlinedTextField(
        value = date?.toString() ?: "",
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() }
    )
}