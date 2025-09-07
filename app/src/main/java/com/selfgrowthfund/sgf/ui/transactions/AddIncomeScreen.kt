package com.selfgrowthfund.sgf.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.data.local.entities.Income
import com.selfgrowthfund.sgf.model.User
import com.selfgrowthfund.sgf.model.enums.MemberRole
import java.time.LocalDate

@Composable
fun AddIncomeScreen(
    user: User

) {
    val incomeViewModel: IncomeViewModel = hiltViewModel()

    var amount by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    if (user.role != MemberRole.MEMBER_TREASURER) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Access Denied: Only Treasurers can record incomes.")
        }
        return
    }
    Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = source,
                onValueChange = { source = it },
                label = { Text("Source (e.g. Bank Interest)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val income = Income(
                        date = LocalDate.now(),
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        remarks = description,
                        recordedBy = user.shareholderId
                    )
                    incomeViewModel.insertIncome(income)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Income")
            }
        }
    }