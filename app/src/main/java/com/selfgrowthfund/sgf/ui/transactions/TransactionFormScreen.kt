package com.selfgrowthfund.sgf.ui.transactions

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.selfgrowthfund.sgf.data.local.dto.TransactionDTO


@Composable
fun TransactionFormScreen(
    onSubmit: (TransactionDTO) -> Unit
) {

        TransactionForm(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            onSubmit = onSubmit
        )
    }