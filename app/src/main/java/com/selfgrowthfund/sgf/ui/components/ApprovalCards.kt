package com.selfgrowthfund.sgf.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/*-------------------------------------------------------
 | Common Helper for Info Rows
 *------------------------------------------------------*/
@Composable
private fun InfoRow(
    label: String,
    value: String,
    accent: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (accent) FontWeight.Bold else FontWeight.Normal,
                color = if (accent)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

/*-------------------------------------------------------
 | Investment Approval Card
 *------------------------------------------------------*/
@Composable
fun InvestmentApprovalCard(
    investeeName: String,
    dateOfApplication: String,
    amount: Double,
    expectedReturnAmount: Double,
    expectedReturnPercent: Double,
    expectedReturnDate: String,
    returnDays: Int,
    xirr: Double,
    statusLabel: String? = null, // 🟢 NEW
    onApprove: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Title + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Investment Application",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                if (!statusLabel.isNullOrBlank()) {
                    StatusChip(label = statusLabel)
                }
            }

            Divider(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .height(1.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )

            InfoRow(label = "Investee", value = investeeName)
            InfoRow(label = "Date", value = dateOfApplication)
            InfoRow(label = "Amount", value = "₹%,.2f".format(amount))
            InfoRow(
                label = "Expected Return",
                value = "₹%,.2f (%.1f%%)".format(expectedReturnAmount, expectedReturnPercent)
            )
            InfoRow(
                label = "Expected By",
                value = "$expectedReturnDate ($returnDays days)"
            )
            InfoRow(label = "XIRR", value = "%.2f%%".format(xirr), accent = true)

            if (onApprove != null && onReject != null) {
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { onApprove() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Approve") }

                    OutlinedButton(
                        onClick = { onReject() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Reject") }
                }
            }
        }
    }
}

/*-------------------------------------------------------
 | Borrowing Approval Card
 *------------------------------------------------------*/
@Composable
fun BorrowingApprovalCard(
    borrowerName: String,
    dateOfApplication: String,
    borrowEligibility: Double,
    amountRequested: Double,
    statusLabel: String? = null, // 🟢 NEW
    onApprove: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Borrowing Application",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                if (!statusLabel.isNullOrBlank()) {
                    StatusChip(label = statusLabel)
                }
            }

            Divider(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .height(1.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )

            InfoRow(label = "Borrower", value = borrowerName)
            InfoRow(label = "Date", value = dateOfApplication)
            InfoRow(label = "Eligibility", value = "₹%,.2f".format(borrowEligibility))
            InfoRow(
                label = "Amount Requested",
                value = "₹%,.2f".format(amountRequested),
                accent = true
            )

            if (onApprove != null && onReject != null) {
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { onApprove() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Approve") }

                    OutlinedButton(
                        onClick = { onReject() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Reject") }
                }
            }
        }
    }
}

/*-------------------------------------------------------
 | Status Chip (New)
 *------------------------------------------------------*/
@Composable
fun StatusChip(label: String) {
    val (bgColor, textColor) = when (label.lowercase()) {
        "approved" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "rejected" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    AssistChip(
        onClick = {},
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = bgColor,
            labelColor = textColor
        )
    )
}
