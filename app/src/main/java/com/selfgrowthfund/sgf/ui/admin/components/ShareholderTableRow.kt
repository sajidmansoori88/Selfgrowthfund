package com.selfgrowthfund.sgf.ui.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import com.selfgrowthfund.sgf.model.enums.ShareholderStatus

@Composable
fun ShareholderTableRow(
    shareholder: Shareholder,
    onModify: (Shareholder) -> Unit,
    onDelete: (Shareholder) -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 0
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    val rowBackground =
        if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        else MaterialTheme.colorScheme.surface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(rowBackground)
            .padding(horizontal = 16.dp)
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ID
        Text(
            text = shareholder.shareholderId.ifBlank { "-" },
            modifier = Modifier.weight(0.8f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Name
        Text(
            text = shareholder.fullName.ifBlank { "Unnamed" },
            modifier = Modifier.weight(1.6f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Role
        Text(
            text = shareholder.role.label,
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Status
        Text(
            text = shareholder.shareholderStatus.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = getStatusColor(shareholder.shareholderStatus),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Actions
        Row(
            modifier = Modifier
                .weight(1f)
                .requiredWidthIn(min = 100.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onModify(shareholder) }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { showConfirmDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // 🔔 Confirmation dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Deletion") },
            text = {
                Text(
                    "Are you sure you want to delete ${shareholder.fullName.ifBlank { "this shareholder" }}?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onDelete(shareholder)
                }) {
                    Text("Yes, Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}
@Composable
fun getStatusColor(status: ShareholderStatus): Color = when (status) {
    ShareholderStatus.Active -> MaterialTheme.colorScheme.primary
    ShareholderStatus.Inactive -> MaterialTheme.colorScheme.error
}
