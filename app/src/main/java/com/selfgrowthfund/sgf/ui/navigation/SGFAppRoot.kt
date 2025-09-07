package com.selfgrowthfund.sgf.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.selfgrowthfund.sgf.model.User
import com.selfgrowthfund.sgf.model.enums.MemberRole
import kotlinx.coroutines.launch

@Composable
fun SGFAppRoot() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val excludedRoutes = listOf(
        Screen.Welcome.route,
        Screen.Login.route,
        Screen.PinEntry.route,
        Screen.CreatePin.route,
        Screen.BiometricSetup.route,
        Screen.AccessDenied.route
    )

    val showDrawer = currentRoute !in excludedRoutes

    // Inject mock user for testing
    val mockUser = User(
        shareholderId = "SH001",
        name = "Test User",
        role = MemberRole.MEMBER_ADMIN,
        id = "123"
    )

    val activeUser = mockUser // Replace with session logic when ready

    if (showDrawer) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    navController = navController,
                    onItemClick = { scope.launch { drawerState.close() } }
                )
            }
        ) {
            AppNavGraph(
                navController = navController,
                onDrawerClick = { scope.launch { drawerState.open() } },
                currentUser = activeUser
            )
        }
    } else {
        AppNavGraph(
            navController = navController,
            onDrawerClick = {}, // No-op since drawer is hidden
            currentUser = activeUser
        )
    }
}