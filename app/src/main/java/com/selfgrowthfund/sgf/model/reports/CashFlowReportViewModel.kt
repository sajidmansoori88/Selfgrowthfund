package com.selfgrowthfund.sgf.model.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dao.DepositDao
import com.selfgrowthfund.sgf.data.local.dao.ExpenseDao
import com.selfgrowthfund.sgf.data.local.dao.IncomeDao
import com.selfgrowthfund.sgf.data.local.dao.PenaltyDao
import com.selfgrowthfund.sgf.data.local.dao.RepaymentDao
import dagger.hilt.android.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CashFlowReportViewModel @Inject constructor(
    private val depositDao: DepositDao,
    private val penaltyDao: PenaltyDao,
    private val repaymentDao: RepaymentDao,
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao
) : ViewModel() {

    private val _cashFlow = MutableStateFlow<List<MonthlyCashFlow>>(emptyList())
    val cashFlow: StateFlow<List<MonthlyCashFlow>> = _cashFlow

    init {
        viewModelScope.launch {
            val deposits = depositDao.getMonthlyDeposits() // List<MonthlyAmount>
            val penalties = penaltyDao.getMonthlyPenalties() // List<MonthlyAmount>
            val repayments = repaymentDao.getMonthlyRepayments() // List<MonthlyAmount>
            val otherIncome = incomeDao.getMonthlyOtherIncome() // List<MonthlyAmount>
            val expenses = expenseDao.getMonthlyExpenses() // List<MonthlyAmount>

            val incomeMap = mutableMapOf<String, Double>()

            listOf(deposits, penalties, repayments, otherIncome).forEach { source ->
                source.forEach { entry ->
                    incomeMap[entry.month] = (incomeMap[entry.month] ?: 0.0) + entry.total
                }
            }

            val expenseMap = expenses.associate { it.month to it.total }

            val allMonths = (incomeMap.keys + expenseMap.keys).toSortedSet()
            val monthlyEntries = allMonths.map { month ->
                MonthlyCashFlow(
                    month = month,
                    income = incomeMap[month] ?: 0.0,
                    expenses = expenseMap[month] ?: 0.0
                )
            }

            var previousClosing = 0.0
            monthlyEntries.forEach { entry ->
                entry.openingBalance = previousClosing
                val net = entry.income - entry.expenses
                entry.closingBalance = entry.openingBalance + net
                previousClosing = entry.closingBalance
            }

            _cashFlow.value = monthlyEntries
        }
    }
}