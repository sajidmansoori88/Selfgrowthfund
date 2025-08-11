package com.selfgrowthfund.sgf.ui.deposits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.utils.DateUtils.generateSelectableDueMonths
import com.selfgrowthfund.sgf.utils.DateUtils.getCurrentMonthFormatted
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DueMonthDropdown(
    selectedMonth: String,
    onMonthSelected: (String) -> Unit
) {
    val options = remember { generateSelectableDueMonths() }
    val currentMonth = remember { getCurrentMonthFormatted() }
    val formatter = remember { SimpleDateFormat("MMM-yyyy", Locale.ENGLISH) }
    val currentDate = remember { formatter.parse(currentMonth) }

    var expanded by remember { mutableStateOf(false) }

    Box {
        Text(
            text = selectedMonth.ifEmpty { "Select Due Month" },
            modifier = Modifier
                .clickable { expanded = true }
                .padding(16.dp)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { month: String ->
                val monthDate = formatter.parse(month)
                val isCurrent = month == currentMonth
                val isOverdue = monthDate != null && currentDate != null && monthDate.before(currentDate)

                DropdownMenuItem(
                    text = {
                        Text(
                            text = month,
                            color = when {
                                isCurrent -> MaterialTheme.colorScheme.primary
                                isOverdue -> MaterialTheme.colorScheme.error
                                else -> LocalContentColor.current
                            },
                            fontWeight = when {
                                isCurrent -> FontWeight.Bold
                                else -> FontWeight.Normal
                            },
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