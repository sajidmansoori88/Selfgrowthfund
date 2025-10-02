package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.OtherExpenseDao
import com.selfgrowthfund.sgf.data.local.entities.OtherExpense
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtherExpenseRepository @Inject constructor(
    private val dao: OtherExpenseDao
) {

    // --- CRUD ---
    suspend fun addExpense(otherExpense: OtherExpense) = dao.insertExpense(otherExpense)

    suspend fun getExpensesForUser(userId: String): List<OtherExpense> = dao.getExpensesByUser(userId)

    suspend fun getAllExpenses(): List<OtherExpense> = dao.getAllExpenses()

    suspend fun deleteExpense(otherExpense: OtherExpense) = dao.deleteExpense(otherExpense)

    suspend fun clearAll() = dao.clearAllExpenses()

    // --- Approval workflow ---
    suspend fun approve(
        expenseId: Long,
        approverId: String?,
        notes: String?,
        newStatus: ApprovalStage
    ): Boolean = withContext(Dispatchers.IO) {
        val rows = dao.updateApprovalStatus(
            expenseId = expenseId,
            status = newStatus,
            approvedBy = approverId,
            notes = notes,
            updatedAt = Instant.now()
        )
        rows > 0
    }

    suspend fun reject(
        expenseId: Long,
        rejectedBy: String?,
        notes: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val rows = dao.updateApprovalStatus(
            expenseId = expenseId,
            status = ApprovalStage.REJECTED,
            approvedBy = rejectedBy,
            notes = notes,
            updatedAt = Instant.now()
        )
        rows > 0
    }

    // --- Reports ---
    suspend fun countApproved(start: LocalDate, end: LocalDate) =
        dao.countByStatus(ApprovalStage.APPROVED, start, end)

    suspend fun countRejected(start: LocalDate, end: LocalDate) =
        dao.countByStatus(ApprovalStage.REJECTED, start, end)

    suspend fun countPending(start: LocalDate, end: LocalDate) =
        dao.countByStatus(ApprovalStage.PENDING, start, end)

    suspend fun countTotal(start: LocalDate, end: LocalDate) =
        dao.countTotal(start, end)

    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<OtherExpense> =
        dao.getApprovalsBetween(start, end)

    suspend fun findById(expenseId: Long): OtherExpense? = dao.findById(expenseId)
}
