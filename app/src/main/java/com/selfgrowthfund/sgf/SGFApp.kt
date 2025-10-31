package com.selfgrowthfund.sgf

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.ui.navigation.AppNavGraph
import com.selfgrowthfund.sgf.ui.navigation.DrawerItem
import com.selfgrowthfund.sgf.ui.navigation.getDrawerItems
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SGFApp() {
    val userSessionViewModel: UserSessionViewModel = hiltViewModel()
    val currentUser by userSessionViewModel.currentUser.collectAsState()

    // 🔒 Block until authenticated user is available
    if (currentUser.shareholderId.isBlank()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text("Loading user session...")
            }
        }
        return
    }

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val drawerItems = remember(currentUser) {
        getDrawerItems(currentUser.role, currentUser.shareholderId)
    }

    val startDestination = when (currentUser.role) {
        MemberRole.MEMBER_ADMIN -> "admin_dashboard"
        MemberRole.MEMBER_TREASURER -> Screen.TreasurerDashboard.route
        else -> Screen.Home.route
    }

    LaunchedEffect(currentUser) {
        Log.d("SGFApp", "User session active: $currentUser")
        Log.d("SGFApp", "Start destination: $startDestination")
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showDrawer = currentRoute !in listOf("welcome", "login")

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showDrawer,
        drawerContent = {
            if (showDrawer) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(vertical = 32.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    drawerItems.forEach { item ->
                        DrawerItem(
                            item = item,
                            textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNavGraph(
                navController = navController,
                onDrawerClick = { scope.launch { drawerState.open() } },
                currentUser = currentUser,
                startDestination = startDestination
            )
        }
    }
}
