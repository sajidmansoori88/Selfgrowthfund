package com.selfgrowthfund.sgf.ui.penalty

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.selfgrowthfund.sgf.data.local.entities.Penalty
import com.selfgrowthfund.sgf.model.User
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.model.enums.PenaltyType
import com.selfgrowthfund.sgf.ui.components.DropdownMenuBox
import com.selfgrowthfund.sgf.ui.components.SGFScaffoldWrapper
import com.selfgrowthfund.sgf.ui.navigation.DrawerContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate


@Composable
fun AddPenaltyScreen(
    navController: NavHostController,
    user: User,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    if (user.role != MemberRole.MEMBER_TREASURER) {
        SGFScaffoldWrapper(
            title = "Access Denied",
            drawerState = drawerState,
            scope = scope,
            drawerContent = {
                DrawerContent(
                    navController = navController,
                    onItemClick = { scope.launch { drawerState.close() } }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Access Denied: Only Treasurers can record penalties.")
            }
        }
        return
    }

    val penaltyViewModel: PenaltyViewModel = hiltViewModel()

    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PenaltyType.SHARE_DEPOSIT) }

    SGFScaffoldWrapper(
        title = "Record Penalty",
        drawerState = drawerState,
        scope = scope,
        drawerContent = {
            DrawerContent(
                navController = navController,
                onItemClick = { scope.launch { drawerState.close() } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason") },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenuBox(
                label = "Penalty Type",
                options = PenaltyType.entries.toList(),
                selected = selectedType,
                onSelected = { selectedType = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val penalty = Penalty(
                        shareholderId = user.shareholderId,
                        date = LocalDate.now(),
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        type = selectedType,
                        reason = reason,
                        recordedBy = user.shareholderId
                    )

                    penaltyViewModel.insertPenalty(penalty)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Penalty")
            }
        }
    }
}