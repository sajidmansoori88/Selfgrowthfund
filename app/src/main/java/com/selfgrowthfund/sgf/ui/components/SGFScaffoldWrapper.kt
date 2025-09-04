package com.selfgrowthfund.sgf.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SGFScaffoldWrapper(
    title: String,
    showTopBar: Boolean = true,
    showDrawer: Boolean = true,
    drawerState: DrawerState,
    scope: CoroutineScope,
    drawerContent: @Composable () -> Unit,
    onDrawerClick: () -> Unit = { scope.launch { drawerState.open() } },
    content: @Composable (PaddingValues) -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showDrawer,
        drawerContent = {
            if (showDrawer) drawerContent()
        }
    ) {
        Scaffold(
            topBar = {
                if (showTopBar) {
                    TopAppBar(
                        title = { Text(title) },
                        navigationIcon = {
                            IconButton(onClick = onDrawerClick) {
                                Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                            }
                        }
                    )
                }
            },
            content = content
        )
    }
}