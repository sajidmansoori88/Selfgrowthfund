package com.selfgrowthfund.sgf.ui.shareholders

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ShareholderFormScreen(
    viewModel: ShareholderViewModel,
    navController: NavController
) {
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

    val submissionResult: com.selfgrowthfund.sgf.utils.Result<Unit>?
            by viewModel.submissionResult.collectAsStateWithLifecycle()

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
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
        )
        fullNameError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
        }

        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text("Mobile Number") },
            isError = mobileError != null,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
        )
        mobileError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
        }

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            isError = addressError != null,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
        )
        addressError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
        }

        OutlinedTextField(
            value = shareBalance,
            onValueChange = { shareBalance = it },
            label = { Text("Share Balance") },
            isError = shareBalanceError != null,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
        )
        shareBalanceError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
        }

        Text("Join Date: ${dateFormatter.format(joinDate)}", modifier = Modifier.padding(vertical = 8.dp))

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

        when (submissionResult) {
            is com.selfgrowthfund.sgf.utils.Result.Success -> {
                Toast.makeText(context, "✅ Shareholder saved!", Toast.LENGTH_SHORT).show()
                resetForm()
                navController.navigate("shareholderList") {
                    popUpTo("shareholderForm") { inclusive = true }
                }
            }
            is com.selfgrowthfund.sgf.utils.Result.Error -> {
                val error = (submissionResult as com.selfgrowthfund.sgf.utils.Result.Error).exception
                Text(
                    "❌ Error: ${error.message ?: "Unknown error"}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            com.selfgrowthfund.sgf.utils.Result.Loading -> {
                // Optional loading UI
            }
            null -> {
                // Initial state
            }
        }
    }
}