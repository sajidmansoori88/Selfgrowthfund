package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.RepaymentDao
import com.selfgrowthfund.sgf.data.local.entities.Borrowing
import com.selfgrowthfund.sgf.data.local.entities.Repayment
import com.selfgrowthfund.sgf.data.local.dao.RepaymentDao.BorrowingRepaymentSummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class RepaymentRepository @Inject constructor(
    private val dao: RepaymentDao,
    private val borrowingRepository: BorrowingRepository // Add this dependency
) {
    suspend fun insert(repayment: Repayment) = dao.insert(repayment)
    suspend fun update(repayment: Repayment) = dao.update(repayment)
    suspend fun delete(repayment: Repayment) = dao.delete(repayment)
    suspend fun getById(repaymentId: String): Repayment? = dao.getById(repaymentId)

    suspend fun getAllByBorrowIdList(borrowId: String): List<Repayment> = dao.getByBorrowIdList(borrowId)
    fun getAllByBorrowId(borrowId: String): Flow<List<Repayment>> = dao.getByBorrowId(borrowId)
    suspend fun getLastRepayment(borrowId: String): Repayment? = dao.getLastRepayment(borrowId)

    suspend fun getTotalPrincipalRepaid(borrowId: String): Double = dao.getTotalPrincipalRepaid(borrowId)
    suspend fun getTotalPenaltyPaid(borrowId: String): Double = dao.getTotalPenaltyPaid(borrowId)
    suspend fun getBorrowingRepaymentSummary(borrowId: String): RepaymentDao.BorrowingRepaymentSummary =
        dao.getBorrowingRepaymentSummary(borrowId)

    suspend fun getLateRepayments(): List<Repayment> = dao.getLateRepayments()
    suspend fun searchRepayments(query: String): List<Repayment> = dao.searchRepayments(query)

    suspend fun getLastRepaymentId(): String? = dao.getLastRepaymentId()

    // NEW METHODS REQUIRED BY VIEWMODEL
    suspend fun getBorrowingById(borrowId: String): Borrowing {
        return borrowingRepository.getBorrowingById(borrowId)
    }

    suspend fun getRepaymentsByBorrowId(borrowId: String): List<Repayment> {
        return dao.getByBorrowIdList(borrowId)
    }
    suspend fun countApproved(start: LocalDate, end: LocalDate) =
        dao.countByStatus("APPROVED", start, end)

    suspend fun countRejected(start: LocalDate, end: LocalDate) =
        dao.countByStatus("REJECTED", start, end)

    suspend fun countPending(start: LocalDate, end: LocalDate) =
        dao.countByStatus("PENDING", start, end)

    suspend fun countTotal(start: LocalDate, end: LocalDate) =
        dao.countTotal(start, end)
}