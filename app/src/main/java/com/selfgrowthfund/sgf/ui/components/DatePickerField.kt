package com.selfgrowthfund.sgf.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

@Composable
fun DatePickerField(
    label: String,
    date: LocalDate?,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val formattedDate = date?.format(formatter) ?: ""

    val today = LocalDate.now()
    val initialDate = date ?: today

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateChange(LocalDate.of(year, month + 1, dayOfMonth))
        },
        initialDate.year,
        initialDate.monthValue - 1,
        initialDate.dayOfMonth
    )

    OutlinedTextField(
        value = formattedDate,
        onValueChange = {}, // Read-only
        label = { Text(label) },
        readOnly = true,
        modifier = modifier.clickable { datePickerDialog.show() }
    )
}