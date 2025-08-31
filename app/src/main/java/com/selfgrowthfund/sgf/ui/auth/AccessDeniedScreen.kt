package com.selfgrowthfund.sgf.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.selfgrowthfund.sgf.ui.navigation.Screen

@Composable
fun AccessDeniedScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Access Denied",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your email is not authorized to access Self Growth Fund.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { navController.navigate(Screen.Welcome.route) }) {
            Text("Back to Welcome")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            // Optional: open email intent or support screen
        }) {
            Text("Contact Support")
        }
    }
}