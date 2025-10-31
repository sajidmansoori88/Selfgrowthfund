package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.OtherIncomeDao
import com.selfgrowthfund.sgf.data.local.entities.OtherIncome
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OtherIncomeRepository (Realtime Firestore <-> Room Sync)
 *
 * - Room is the single source of truth.
 * - Local writes mark isSynced = false.
 * - realtimeSyncRepository handles Firestore ↔ Room sync automatically.
 */
@Singleton
class OtherIncomeRepository @Inject constructor(
    private val dao: OtherIncomeDao,
    private val realtimeSyncRepository: RealtimeSyncRepository
) {

    // =============================
    // CRUD Operations
    // =============================

    suspend fun addIncome(otherIncome: OtherIncome): Result<Unit> = try {
        dao.insertIncome(otherIncome.copy(isSynced = false))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateIncome(otherIncome: OtherIncome): Result<Unit> = try {
        dao.updateIncome(otherIncome.copy(isSynced = false, updatedAt = Instant.now()))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteIncome(otherIncome: OtherIncome): Result<Unit> = try {
        dao.deleteIncome(otherIncome)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun clearAll(): Result<Unit> = try {
        dao.clearAllIncomes()
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // =============================
    // Queries
    // =============================

    suspend fun getIncomesForUser(userId: String): List<OtherIncome> = dao.getIncomesByUser(userId)
    suspend fun getAllIncomes(): List<OtherIncome> = dao.getAllIncomes()
    suspend fun findById(incomeId: Long): OtherIncome? = dao.findById(incomeId)

    // =============================
    // Approval Workflow
    // =============================

    suspend fun approve(
        incomeId: Long,
        approverId: String?,
        notes: String?,
        newStatus: ApprovalStage
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val rows = dao.updateApprovalStatus(
                incomeId = incomeId,
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
        incomeId: Long,
        rejectedBy: String?,
        notes: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val rows = dao.updateApprovalStatus(
                incomeId = incomeId,
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

    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<OtherIncome> =
        dao.getApprovalsBetween(start, end)
}
