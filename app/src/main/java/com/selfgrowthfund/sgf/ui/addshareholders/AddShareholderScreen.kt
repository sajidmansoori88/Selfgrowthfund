package com.selfgrowthfund.sgf.ui.addshareholders

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.selfgrowthfund.sgf.data.local.entities.ShareholderEntry
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddShareholderScreen(
    viewModel: AddShareholderViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // Form state
    var fullName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var shareBalance by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf(Date()) }
    var joinDate by remember { mutableStateOf(Date()) }
    var role by remember { mutableStateOf(MemberRole.MEMBER) }

    // Error state
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var shareBalanceError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        fullNameError = if (fullName.isBlank()) "Required" else null
        mobileError = if (mobile.length != 10) "Enter valid 10-digit number" else null
        emailError = if (!email.contains("@")) "Invalid email" else null
        addressError = if (address.isBlank()) "Required" else null
        shareBalanceError = when {
            shareBalance.isBlank() -> "Required"
            shareBalance.toDoubleOrNull() == null -> "Invalid number"
            shareBalance.toDouble() <= 0.0 -> "Must be greater than 0"
            else -> null
        }
        return listOf(fullNameError, mobileError, emailError, addressError, shareBalanceError).all { it == null }
    }

    fun resetForm() {
        fullName = ""
        mobile = ""
        email = ""
        address = ""
        shareBalance = ""
        dob = Date()
        joinDate = Date()
        role = MemberRole.MEMBER
        fullNameError = null
        mobileError = null
        emailError = null
        addressError = null
        shareBalanceError = null
    }

    val userSession: UserSessionViewModel = hiltViewModel()
    val currentUser = userSession.currentUser.value

    if (currentUser.role != MemberRole.MEMBER_ADMIN) {
        Text("Access Denied: Only admins can add shareholders")
        return
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
        fullNameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text("Mobile Number") },
            isError = mobileError != null,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
        )
        mobileError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            isError = emailError != null,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            isError = addressError != null,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
        )
        addressError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        OutlinedTextField(
            value = shareBalance,
            onValueChange = { shareBalance = it },
            label = { Text("Share Balance") },
            isError = shareBalanceError != null,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
        )
        shareBalanceError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Text("Date of Birth: ${dateFormatter.format(dob)}", modifier = Modifier.padding(vertical = 8.dp))
        Button(onClick = {
            val cal = Calendar.getInstance().apply { time = dob }
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    dob = Calendar.getInstance().apply {
                        set(year, month, day)
                    }.time
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }) {
            Text("Select DOB")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Joining Date: ${dateFormatter.format(joinDate)}", modifier = Modifier.padding(vertical = 8.dp))
        Button(onClick = {
            val cal = Calendar.getInstance().apply { time = joinDate }
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    joinDate = Calendar.getInstance().apply {
                        set(year, month, day)
                    }.time
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }) {
            Text("Select Joining Date")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Role: ${role.name}", modifier = Modifier.padding(vertical = 8.dp))
        Button(onClick = {
            role = if (role == MemberRole.MEMBER) MemberRole.MEMBER_ADMIN else MemberRole.MEMBER
        }) {
            Text("Toggle Role")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (validate()) {
                viewModel.addShareholder(
                    ShareholderEntry(
                        fullName = fullName,
                        mobileNumber = mobile,
                        email = email,
                        dob = dob,
                        address = address,
                        shareBalance = shareBalance.toDouble(),
                        joiningDate = joinDate,
                        role = role
                    )
                ) { success, errorMessage ->
                    if (success) {
                        Toast.makeText(context, "✅ Shareholder saved!", Toast.LENGTH_SHORT).show()
                        resetForm()
                        navController.navigate("shareholderList") {
                            popUpTo("addShareholder") { inclusive = true }
                        }
                    } else {
                        Toast.makeText(context, "❌ Error: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }) {
            Text("Submit")
        }
    }
}