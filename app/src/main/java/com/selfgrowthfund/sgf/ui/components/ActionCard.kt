package com.selfgrowthfund.sgf.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.ActionItem
import com.selfgrowthfund.sgf.model.enums.ActionResponse
import com.selfgrowthfund.sgf.model.enums.ActionType
import java.time.format.DateTimeFormatter

@Composable
fun ActionCard(
    action: ActionItem,
    currentShareholderId: String,
    onRespond: (ActionResponse) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
    val userResponse = action.responses[currentShareholderId]

    Card(modifier = modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(action.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))

            Text(action.description, style = MaterialTheme.typography.bodyMedium)
            Text("Created by: ${action.createdBy}", style = MaterialTheme.typography.bodySmall)
            Text("Type: ${action.type.label}", style = MaterialTheme.typography.bodySmall)
            Text("Deadline: ${action.deadline?.format(formatter) ?: "No deadline"}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(12.dp))

            if (userResponse != null) {
                Text("Your Response: ${userResponse.label}", style = MaterialTheme.typography.bodyMedium)
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    when (action.type) {
                        ActionType.BORROWING_APPROVAL, ActionType.EXPENSE_REVIEW -> {
                            Button(onClick = { onRespond(ActionResponse.APPROVE) }) {
                                Text("Approve")
                            }
                            Button(onClick = { onRespond(ActionResponse.REJECT) }) {
                                Text("Reject")
                            }
                        }
                        ActionType.INVESTMENT_CONSENT -> {
                            Button(onClick = { onRespond(ActionResponse.CONSENT) }) {
                                Text("Consent")
                            }
                            Button(onClick = { onRespond(ActionResponse.DISSENT) }) {
                                Text("Dissent")
                            }
                        }
                    }
                }
            }
        }
    }
}