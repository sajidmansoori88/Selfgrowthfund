package com.selfgrowthfund.sgf.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.selfgrowthfund.sgf.data.local.dto.TransactionDTO
import com.selfgrowthfund.sgf.model.enums.TransactionType
import com.selfgrowthfund.sgf.ui.components.EnumDropdown
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionManagerScreen(navController: NavHostController) {
    val viewModel: TransactionViewModel = hiltViewModel()
    val transactions by viewModel.transactions.collectAsState(initial = emptyList())

    var selectedType by remember { mutableStateOf("All") }

    val filtered = if (selectedType == "All") transactions
    else transactions.filter { it.type.label == selectedType }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Transaction Manager") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("addTransaction")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {

            EnumDropdown(
                label = "Filter by Type",
                options = listOf("All") + TransactionType.getAllLabels(),
                selected = selectedType,
                onSelected = { selectedType = it }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Transactions: ${filtered.size}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions found.")
                }
            } else {
                LazyColumn {
                    items(filtered) { txn ->
                        TransactionCard(txn)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(txn: TransactionDTO) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Type: ${txn.type.label}", style = MaterialTheme.typography.titleMedium)
            Text("Amount: ₹${txn.amount}")
            Text("Date: ${txn.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}")
            Text("Shareholder ID: ${txn.shareholderId}")
            if (txn.description.isNotBlank()) {
                Text("Note: ${txn.description}")
            }
        }
    }
}