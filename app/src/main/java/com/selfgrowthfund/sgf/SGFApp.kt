package com.selfgrowthfund.sgf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.selfgrowthfund.sgf.model.User
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.ui.navigation.AppNavGraph
import com.selfgrowthfund.sgf.ui.navigation.DrawerItem
import kotlinx.coroutines.launch
import com.selfgrowthfund.sgf.ui.navigation.getDrawerItems
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SGFApp() {
    val userSessionViewModel: UserSessionViewModel = hiltViewModel()
    val currentUser by userSessionViewModel.currentUser.collectAsState()

    // Temporary mock user (remove later)
    val mockUser = User(
        shareholderId = "SH001",
        name = "Test User",
        role = MemberRole.MEMBER_TREASURER,
        id = "123",
    )
    val activeUser = currentUser ?: mockUser

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val drawerItems = remember(activeUser) {
        getDrawerItems(activeUser.role, activeUser.shareholderId)
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
                            badgeCount = if (item.route == Screen.Actions.route) {
                                // TODO: Replace with real pendingCount from ActionScreenViewModel
                                0
                            } else null,
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
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNavGraph(
                navController = navController,
                onDrawerClick = { scope.launch { drawerState.open() } },
                currentUser = activeUser
            )
        }
    }
}
