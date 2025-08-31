package com.selfgrowthfund.sgf.ui.addshareholders

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.selfgrowthfund.sgf.data.local.entities.Shareholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareholderListScreen(
    navController: NavController,
    viewModel: ShareholderListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val shareholders by viewModel.shareholders.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Shareholders") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addShareholder") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Shareholder")
            }
        }
    ) { innerPadding ->
        if (shareholders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No shareholders found", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("addShareholder") }) {
                    Text("Add First Shareholder")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                items(shareholders) { shareholder ->
                    ShareholderCard(
                        shareholder = shareholder,
                        viewModel = viewModel,
                        navController = navController,
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
fun ShareholderCard(
    shareholder: Shareholder,
    viewModel: ShareholderListViewModel,
    navController: NavController,
    context: Context
) {
    var showDialog by remember { mutableStateOf(false) }
    val id = shareholder.shareholderId

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete ${shareholder.fullName}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteShareholder(id)
                    Toast.makeText(context, "Deleted ${shareholder.fullName}", Toast.LENGTH_SHORT).show()
                    showDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                navController.navigate("editShareholder/$id")
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(shareholder.fullName, style = MaterialTheme.typography.titleMedium)
            Text("Mobile: ${shareholder.mobileNumber}")
            Text("Balance: ₹${shareholder.shareBalance}")
            // ✅ Use the enum's label for readability
            Text("Role: ${shareholder.role.label}")

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Button(onClick = { showDialog = true }) {
                    Text("Delete")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    navController.navigate("editShareholder/$id")
                }) {
                    Text("Edit")
                }
            }
        }
    }
}