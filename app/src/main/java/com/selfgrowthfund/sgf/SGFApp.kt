package com.selfgrowthfund.sgf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.ui.navigation.AppNavGraph
import com.selfgrowthfund.sgf.ui.navigation.DrawerItem
import kotlinx.coroutines.launch
import com.selfgrowthfund.sgf.ui.navigation.getDrawerItems
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SGFApp() {
    val userSessionViewModel: UserSessionViewModel = hiltViewModel()
    val currentUser by userSessionViewModel.currentUser.collectAsState()

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val drawerItems = remember(currentUser) {
        getDrawerItems(currentUser.role)
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showDrawer = currentRoute !in listOf("welcome", "login")
    val showTopBar = showDrawer

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showDrawer,
        drawerContent = {
            if (showDrawer) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer) // AccentLight
                        .padding(vertical = 32.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    drawerItems.forEach { item ->
                        DrawerItem(
                            label = item.label,
                            badgeCount = item.badgeCount,
                            icon = item.icon,
                            textColor = MaterialTheme.colorScheme.onPrimaryContainer, // AccentDark
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (showTopBar) {
                    TopAppBar(
                        title = { Text("Self Growth Fund") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            }
        ) { padding ->
            Surface(modifier = Modifier.padding(padding)) {
                AppNavGraph(navController = navController)
            }
        }
    }
}