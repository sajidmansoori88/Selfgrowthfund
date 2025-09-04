package com.selfgrowthfund.sgf.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.*
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
                drawerState = drawerState,
                scope = scope,
                onDrawerClick = { scope.launch { drawerState.open() } }
            )

        }
    } else {
        AppNavGraph(
            navController = navController,
            drawerState = drawerState,
            scope = scope,
            onDrawerClick = {} // No-op since drawer is hidden
        )
    }
}