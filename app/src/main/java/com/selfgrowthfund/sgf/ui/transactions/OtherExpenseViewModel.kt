package com.selfgrowthfund.sgf.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dao.OtherExpenseDao
import com.selfgrowthfund.sgf.data.local.entities.OtherExpenses
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtherExpenseViewModel @Inject constructor(
    private val otherExpenseDao: OtherExpenseDao
) : ViewModel() {

    fun insertExpense(otherExpenses: OtherExpenses) {
        viewModelScope.launch {
            otherExpenseDao.insertExpense(otherExpenses)
        }
    }
}