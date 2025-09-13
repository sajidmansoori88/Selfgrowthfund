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
import com.selfgrowthfund.sgf.model.User
import com.selfgrowthfund.sgf.model.enums.MemberRole

@Composable
fun ShareholderTableRow(
    user: User,
    onModify: (User) -> Unit,
    onDelete: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember(user) { mutableStateOf(user.name) }
    var editedRole by remember(user) { mutableStateOf(user.role) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable { /* Optional: row click */ },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ID Column - Auto width
        Text(
            text = user.id,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Name Column - More space
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
                text = user.name,
                modifier = Modifier.weight(2f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Role Column - Fixed proportional width
        if (isEditing) {
            RoleDropdown(
                selectedRole = editedRole,
                onRoleSelected = { editedRole = it },
                modifier = Modifier.weight(1.5f)
            )
        } else {
            Text(
                text = user.role.label,
                modifier = Modifier.weight(1.5f),
                style = MaterialTheme.typography.bodyMedium,
                color = getRoleColor(user.role), // ✅ Now this function exists
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Actions Column - Fixed width
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center
        ) {
            if (isEditing) {
                // Save button
                IconButton(
                    onClick = {
                        onModify(user.copy(name = editedName, role = editedRole))
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

                // Cancel button
                IconButton(
                    onClick = {
                        isEditing = false
                        editedName = user.name
                        editedRole = user.role
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
                // Edit button
                IconButton(
                    onClick = { isEditing = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }

                // Delete button
                IconButton(
                    onClick = { onDelete(user) },
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
}

// ✅ Add this missing function
@Composable
private fun getRoleColor(role: MemberRole): Color {
    return when (role) {
        MemberRole.MEMBER_ADMIN -> MaterialTheme.colorScheme.primary
        MemberRole.MEMBER_TREASURER -> MaterialTheme.colorScheme.secondary
        MemberRole.MEMBER -> MaterialTheme.colorScheme.onSurface
    }
}