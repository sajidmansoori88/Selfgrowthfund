package com.selfgrowthfund.sgf.ui.admin.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.model.enums.MemberRole

@Composable
fun ShareholderTableRow(
    shareholder: Shareholder,
    onModify: (Shareholder) -> Unit,
    onDelete: (Shareholder) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember(shareholder) { mutableStateOf(shareholder.fullName) }
    var editedRole by remember(shareholder) { mutableStateOf(shareholder.role) }

    // NEW: for confirmation dialog
    var showConfirmDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable { /* Optional: row click */ },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ ID column
        Text(
            text = shareholder.shareholderId,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // ✅ Name column
        if (isEditing) {
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                modifier = Modifier.weight(2f),
                textStyle = MaterialTheme.typography.bodyMedium,
                singleLine = true,
                label = { Text("Name") }
            )
        } else {
            Text(
                text = shareholder.fullName,
                modifier = Modifier.weight(2f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // ✅ Role column
        if (isEditing) {
            RoleDropdown(
                selectedRole = editedRole,
                onRoleSelected = { editedRole = it },
                modifier = Modifier.weight(1.5f)
            )
        } else {
            Text(
                text = shareholder.role.label,
                modifier = Modifier.weight(1.5f),
                style = MaterialTheme.typography.bodyMedium,
                color = getRoleColor(shareholder.role),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // ✅ Actions column
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center
        ) {
            if (isEditing) {
                IconButton(
                    onClick = {
                        onModify(
                            shareholder.copy(
                                fullName = editedName,
                                role = editedRole
                            )
                        )
                        isEditing = false
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = {
                        isEditing = false
                        editedName = shareholder.fullName
                        editedRole = shareholder.role
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                IconButton(
                    onClick = { isEditing = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }

                IconButton(
                    onClick = { showConfirmDialog = true }, // open confirmation
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // ✅ Confirmation dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete shareholder '${shareholder.fullName}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onDelete(shareholder)
                    }
                ) { Text("Yes, Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
private fun getRoleColor(role: MemberRole): Color {
    return when (role) {
        MemberRole.MEMBER_ADMIN -> MaterialTheme.colorScheme.primary
        MemberRole.MEMBER_TREASURER -> MaterialTheme.colorScheme.secondary
        MemberRole.MEMBER -> MaterialTheme.colorScheme.onSurface
    }
}
