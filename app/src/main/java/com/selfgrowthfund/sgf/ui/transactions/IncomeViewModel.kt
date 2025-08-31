package com.selfgrowthfund.sgf.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dao.IncomeDao
import com.selfgrowthfund.sgf.data.local.entities.Income
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val incomeDao: IncomeDao
) : ViewModel() {

    fun insertIncome(income: Income) {
        viewModelScope.launch {
            incomeDao.insertIncome(income)
        }
    }
}