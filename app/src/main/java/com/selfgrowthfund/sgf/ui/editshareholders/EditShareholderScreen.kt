package com.selfgrowthfund.sgf.ui.editshareholders

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

@Composable
fun EditShareholderScreen(
    shareholderId: String,
    viewModel: EditShareholderViewModel = hiltViewModel(),
    userSession: UserSessionViewModel = hiltViewModel(),
    onDone: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val currentUser by userSession.currentUser.collectAsState()
    val context = LocalContext.current
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    if (currentUser.role != MemberRole.MEMBER_ADMIN) {
        Text("Access Denied: Only admins can edit shareholders", modifier = Modifier.padding(16.dp))
        return
    }

    LaunchedEffect(shareholderId) {
        viewModel.load(shareholderId)
    }

    if (state.success) {
        Toast.makeText(context, "✅ Changes saved", Toast.LENGTH_SHORT).show()
        onDone()
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            return@Column
        }

        state.errorMessage?.let {
            Text("❌ Error: $it", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        TextField(
            value = state.name,
            onValueChange = viewModel::updateName,
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.mobile,
            onValueChange = viewModel::updateMobile,
            label = { Text("Mobile Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.email,
            onValueChange = viewModel::updateEmail,
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Date of Birth: ${state.dob.format(dateFormatter)}")
        Button(onClick = {
            val dob = state.dob
            DatePickerDialog(
                context,
                { _, y, m, d -> viewModel.updateDob(LocalDate.of(y, m + 1, d)) },
                dob.year,
                dob.monthValue - 1,
                dob.dayOfMonth
            ).show()
        }) {
            Text("Select DOB")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.address,
            onValueChange = viewModel::updateAddress,
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.shareBalance,
            onValueChange = viewModel::updateShareBalance,
            label = { Text("Share Balance") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Joining Date: ${state.joinDate.format(dateFormatter)}")
        Button(onClick = {
            val joinDate = state.joinDate
            DatePickerDialog(
                context,
                { _, y, m, d -> viewModel.updateJoinDate(LocalDate.of(y, m + 1, d)) },
                joinDate.year,
                joinDate.monthValue - 1,
                joinDate.dayOfMonth
            ).show()
        }) {
            Text("Select Joining Date")
        }

        Spacer(modifier = Modifier.height(8.dp))

        RoleDropdown(
            selectedRole = state.role,
            onRoleSelected = viewModel::updateRole
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.save(shareholderId) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }
    }
}

@Composable
fun RoleDropdown(
    selectedRole: MemberRole,
    onRoleSelected: (MemberRole) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedRole.name)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MemberRole.entries.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role.name) },
                    onClick = {
                        onRoleSelected(role)
                        expanded = false
                    }
                )
            }
        }
    }
}