package com.selfgrowthfund.sgf.model.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dao.DepositDao
import com.selfgrowthfund.sgf.data.local.dao.PenaltyDao
import com.selfgrowthfund.sgf.data.local.dao.ShareholderDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.pow
import javax.inject.Inject

@HiltViewModel
class ShareholderSummaryViewModel @Inject constructor(
    private val depositDao: DepositDao,
    private val penaltyDao: PenaltyDao,
    private val shareholderDao: ShareholderDao
) : ViewModel() {

    private val _summaries = MutableStateFlow<List<ShareholderSummary>>(emptyList())
    val summaries: StateFlow<List<ShareholderSummary>> = _summaries

    fun loadAllSummaries() {
        viewModelScope.launch {
            println("DEBUG: Loading summaries...")

            val depositSummary = depositDao.getShareholderDepositSummary()
            val penaltySummary = penaltyDao.getShareholderPenaltySummary()
            val shareholders = shareholderDao.getAllShareholders()

            println("DEBUG: Deposit summaries: ${depositSummary.size}")
            println("DEBUG: Penalty summaries: ${penaltySummary.size}")
            println("DEBUG: Shareholders: ${shareholders.size}")
            println("DEBUG: First shareholder: ${shareholders.firstOrNull()?.fullName}")


            val totalFundDeposit = depositDao.getTotalFundDeposit()
            val fundProfit = 120000.0 // placeholder
            val navPerShare: Double? = null

            val penaltyMap = penaltySummary.associateBy { it.shareholderId }
            val today = LocalDate.now()

            val summaries = depositSummary.mapNotNull { deposit ->
                val shareholder = shareholders.find { it.shareholderId == deposit.shareholderId } ?: return@mapNotNull null
                val penalties = penaltyMap[deposit.shareholderId]?.totalPenalties ?: 0.0
                val netDeposit = deposit.totalDeposits - penalties
                val shares = (netDeposit / 2000).toInt()
                val shareValue = navPerShare?.let { shares * it } ?: netDeposit
                val percentContribution = if (totalFundDeposit > 0) (netDeposit / totalFundDeposit) * 100 else 0.0
                val netProfit = (percentContribution / 100) * fundProfit
                val absoluteReturn = if (netDeposit > 0) (netProfit / netDeposit) * 100 else 0.0

                val lastDeposit = depositDao.getLastDepositForShareholder(deposit.shareholderId)
                val lastAmount = lastDeposit?.let { it.shareAmount * it.shareNos } ?: 0.0
                val lastDate = lastDeposit?.paymentDate ?: shareholder.joiningDate

                val daysHeld = ChronoUnit.DAYS.between(shareholder.joiningDate, today).coerceAtLeast(1)
                val yearsHeld = daysHeld / 365.0
                val annualizedReturn = if (absoluteReturn > -100.0 && yearsHeld > 0) {
                    ((1 + absoluteReturn / 100).pow(1 / yearsHeld) - 1) * 100
                } else 0.0

                ShareholderSummary(
                    shareholderId = deposit.shareholderId,
                    name = shareholder.fullName,
                    shares = shares,
                    shareAmount = netDeposit,
                    shareValue = shareValue,
                    percentContribution = percentContribution,
                    netProfit = netProfit,
                    absoluteReturn = absoluteReturn,
                    annualizedReturn = annualizedReturn,
                    lastContributionAmount = lastAmount,
                    lastContributionDate = lastDate,
                    nextDue = today.plusMonths(1),
                    outstandingBorrowing = 0.0
                )
            }
            println("DEBUG: Final summaries: ${summaries.size}")
            _summaries.value = summaries.sortedByDescending { it.netProfit }
        }
    }
}