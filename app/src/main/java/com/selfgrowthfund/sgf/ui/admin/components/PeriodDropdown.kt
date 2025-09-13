package com.selfgrowthfund.sgf.ui.admin.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.selfgrowthfund.sgf.model.ApprovalPeriod

@Composable
fun PeriodDropdown(
    selected: ApprovalPeriod,
    onSelect: (ApprovalPeriod) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(selected.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ApprovalPeriod.entries.forEach { period ->
                DropdownMenuItem(
                    text = { Text(period.name) },
                    onClick = {
                        onSelect(period)
                        expanded = false
                    }
                )
            }
        }
    }
}