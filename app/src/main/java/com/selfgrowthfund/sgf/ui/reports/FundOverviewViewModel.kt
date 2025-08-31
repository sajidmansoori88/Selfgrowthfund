package com.selfgrowthfund.sgf.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dao.*
import com.selfgrowthfund.sgf.data.local.entities.FundOverviewMetrics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FundOverviewViewModel @Inject constructor(
    private val depositDao: DepositDao,
    private val borrowingDao: BorrowingDao,
    private val investmentDao: InvestmentDao,
    private val expenseDao: ExpenseDao,
    private val penaltyDao: PenaltyDao,
    private val repaymentDao: RepaymentDao
) : ViewModel() {

    private val _metrics = MutableStateFlow(
        FundOverviewMetrics(
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
        )
    )
    val metrics: StateFlow<FundOverviewMetrics> = _metrics

    init {
        viewModelScope.launch {
            // Income
            val totalShareAmount = depositDao.getTotalShareCount() * 2000.0
            val penaltiesFromShares = penaltyDao.getShareDepositPenalties()
            val additionalContributions = depositDao.getAdditionalContributions()
            val penaltiesFromBorrowings = penaltyDao.getBorrowingPenalties()
            val penaltiesFromInvestments = penaltyDao.getInvestmentPenalties()
            val otherIncomes = penaltyDao.getOtherIncome()
            val totalEarnings = totalShareAmount +
                    additionalContributions +
                    penaltiesFromShares +
                    penaltiesFromBorrowings +
                    penaltiesFromInvestments +
                    otherIncomes

            // Investments
            val totalInvestments = investmentDao.getTotalInvested()
            val activeInvestments = investmentDao.getActiveCount()
            val closedInvestments = investmentDao.getClosedCount()
            val overdueInvestments = investmentDao.getOverdueCount()
            val returnsFromClosed = investmentDao.getReturnsFromClosed()
            val writtenOffInvestments = investmentDao.getWrittenOffAmount()
            val investmentProfitAmount = returnsFromClosed - totalInvestments
            val investmentProfitPercent =
                if (totalInvestments != 0.0) (investmentProfitAmount / totalInvestments) * 100 else 0.0

            // Borrowings
            val totalBorrowIssued = borrowingDao.getTotalBorrowed()
            val activeBorrowings = borrowingDao.getActiveCount()
            val closedBorrowings = borrowingDao.getClosedCount()
            val repaymentsReceived = repaymentDao.getTotalRepaidAll()

            // Outstanding = total issued – total repaid
            val outstandingBorrowings = totalBorrowIssued - repaymentsReceived
            val overdueBorrowings = borrowingDao.getOverdueCount()

            // Expenses & Net
            val otherExpenses = expenseDao.getTotalExpenses()
            val netProfitOrLoss = totalEarnings + returnsFromClosed - otherExpenses

            _metrics.value = FundOverviewMetrics(
                totalShareAmount = totalShareAmount,
                penaltiesFromShareDeposits = penaltiesFromShares,
                additionalContributions = additionalContributions,
                penaltiesFromBorrowings = penaltiesFromBorrowings,
                penaltiesFromInvestments = penaltiesFromInvestments,
                totalOtherIncomes = otherIncomes,
                totalEarnings = totalEarnings,
                totalInvestments = totalInvestments,
                activeInvestments = activeInvestments,
                closedInvestments = closedInvestments,
                overdueInvestments = overdueInvestments,
                returnsFromClosedInvestments = returnsFromClosed,
                writtenOffInvestments = writtenOffInvestments,
                investmentProfitPercent = investmentProfitPercent,
                investmentProfitAmount = investmentProfitAmount,
                totalBorrowIssued = totalBorrowIssued,
                activeBorrowings = activeBorrowings,
                closedBorrowings = closedBorrowings,
                repaymentsReceived = repaymentsReceived,
                outstandingBorrowings = outstandingBorrowings,
                overdueBorrowings = overdueBorrowings,
                otherExpenses = otherExpenses,
                netProfitOrLoss = netProfitOrLoss
            )
        }
    }
}