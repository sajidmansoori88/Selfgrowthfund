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

@Singleton
class InvestmentReturnsRepository @Inject constructor(
    private val returnsDao: InvestmentReturnsDao,
    private val investmentDao: InvestmentDao,
    dates: Dates
) {

    // ─────────────── Add / Insert ───────────────
    suspend fun addReturn(returnEntity: InvestmentReturns): Result<Unit> = try {
        returnsDao.insertReturn(returnEntity)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // ─────────────── Reactive (Flows) ───────────────
    fun getReturnsByInvestmentId(investmentId: String): Flow<List<InvestmentReturns>> =
        returnsDao.getReturnsByInvestmentIdFlow(investmentId)

    // ─────────────── Queries (suspend, one-shot) ───────────────
    suspend fun getReturnsForInvestment(investmentId: String): List<InvestmentReturns> =
        returnsDao.getReturnsByInvestmentId(investmentId)

    suspend fun getTotalProfitForInvestment(investmentId: String): Double =
        returnsDao.getReturnsByInvestmentId(investmentId).sumOf { it.actualProfitAmount }

    suspend fun getTotalReturnedAmount(investmentId: String): Double =
        returnsDao.getReturnsByInvestmentId(investmentId).sumOf { it.amountReceived }

    suspend fun getPendingForTreasurer(): List<InvestmentReturns> {
        return returnsDao.getByApprovalStatus(ApprovalStage.PENDING)
    }

    suspend fun getApprovedPendingRelease(): List<InvestmentReturns> {
        return returnsDao.getByApprovalStatus(ApprovalStage.TREASURER_APPROVED)
    }

    suspend fun approveByTreasurer(
        provisionalId: String,
        treasurerId: String,
        note: String
    ): Boolean {
        return try {
            val existing = returnsDao.getByProvisionalId(provisionalId) ?: return false
            val updated = existing.copy(
                approvalStatus = ApprovalStage.TREASURER_APPROVED,
                approvedBy = treasurerId,
                approvalNotes = note,
                updatedAt = java.time.LocalDate.now()
            )
            returnsDao.update(updated)
            true
        } catch (e: Exception) {
            false
        }
    }



    // ─────────────── Approval workflow ───────────────
    suspend fun approve(
        returnId: String,
        approverId: String?,
        notes: String?,
        newStatus: ApprovalStage
    ): Boolean = withContext(Dispatchers.IO) {
        val updatedAt = LocalDate.now()
        val rows = returnsDao.updateApprovalStatus(returnId, newStatus, approverId, notes, updatedAt)
        rows > 0
    }

    suspend fun reject(
        returnId: String,
        rejectedBy: String?,
        notes: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val updatedAt = LocalDate.now()
        val rows = returnsDao.updateApprovalStatus(returnId, ApprovalStage.REJECTED, rejectedBy, notes, updatedAt)
        rows > 0
    }

    // ─────────────── Reports ───────────────
    suspend fun countApproved(start: LocalDate, end: LocalDate) =
        returnsDao.countByStatus(ApprovalStage.APPROVED, start, end)

    suspend fun countRejected(start: LocalDate, end: LocalDate) =
        returnsDao.countByStatus(ApprovalStage.REJECTED, start, end)

    suspend fun countPending(start: LocalDate, end: LocalDate) =
        returnsDao.countByStatus(ApprovalStage.PENDING, start, end)

    suspend fun countTotal(start: LocalDate, end: LocalDate) =
        returnsDao.countTotal(start, end)

    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<InvestmentReturns> =
        returnsDao.getApprovalsBetween(start, end)

    suspend fun findById(returnsId: String) = returnsDao.findById(returnsId)
}
