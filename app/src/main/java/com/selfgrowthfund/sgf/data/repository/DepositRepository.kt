package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.DepositDao
import com.selfgrowthfund.sgf.data.local.entities.Deposit
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import com.selfgrowthfund.sgf.utils.Dates
import java.util.Date
import javax.inject.Inject

class DepositRepository @Inject constructor(
    private val depositDao: DepositDao,
    private val dates: Dates
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
        val parsedDueMonth = try {
            DueMonth.parse(dueMonth)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Invalid dueMonth format: '$dueMonth'. Expected format: YYYY-MM"
            )
        }

        val paymentDateObj: Date = dates.parsePaymentDate(paymentDate)
            ?: throw IllegalArgumentException(
                "Invalid paymentDate format: '$paymentDate'. Expected format: ddMMyyyy"
            )

        val penalty = Deposit.calculatePenalty(parsedDueMonth, paymentDateObj)
        val totalAmount = (shareNos * 2000) + penalty + additionalContribution
        val status = if (penalty > 0) Deposit.PAYMENT_LATE else Deposit.PAYMENT_ON_TIME

        val deposit = Deposit(
            depositId = Deposit.generateNextId(depositDao.getLastId()),
            shareholderId = shareholderId,
            shareholderName = shareholderName,
            dueMonth = parsedDueMonth,
            paymentDate = dates.formatToPaymentDate(paymentDateObj),
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

    fun getAllDeposits() = depositDao.getAll()

    suspend fun getDepositById(id: String): Deposit? =
        depositDao.getById(id)

    suspend fun getMonthlySummary(monthYear: String): Double =
        depositDao.getMonthlyTotal(monthYear)

    suspend fun updateDeposit(deposit: Deposit) =
        depositDao.update(deposit)

    suspend fun deleteDeposit(id: String) =
        depositDao.deleteById(id)
}
