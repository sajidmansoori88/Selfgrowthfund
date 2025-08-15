package com.selfgrowthfund.sgf.features.editshareholders

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.selfgrowthfund.sgf.model.enums.MemberRole

@Composable
fun MainScreen() {
    EditShareholderScreen(
        shareholderId = "SH001",
        viewModel = viewModel(),
        onDone = { /* navigate or show toast */ }
    )
}

@Composable
fun EditShareholderScreen(
    shareholderId: String,
    viewModel: EditShareholderViewModel,
    onDone: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(shareholderId) {
        viewModel.load(shareholderId)
    }

    if (state.success) {
        onDone()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = state.name,
            onValueChange = viewModel::updateName,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

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