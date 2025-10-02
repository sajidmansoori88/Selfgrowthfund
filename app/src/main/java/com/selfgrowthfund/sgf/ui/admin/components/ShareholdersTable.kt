package com.selfgrowthfund.sgf.ui.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.ui.components.EmptyStateCard

@Composable
fun ShareholdersTable(
    shareholders: List<Shareholder>,
    onModify: (Shareholder) -> Unit,
    onDelete: (Shareholder) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        ShareholderTableHeader()

        if (shareholders.isEmpty()) {
            // Show empty state
            EmptyStateCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                icon = Icons.Default.Person,
                title = "No shareholders found",
                message = "Add new members to get started."
            )
        } else {
            // Show data rows
            shareholders.forEach { shareholder ->
                ShareholderTableRow(
                    shareholder = shareholder,        // ✅ already a Shareholder
                    onModify = onModify,
                    onDelete = onDelete
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun ShareholderTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .defaultMinSize(minWidth = 350.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableHeaderText("ID", Modifier.weight(1f), TextAlign.Start)
        TableHeaderText("Name", Modifier.weight(2f), TextAlign.Start)
        TableHeaderText("Role", Modifier.weight(1.5f), TextAlign.Start)
        TableHeaderText("Actions", Modifier.weight(1f), TextAlign.Center)
    }
}

@Composable
private fun TableHeaderText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        textAlign = textAlign
    )
}
