package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.DepositDao
import com.selfgrowthfund.sgf.data.local.entities.Deposit
import com.selfgrowthfund.sgf.utils.Dates
import javax.inject.Inject

class DepositRepository @Inject constructor(
    private val depositDao: DepositDao,
    dates: Dates
) {
    suspend fun createDeposit(
        shareholderId: String,
        shareholderName: String,
        dueMonth: String,
        paymentDate: String,
        shareNos: Int,
        additionalContribution: Double = 0.0,
        modeOfPayment: String
    ): String {
        val penalty = Deposit.calculatePenalty(dueMonth, paymentDate)
        val totalAmount = (shareNos * 2000) + penalty + additionalContribution

        val status = if (penalty > 0) Deposit.PAYMENT_LATE else Deposit.PAYMENT_ON_TIME

        val deposit = Deposit(
            depositId = Deposit.generateNextId(depositDao.getLastId()),
            shareholderId = shareholderId,
            shareholderName = shareholderName,
            dueMonth = dueMonth,
            paymentDate = paymentDate,
            shareNos = shareNos,
            additionalContribution = additionalContribution,
            penalty = penalty,
            totalAmount = totalAmount,
            paymentStatus = status,
            modeOfPayment = modeOfPayment
        )

        depositDao.insert(deposit)
        return deposit.depositId
    }

    fun getShareholderDeposits(shareholderId: String) =
        depositDao.getByShareholder(shareholderId)

    suspend fun getMonthlySummary(monthYear: String) =
        depositDao.getMonthlyTotal(monthYear)
}
