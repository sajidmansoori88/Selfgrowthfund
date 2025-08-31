package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.RepaymentDao
import com.selfgrowthfund.sgf.data.local.entities.Repayment
import com.selfgrowthfund.sgf.data.local.dao.RepaymentDao.BorrowingRepaymentSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RepaymentRepository @Inject constructor(
    private val dao: RepaymentDao
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
}