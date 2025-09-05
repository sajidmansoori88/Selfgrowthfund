package com.selfgrowthfund.sgf.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.ui.components.SGFScaffoldWrapper
import com.selfgrowthfund.sgf.ui.navigation.DrawerContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavHostController,
    role: MemberRole,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    SGFScaffoldWrapper(
        title = "Admin Dashboard",
        drawerState = drawerState,
        scope = scope,
        drawerContent = {
            DrawerContent(
                navController = navController,
                onItemClick = { scope.launch { drawerState.close() } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text("Welcome, Admin", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Role: ${role.name}", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(32.dp))

            Text("Admin Actions", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate("approve_members") }) {
                Text("Approve New Members")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { navController.navigate("manage_roles") }) {
                Text("Manage Member Roles")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { navController.navigate("reports") }) {
                Text("View Reports")
            }
        }
    }
}