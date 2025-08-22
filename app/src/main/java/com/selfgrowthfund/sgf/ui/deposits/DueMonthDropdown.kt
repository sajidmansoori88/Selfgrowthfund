package com.selfgrowthfund.sgf.ui.deposits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.utils.DateUtils.generateSelectableDueMonths
import com.selfgrowthfund.sgf.utils.DateUtils.getCurrentMonthFormatted
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DueMonthDropdown(
    selectedMonth: String,
    onMonthSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    val options = remember { generateSelectableDueMonths() }
    val currentMonth = remember { getCurrentMonthFormatted() }
    val formatter = remember { DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH) }

    val currentDate = remember {
        try {
            YearMonth.parse(currentMonth, formatter)
        } catch (_: Exception) {
            null
        }
    }

    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clickable(enabled = enabled) { expanded = true }
                .padding(16.dp)
        ) {
            Text(
                text = selectedMonth.ifEmpty { "Select Due Month" },
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { month ->
                val monthDate = try {
                    YearMonth.parse(month, formatter)
                } catch (_: Exception) {
                    null
                }

                val isCurrent = month == currentMonth
                val isOverdue = monthDate != null && currentDate != null && monthDate.isBefore(currentDate)

                DropdownMenuItem(
                    text = {
                        Text(
                            text = month,
                            color = when {
                                isCurrent -> MaterialTheme.colorScheme.primary
                                isOverdue -> MaterialTheme.colorScheme.error
                                else -> LocalContentColor.current
                            },
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (isOverdue) FontStyle.Italic else FontStyle.Normal
                        )
                    },
                    onClick = {
                        onMonthSelected(month)
                        expanded = false
                    }
                )
            }
        }
    }
}