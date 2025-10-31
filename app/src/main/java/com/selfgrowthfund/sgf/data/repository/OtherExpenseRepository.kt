package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.OtherExpenseDao
import com.selfgrowthfund.sgf.data.local.entities.OtherExpense
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OtherExpenseRepository (Realtime Firestore <-> Room Sync)
 *
 * - All local writes mark isSynced = false.
 * - Automatically pushes to Firestore via realtimeSyncRepository.pushAllUnsynced().
 * - Firestore listeners (RealtimeSyncRepository) already handle remote -> local sync.
 */
@Singleton
class OtherExpenseRepository @Inject constructor(
    private val dao: OtherExpenseDao,
    private val realtimeSyncRepository: RealtimeSyncRepository
) {

    // =============================
    // CRUD Operations
    // =============================

    suspend fun addExpense(otherExpense: OtherExpense): Result<Unit> = try {
        dao.insertExpense(otherExpense.copy(isSynced = false))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateExpense(otherExpense: OtherExpense): Result<Unit> = try {
        dao.updateExpense(otherExpense.copy(isSynced = false, updatedAt = Instant.now()))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteExpense(otherExpense: OtherExpense): Result<Unit> = try {
        dao.deleteExpense(otherExpense)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun clearAll(): Result<Unit> = try {
        dao.clearAllExpenses()
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // =============================
    // Queries
    // =============================
    suspend fun getExpensesForUser(userId: String): List<OtherExpense> = dao.getExpensesByUser(userId)
    suspend fun getAllExpenses(): List<OtherExpense> = dao.getAllExpenses()
    suspend fun findById(expenseId: Long): OtherExpense? = dao.findById(expenseId)

    // =============================
    // Approval Workflow
    // =============================

    suspend fun approve(
        expenseId: Long,
        approverId: String?,
        notes: String?,
        newStatus: ApprovalStage
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val rows = dao.updateApprovalStatus(
                expenseId = expenseId,
                status = newStatus,
                approvedBy = approverId,
                notes = notes,
                updatedAt = Instant.now()
            )
            if (rows > 0) {
                realtimeSyncRepository.pushAllUnsynced()
                Result.Success(Unit)
            } else Result.Error(Exception("No rows updated"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun reject(
        expenseId: Long,
        rejectedBy: String?,
        notes: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val rows = dao.updateApprovalStatus(
                expenseId = expenseId,
                status = ApprovalStage.REJECTED,
                approvedBy = rejectedBy,
                notes = notes,
                updatedAt = Instant.now()
            )
            if (rows > 0) {
                realtimeSyncRepository.pushAllUnsynced()
                Result.Success(Unit)
            } else Result.Error(Exception("No rows updated"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // =============================
    // Reports (Approval Summary)
    // =============================

    suspend fun countByStatus(status: ApprovalStage, start: LocalDate, end: LocalDate): Result<Int> = try {
        Result.Success(dao.countByStatus(status, start, end))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun countApproved(start: LocalDate, end: LocalDate): Result<Int> =
        countByStatus(ApprovalStage.APPROVED, start, end)

    suspend fun countRejected(start: LocalDate, end: LocalDate): Result<Int> =
        countByStatus(ApprovalStage.REJECTED, start, end)

    suspend fun countPending(start: LocalDate, end: LocalDate): Result<Int> =
        countByStatus(ApprovalStage.PENDING, start, end)

    suspend fun countTotal(start: LocalDate, end: LocalDate): Result<Int> = try {
        Result.Success(dao.countTotal(start, end))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<OtherExpense> =
        dao.getApprovalsBetween(start, end)
}
