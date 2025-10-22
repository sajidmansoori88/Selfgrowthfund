package com.selfgrowthfund.sgf.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.model.enums.TransactionType
import com.selfgrowthfund.sgf.data.local.dto.TransactionDTO
import com.selfgrowthfund.sgf.ui.components.EnumDropdown
import java.time.LocalDate

@Composable
fun TransactionForm(
    modifier: Modifier = Modifier,
    onSubmit: (TransactionDTO) -> Unit
) {
    var selectedType by remember { mutableStateOf(TransactionType.Deposit.label) }
    var selectedMode by remember { mutableStateOf(PaymentMode.CASH.label) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val date = remember { mutableStateOf(LocalDate.now()) }

    Column(modifier = modifier
                .fillMaxSize()
                .padding(16.dp),) {
        EnumDropdown(
            label = "Transaction Type",
            options = TransactionType.getAllLabels(),
            selected = selectedType,
            onSelected = { selectedType = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        EnumDropdown(
            label = "Payment Mode",
            options = PaymentMode.getAllLabels(),
            selected = selectedMode,
            onSelected = { selectedMode = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val txn = TransactionDTO(
                transactionId = "TXN_${System.currentTimeMillis()}",
                shareholderId = "SH001", // Replace with actual selection
                type = TransactionType.fromLabel(selectedType),
                amount = amount.toDoubleOrNull() ?: 0.0,
                date = date.value,
                description = description,
                createdBy = "admin"
            )
            onSubmit(txn)
        }) {
            Text("Submit Transaction")
        }
    }
}