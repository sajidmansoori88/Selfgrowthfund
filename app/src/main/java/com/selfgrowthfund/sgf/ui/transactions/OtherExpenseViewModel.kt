package com.selfgrowthfund.sgf.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dao.OtherExpenseDao
import com.selfgrowthfund.sgf.data.local.entities.OtherExpense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtherExpenseViewModel @Inject constructor(
    private val otherExpenseDao: OtherExpenseDao
) : ViewModel() {

    fun insertExpense(otherExpenses: OtherExpense) {
        viewModelScope.launch {
            otherExpenseDao.insertExpense(otherExpenses)
        }
    }
}