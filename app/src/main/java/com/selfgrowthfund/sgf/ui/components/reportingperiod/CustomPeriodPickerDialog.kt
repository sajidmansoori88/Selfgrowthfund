package com.selfgrowthfund.sgf.ui.components.reportingperiod

import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPeriodPickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (CustomPeriod) -> Unit
) {
    val datePickerState = rememberDateRangePickerState()

    val startMillis = datePickerState.selectedStartDateMillis
    val endMillis = datePickerState.selectedEndDateMillis
    val isValid = startMillis != null && endMillis != null

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = isValid, // ✅ disable until valid
                onClick = {
                    if (startMillis != null && endMillis != null) {
                        val startDate = Instant.ofEpochMilli(startMillis)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                        val endDate = Instant.ofEpochMilli(endMillis)
                            .atZone(ZoneId.systemDefault()).toLocalDate()

                        onConfirm(CustomPeriod(startDate, endDate))
                        onDismiss() // close after confirm
                    }
                }
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DateRangePicker(state = datePickerState)
    }
}
