package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.RepaymentDao
import com.selfgrowthfund.sgf.data.local.entities.Repayment
import com.selfgrowthfund.sgf.data.local.dao.RepaymentDao.BorrowingRepaymentSummary
import com.selfgrowthfund.sgf.data.local.dao.RepaymentDao.MonthlyRepaymentReport
import com.selfgrowthfund.sgf.data.local.dao.RepaymentDao.RepaymentWithBorrowingDetails
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RepaymentRepository @Inject constructor(
    private val dao: RepaymentDao
) {
    // CRUD
    suspend fun insert(repayment: Repayment) = dao.insert(repayment)
    suspend fun update(repayment: Repayment) = dao.update(repayment)
    suspend fun delete(repayment: Repayment) = dao.delete(repayment)
    suspend fun getById(repaymentId: String): Repayment? = dao.getById(repaymentId)

    // Borrowing-specific Queries
    suspend fun getAllByBorrowIdList(borrowId: String): List<Repayment> = dao.getByBorrowIdList(borrowId)
    fun getAllByBorrowId(borrowId: String): Flow<List<Repayment>> = dao.getByBorrowIdFlow(borrowId)
    suspend fun getLastRepayment(borrowId: String): Repayment? = dao.getLastRepayment(borrowId)

    // Aggregates
    suspend fun getTotalPrincipalRepaid(borrowId: String): Double = dao.getTotalPrincipalRepaid(borrowId)
    suspend fun getTotalPenaltyPaid(borrowId: String): Double = dao.getTotalPenaltyPaid(borrowId)
    suspend fun getBorrowingRepaymentSummary(borrowId: String): BorrowingRepaymentSummary =
        dao.getBorrowingRepaymentSummary(borrowId)

    // Shareholder Reports
    suspend fun getMonthlyShareholderReport(shareholderName: String): List<MonthlyRepaymentReport> =
        dao.getMonthlyShareholderReport(shareholderName)

    // Late Repayment Detection
    suspend fun getLateRepayments(shareholderName: String): List<Repayment> =
        dao.getLateRepayments(shareholderName)

    // Search
    suspend fun searchRepayments(query: String): List<Repayment> = dao.searchRepayments(query)

    // Detailed Info
    suspend fun getRepaymentsWithBorrowingDetails(borrowId: String): List<RepaymentWithBorrowingDetails> =
        dao.getRepaymentsWithBorrowingDetails(borrowId)
}
