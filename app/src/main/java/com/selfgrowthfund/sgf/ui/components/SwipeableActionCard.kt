package com.selfgrowthfund.sgf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SwipeableActionCard(
    modifier: Modifier = Modifier,
    entry: T,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    showSnackbar: (String) -> Unit = {},

    content: @Composable ColumnScope.(T) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onApprove()
                    showSnackbar("Approved")
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onReject()
                    showSnackbar("Rejected")
                    true
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val (color, icon, alignment) = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Triple(Color(0xFF4CAF50), Icons.Default.Check, Alignment.CenterStart)
                SwipeToDismissBoxValue.EndToStart -> Triple(Color(0xFFF44336), Icons.Default.Close, Alignment.CenterEnd)
                else -> Triple(Color.Transparent, null, Alignment.Center)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(16.dp),
                contentAlignment = alignment
            ) {
                icon?.let {
                    Icon(imageVector = it, contentDescription = null, tint = Color.White)
                }
            }
        }
    ) {
        Card(modifier = modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                content(entry) // 🔹 custom slot
            }
        }
    }
}
