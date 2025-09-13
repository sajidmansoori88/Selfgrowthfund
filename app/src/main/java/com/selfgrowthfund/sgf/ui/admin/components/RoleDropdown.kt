package com.selfgrowthfund.sgf.ui.admin.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.selfgrowthfund.sgf.model.enums.MemberRole

@Composable
fun RoleDropdown(
    selectedRole: MemberRole,
    onRoleSelected: (MemberRole) -> Unit,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
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