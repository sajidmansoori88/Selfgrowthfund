package com.selfgrowthfund.sgf.ui.admin.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
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
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 3.dp,
        modifier = modifier
            .fillMaxSize() // ✅ let the table use available height (fixes cut-off)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // ───── Header ─────
            ShareholderTableHeader()

            if (shareholders.isEmpty()) {
                // ───── Empty State ─────
                EmptyStateCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    icon = Icons.Default.Person,
                    title = "No shareholders found",
                    message = "Add new members to get started."
                )
            } else {
                // ───── Data Rows ─────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.Top,
                ) {
                    shareholders.forEachIndexed { index, shareholder ->
                        ShareholderTableRow(
                            shareholder = shareholder,
                            index = index,
                            onModify = onModify,
                            onDelete = onDelete
                        )
                        if (index < shareholders.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ShareholderTableHeader() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .defaultMinSize(minWidth = 400.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderText("ID", Modifier.weight(0.8f))
            TableHeaderText("Name", Modifier.weight(1.6f))
            TableHeaderText("Role", Modifier.weight(1.2f))
            TableHeaderText("Status", Modifier.weight(1f))
            TableHeaderText("Actions", Modifier.weight(1f), TextAlign.Center)
        }
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
