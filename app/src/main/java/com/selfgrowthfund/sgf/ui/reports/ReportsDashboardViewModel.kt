package com.selfgrowthfund.sgf.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.model.reports.FundOverviewMetrics
import com.selfgrowthfund.sgf.model.reports.CashFlowEntry
import com.selfgrowthfund.sgf.data.local.dao.ExpenseDao
import com.selfgrowthfund.sgf.data.local.dao.PenaltyDao
import com.selfgrowthfund.sgf.data.local.dao.DepositDao
import com.selfgrowthfund.sgf.data.local.dao.BorrowingDao
import com.selfgrowthfund.sgf.data.local.dao.InvestmentDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class ReportsDashboardViewModel @Inject constructor(
    private val depositDao: DepositDao,
    private val borrowingDao: BorrowingDao,
    private val investmentDao: InvestmentDao,
    private val expenseDao: ExpenseDao,
    private val penaltyDao: PenaltyDao
) : ViewModel() {

    private val _fundOverview = MutableStateFlow(FundOverviewMetrics(
        totalShareAmount = 0.0,
        penaltiesFromShareDeposits = 0.0,
        additionalContributions = 0.0,
        penaltiesFromBorrowings = 0.0,
        penaltiesFromInvestments = 0.0,
        totalOtherIncomes = 0.0,
        totalEarnings = 0.0,
        totalInvestments = 0.0,
        activeInvestments = 0,
        closedInvestments = 0,
        overdueInvestments = 0,
        returnsFromClosedInvestments = 0.0,
        writtenOffInvestments = 0.0,
        investmentProfitPercent = 0.0,
        investmentProfitAmount = 0.0,
        totalBorrowIssued = 0.0,
        activeBorrowings = 0,
        closedBorrowings = 0,
        repaymentsReceived = 0.0,
        outstandingBorrowings = 0.0,
        overdueBorrowings = 0,
        otherExpenses = 0.0,
        netProfitOrLoss = 0.0
    ))
    val fundOverview: StateFlow<FundOverviewMetrics> = _fundOverview

    private val _monthlyCashFlow = MutableStateFlow<List<CashFlowEntry>>(emptyList())
    val monthlyCashFlow: StateFlow<List<CashFlowEntry>> = _monthlyCashFlow

    init {
        viewModelScope.launch {
            _fundOverview.value = computeFundOverview()
            _monthlyCashFlow.value = computeMonthlyCashFlow()
        }
    }

    private suspend fun computeFundOverview(): FundOverviewMetrics {
        val totalShares = depositDao.getTotalShareAmount()
        val penaltiesFromShares = penaltyDao.getShareDepositPenalties()
        val additionalContributions = depositDao.getAdditionalContributions()
        val penaltiesFromBorrowings = penaltyDao.getBorrowingPenalties()
        val penaltiesFromInvestments = penaltyDao.getInvestmentPenalties()
        val otherIncomes = penaltyDao.getOtherIncome()
        val totalEarnings = totalShares + additionalContributions + penaltiesFromShares +
                penaltiesFromBorrowings + penaltiesFromInvestments + otherIncomes

        val totalInvestments = investmentDao.getTotalInvested()
        val activeInvestments = investmentDao.getActiveCount()
        val closedInvestments = investmentDao.getClosedCount()
        val overdueInvestments = investmentDao.getOverdueCount()
        val returnsFromClosed = investmentDao.getReturnsFromClosed()
        val writtenOff = investmentDao.getWrittenOffAmount()
        val investmentProfitAmount = returnsFromClosed - totalInvestments
        val investmentProfitPercent = if (totalInvestments > 0)
            (investmentProfitAmount / totalInvestments) * 100 else 0.0

        val totalBorrowIssued = borrowingDao.getTotalBorrowed()
        val activeBorrowings = borrowingDao.getActiveCount()
        val closedBorrowings = borrowingDao.getClosedCount()
        val repaymentsReceived = borrowingDao.getTotalRepaid()
        val outstandingBorrowings = borrowingDao.getOutstandingAmount()
        val overdueBorrowings = borrowingDao.getOverdueCount()

        val otherExpenses = expenseDao.getTotalExpenses()
        val netProfit = totalEarnings + returnsFromClosed - otherExpenses

        return FundOverviewMetrics(
            totalShares, penaltiesFromShares, additionalContributions,
            penaltiesFromBorrowings, penaltiesFromInvestments, otherIncomes, totalEarnings,
            totalInvestments, activeInvestments, closedInvestments, overdueInvestments,
            returnsFromClosed, writtenOff, investmentProfitPercent, investmentProfitAmount,
            totalBorrowIssued, activeBorrowings, closedBorrowings, repaymentsReceived,
            outstandingBorrowings, overdueBorrowings, otherExpenses, netProfit
        )
    }

    private suspend fun computeMonthlyCashFlow(): List<CashFlowEntry> {
        val months = depositDao.getActiveMonths() // e.g. ["2025-06", "2025-07", "2025-08"]
        val entries = mutableListOf<CashFlowEntry>()
        var previousClosing = 0.0

        for (monthStr in months) {
            val ym = YearMonth.parse(monthStr)
            val monthStrFormatted = ym.toString() // e.g. "2025-08"

            val income = depositDao.getMonthlyIncome(monthStrFormatted) +
                    penaltyDao.getMonthlyPenaltyTotal(monthStrFormatted) +
                    investmentDao.getMonthlyReturns(monthStrFormatted)

            val expense = expenseDao.getMonthlyExpenses(monthStrFormatted) +
                    borrowingDao.getMonthlyRepayments(monthStrFormatted)

            val opening = previousClosing
            val closing = opening + income - expense

            entries.add(
                CashFlowEntry(
                    month = ym.month.name.lowercase().replaceFirstChar { it.uppercase() } + " ${ym.year}",
                    openingBalance = opening,
                    income = income,
                    expense = expense,
                    closingBalance = closing
                )
            )

            previousClosing = closing
        }

        return entries
    }
}