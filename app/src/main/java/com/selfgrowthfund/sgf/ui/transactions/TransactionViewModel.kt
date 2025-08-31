package com.selfgrowthfund.sgf.ui.transactions

import androidx.lifecycle.ViewModel
import com.selfgrowthfund.sgf.data.local.dto.TransactionDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TransactionViewModel : ViewModel() {
    private val _transactions = MutableStateFlow<List<TransactionDTO>>(emptyList())
    val transactions: StateFlow<List<TransactionDTO>> = _transactions

    fun addTransaction(txn: TransactionDTO) {
        _transactions.value = _transactions.value + txn
        // 🔜 Replace with Firestore save logic
    }
}