package com.selfgrowthfund.sgf.ui.shareholders

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.selfgrowthfund.sgf.data.local.entities.Shareholder

@Composable
fun ShareholderListScreen(
    viewModel: ShareholderListViewModel = hiltViewModel(),
    navController: NavController
) {
    val shareholders by viewModel.shareholders.collectAsState()
    val context = LocalContext.current

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(shareholders) { shareholder ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        navController.navigate("editShareholder/${shareholder.shareholderId}")
                    },
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(shareholder.fullName, style = MaterialTheme.typography.titleMedium)
                    Text("Mobile: ${shareholder.mobileNumber}")
                    Text("Balance: ₹${shareholder.shareBalance}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(onClick = {
                            viewModel.deleteShareholder(shareholder.shareholderId)
                            Toast.makeText(context, "Deleted ${shareholder.fullName}", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Delete")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            navController.navigate("editShareholder/${shareholder.shareholderId}")
                        }) {
                            Text("Edit")
                        }
                    }
                }
            }
        }
    }
}