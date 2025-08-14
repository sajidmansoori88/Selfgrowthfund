package com.selfgrowthfund.sgf.ui.repayment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.Repayment

@Composable
fun RepaymentItem(repayment: Repayment) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("ID: ${repayment.repaymentId}")
        Text("Amount Paid: ₹${repayment.totalAmountPaid}")
        Text("Date: ${repayment.repaymentDate}")
        // Add more fields as needed
    }
}