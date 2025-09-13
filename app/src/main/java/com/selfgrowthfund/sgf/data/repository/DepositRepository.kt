package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.DepositDao
import com.selfgrowthfund.sgf.data.local.entities.Deposit
import com.selfgrowthfund.sgf.data.local.entities.DepositEntry
import com.selfgrowthfund.sgf.data.local.dao.DepositEntryDao
import com.selfgrowthfund.sgf.data.local.dto.DepositEntrySummaryDTO
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import com.selfgrowthfund.sgf.model.enums.ApprovalAction
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.model.enums.PaymentStatus
import kotlinx.coroutines.flow.Flow
import com.selfgrowthfund.sgf.utils.Dates
import java.time.LocalDate
import javax.inject.Inject

class DepositRepository @Inject constructor(
    private val depositDao: DepositDao,
    private val depositEntryDao: DepositEntryDao,
    dates: Dates
) {
    suspend fun createDeposit(
        shareholderId: String,
        shareholderName: String,
        dueMonth: String,  // Keep as String parameter
        paymentDate: LocalDate,
        shareNos: Int,
        additionalContribution: Double = 0.0,
        modeOfPayment: String
    ): String {

        // Convert String to DueMonth
        val dueMonthObj = DueMonth(dueMonth)  // Create DueMonth object from String

        val penalty = Deposit.calculatePenalty(dueMonth, paymentDate)
        val totalAmount = (shareNos * 2000) + penalty + additionalContribution

        val status = if (penalty > 0) PaymentStatus.LATE else PaymentStatus.ON_TIME

        val deposit = Deposit(
            depositId = Deposit.generateNextId(depositDao.getLastId()),
            shareholderId = shareholderId,
            shareholderName = shareholderName,
            dueMonth = dueMonthObj,  // Pass DueMonth object, not String
            paymentDate = paymentDate,
            shareNos = shareNos,
            additionalContribution = additionalContribution,
            penalty = penalty,
            totalAmount = totalAmount,
            paymentStatus = PaymentStatus.PENDING,
            modeOfPayment = PaymentMode.valueOf(modeOfPayment),
            approvalStatus = ApprovalAction.PENDING
        )

        depositDao.insert(deposit)
        return deposit.depositId
    }

    fun getShareholderDeposits(shareholderId: String) =
        depositDao.getByShareholder(shareholderId)

    suspend fun getMonthlySummary(monthYear: String) =
        depositDao.getMonthlyTotal(monthYear)

    suspend fun insertDepositEntry(entry: DepositEntry) {
        depositEntryDao.insert(entry)
        }
    fun getAllDeposits(): List<Deposit> {
        return depositDao.getAllDeposits()
    }

    fun getDepositSummaries(): Flow<List<DepositEntrySummaryDTO>> {
        return depositDao.getDepositEntrySummary()
    }
    fun getDepositEntrySummary(): Flow<List<DepositEntrySummaryDTO>> {
        return depositDao.getDepositEntrySummary()
    }
    suspend fun countApproved(start: LocalDate, end: LocalDate) =
        depositDao.countByStatus("APPROVED", start, end)

    suspend fun countRejected(start: LocalDate, end: LocalDate) =
        depositDao.countByStatus("REJECTED", start, end)

    suspend fun countPending(start: LocalDate, end: LocalDate) =
        depositDao.countByStatus("PENDING", start, end)

    suspend fun countTotal(start: LocalDate, end: LocalDate) =
        depositDao.countTotal(start, end)



}
