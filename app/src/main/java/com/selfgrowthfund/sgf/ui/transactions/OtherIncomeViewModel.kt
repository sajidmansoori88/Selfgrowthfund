package com.selfgrowthfund.sgf.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dao.OtherIncomeDao
import com.selfgrowthfund.sgf.data.local.entities.OtherIncomes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtherIncomeViewModel @Inject constructor(
    private val otherIncomeDao: OtherIncomeDao
) : ViewModel() {

    fun insertIncome(otherIncomes: OtherIncomes) {
        viewModelScope.launch {
            otherIncomeDao.insertIncome(otherIncomes)
        }
    }
}