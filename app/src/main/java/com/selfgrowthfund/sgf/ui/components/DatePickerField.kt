package com.selfgrowthfund.sgf.ui.components

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
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

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateChange(LocalDate.of(year, month + 1, dayOfMonth))
        },
        date?.year ?: LocalDate.now().year,
        date?.monthValue?.minus(1) ?: (LocalDate.now().monthValue - 1),
        date?.dayOfMonth ?: LocalDate.now().dayOfMonth
    )

    OutlinedTextField(
        value = formattedDate,
        onValueChange = {}, // No-op: field is read-only
        label = { Text(label) },
        readOnly = true,
        modifier = modifier.clickable { datePickerDialog.show() }
    )
}