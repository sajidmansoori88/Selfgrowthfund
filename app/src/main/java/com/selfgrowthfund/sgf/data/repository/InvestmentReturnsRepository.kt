package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.InvestmentDao
import com.selfgrowthfund.sgf.data.local.dao.InvestmentReturnsDao
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * InvestmentReturnsRepository (Realtime Firestore <-> Room Sync)
 *
 * - All local writes set isSynced = false.
 * - Triggers realtimeSyncRepository.pushAllUnsynced() to sync with Firestore.
 * - Firestore -> Room updates handled by RealtimeSyncRepository global listener.
 */
@Singleton
class InvestmentReturnsRepository @Inject constructor(
    private val returnsDao: InvestmentReturnsDao,
    private val investmentDao: InvestmentDao,
    private val realtimeSyncRepository: RealtimeSyncRepository,
    private val dates: Dates
) {

    // =============================
    // Add / Insert
    // =============================
    suspend fun addReturn(returnEntity: InvestmentReturns): Result<Unit> = try {
        returnsDao.insertReturn(returnEntity.copy(isSynced = false))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // =============================
    // Reactive (Flows)
    // =============================
    fun getReturnsByInvestmentId(investmentId: String): Flow<List<InvestmentReturns>> =
        returnsDao.getReturnsByInvestmentIdFlow(investmentId)

    // =============================
    // Queries (one-shot)
    // =============================
    suspend fun getReturnsForInvestment(investmentId: String): List<InvestmentReturns> =
        returnsDao.getReturnsByInvestmentId(investmentId)

    suspend fun getTotalProfitForInvestment(investmentId: String): Double =
        returnsDao.getReturnsByInvestmentId(investmentId).sumOf { it.actualProfitAmount }

    suspend fun getTotalReturnedAmount(investmentId: String): Double =
        returnsDao.getReturnsByInvestmentId(investmentId).sumOf { it.amountReceived }

    suspend fun getPendingForTreasurer(): List<InvestmentReturns> =
        returnsDao.getByApprovalStatus(ApprovalStage.PENDING)

    suspend fun getApprovedPendingRelease(): List<InvestmentReturns> =
        returnsDao.getByApprovalStatus(ApprovalStage.TREASURER_APPROVED)

    // =============================
    // Approval Workflow
    // =============================
    suspend fun approveByTreasurer(
        provisionalId: String,
        treasurerId: String,
        note: String
    ): Result<Unit> = try {
        val existing = returnsDao.getByProvisionalId(provisionalId) ?: throw Exception("Return not found")
        val updated = existing.copy(
            approvalStatus = ApprovalStage.TREASURER_APPROVED,
            approvedBy = treasurerId,
            approvalNotes = note,
            updatedAt = LocalDate.now(),
            isSynced = false
        )
        returnsDao.update(updated)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun approve(
        returnId: String,
        approverId: String?,
        notes: String?,
        newStatus: ApprovalStage
    ): Result<Unit> = try {
        val updatedAt = LocalDate.now()
        returnsDao.updateApprovalStatus(returnId, newStatus, approverId, notes, updatedAt)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun reject(
        returnId: String,
        rejectedBy: String?,
        notes: String?
    ): Result<Unit> = try {
        val updatedAt = LocalDate.now()
        returnsDao.updateApprovalStatus(returnId, ApprovalStage.REJECTED, rejectedBy, notes, updatedAt)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // =============================
    // Reports
    // =============================

    suspend fun countByStatus(status: ApprovalStage, start: LocalDate, end: LocalDate): Result<Int> = try {
        Result.Success(returnsDao.countByStatus(status, start, end))
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
        Result.Success(returnsDao.countTotal(start, end))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<InvestmentReturns> =
        returnsDao.getApprovalsBetween(start, end)

    suspend fun findById(returnsId: String): InvestmentReturns? =
        returnsDao.findById(returnsId)
}
