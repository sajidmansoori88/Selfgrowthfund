package com.selfgrowthfund.sgf.ui.shareholders

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ShareholderFormScreen(viewModel: ShareholderViewModel) {
    var fullName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var shareBalance by remember { mutableStateOf("") }
    var joinDate by remember { mutableStateOf(Date()) }

    var fullNameError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var shareBalanceError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val submissionResult by viewModel.submissionResult.collectAsStateWithLifecycle()

    fun validate(): Boolean {
        fullNameError = if (fullName.isBlank()) "Required" else null
        mobileError = if (mobile.isBlank()) "Required" else null
        addressError = if (address.isBlank()) "Required" else null
        shareBalanceError = when {
            shareBalance.isBlank() -> "Required"
            shareBalance.toDoubleOrNull() == null -> "Invalid number"
            shareBalance.toDouble() <= 0.0 -> "Must be greater than 0"
            else -> null
        }
        return listOf(fullNameError, mobileError, addressError, shareBalanceError).all { it == null }
    }

    fun resetForm() {
        fullName = ""
        mobile = ""
        address = ""
        shareBalance = ""
        joinDate = Date()
        fullNameError = null
        mobileError = null
        addressError = null
        shareBalanceError = null
    }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            isError = fullNameError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (fullNameError != null) Text(fullNameError!!, color = MaterialTheme.colorScheme.error)

        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text("Mobile Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = mobileError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (mobileError != null) Text(mobileError!!, color = MaterialTheme.colorScheme.error)

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            isError = addressError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (addressError != null) Text(addressError!!, color = MaterialTheme.colorScheme.error)

        OutlinedTextField(
            value = shareBalance,
            onValueChange = { shareBalance = it },
            label = { Text("Share Balance") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = shareBalanceError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (shareBalanceError != null) Text(shareBalanceError!!, color = MaterialTheme.colorScheme.error)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Join Date: ${dateFormatter.format(joinDate)}")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            val calendar = Calendar.getInstance().apply { time = joinDate }
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)
                    joinDate = selectedCalendar.time
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }) {
            Text("Select Join Date")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (validate()) {
                viewModel.submitShareholder(
                    fullName = fullName,
                    mobileNumber = mobile,
                    address = address,
                    shareBalanceInput = shareBalance,
                    joinDate = joinDate
                )
            }
        }) {
            Text("Submit")
        }

        submissionResult?.let {
            if (it.isSuccess) {
                Text("✅ Shareholder saved successfully!", color = MaterialTheme.colorScheme.primary)
                resetForm()
            } else {
                Text("❌ Error: ${it.exceptionOrNull()?.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}