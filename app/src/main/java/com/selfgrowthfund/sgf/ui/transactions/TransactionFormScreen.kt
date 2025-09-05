package com.selfgrowthfund.sgf.ui.transactions

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.selfgrowthfund.sgf.data.local.dto.TransactionDTO
import com.selfgrowthfund.sgf.ui.components.SGFScaffoldWrapper
import com.selfgrowthfund.sgf.ui.navigation.DrawerContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TransactionFormScreen(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    onSubmit: (TransactionDTO) -> Unit
) {
    SGFScaffoldWrapper(
        title = "Record Transaction",
        drawerState = drawerState,
        scope = scope,
        drawerContent = {
            DrawerContent(
                navController = navController,
                onItemClick = { scope.launch { drawerState.close() } }
            )
        }
    ) { padding ->
        TransactionForm(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            onSubmit = onSubmit
        )
    }
}