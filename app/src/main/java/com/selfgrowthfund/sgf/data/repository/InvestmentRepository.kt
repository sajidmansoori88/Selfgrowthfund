package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.InvestmentDao
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.model.enums.InvesteeType
import com.selfgrowthfund.sgf.model.enums.InvestmentStatus
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.IdGenerator
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * InvestmentRepository (Realtime Firestore <-> Room Sync)
 *
 * - Room is the single source of truth.
 * - Local writes mark isSynced = false and trigger realtimeSyncRepository.pushAllUnsynced().
 * - Firestore updates handled by RealtimeSyncRepository global listener.
 */
@Singleton
class InvestmentRepository @Inject constructor(
    private val dao: InvestmentDao,
    private val realtimeSyncRepository: RealtimeSyncRepository,
    private val dates: Dates
) {

    // =========================
    // Basic CRUD (Local + Sync)
    // =========================

    suspend fun createInvestment(investment: Investment): Result<Unit> = try {
        dao.insert(investment.copy(isSynced = false))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateInvestment(investment: Investment): Result<Unit> = try {
        dao.update(investment.copy(isSynced = false))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteInvestment(provisionalId: String): Result<Unit> = try {
        dao.deleteByProvisionalId(provisionalId)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // =========================
    // Queries
    // =========================

    suspend fun getByProvisionalId(id: String): Investment? = dao.getByProvisionalId(id)
    suspend fun getByInvestmentId(id: String): Investment? = dao.getByInvestmentId(id)

    fun getByProvisionalIdFlow(id: String): Flow<Investment?> = dao.getByProvisionalIdFlow(id)
    fun getByInvestmentIdFlow(id: String): Flow<Investment?> = dao.getByInvestmentIdFlow(id)

    fun getAllInvestments(): Flow<List<Investment>> = dao.getAllInvestmentsFlow()
    fun getActiveInvestments(): Flow<List<Investment>> = dao.getByStatus(InvestmentStatus.Active)
    fun getInvestmentsByType(type: InvesteeType): Flow<List<Investment>> = dao.getByInvesteeType(type)

    // =========================
    // Business Logic
    // =========================

    suspend fun changeInvestmentStatus(provisionalId: String, newStatus: InvestmentStatus): Result<Unit> = try {
        val investment = dao.getByProvisionalId(provisionalId) ?: throw Exception("Investment not found")
        dao.update(investment.copy(status = newStatus, isSynced = false))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getInvestmentsDueSoon(daysThreshold: Int = 7): Result<List<Investment>> = try {
        val today = LocalDate.now()
        val thresholdDate = today.plusDays(daysThreshold.toLong())
        Result.Success(dao.getDueBetween(today, thresholdDate))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun searchInvestments(query: String): Result<List<Investment>> = try {
        Result.Success(dao.search(query))
    } catch (e: Exception) {
        Result.Error(e)
    }

    // =========================
    // Aggregates
    // =========================

    suspend fun getTotalActiveInvestmentValue(): Result<Double> = try {
        Result.Success(dao.getTotalActiveAmount() ?: 0.0)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getActiveInvestmentCount(): Result<Int> = try {
        Result.Success(dao.getActiveCount())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getInvestmentSummary(): Result<InvestmentDao.InvestmentSummary> = try {
        Result.Success(dao.getInvestmentSummary())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getPendingForTreasurer(): List<Investment> =
        dao.getByApprovalStatus(ApprovalStage.PENDING)

    suspend fun getApprovedPendingRelease(): List<Investment> =
        dao.getByApprovalStatus(ApprovalStage.TREASURER_APPROVED)

    // =========================
    // Approval Workflow
    // =========================

    suspend fun approve(
        provisionalId: String,
        approverId: String?,
        notes: String?,
        newStatus: ApprovalStage
    ): Result<Unit> = try {
        val updatedAt = LocalDate.now()
        dao.updateApprovalStatus(provisionalId, newStatus, approverId, notes, updatedAt)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun reject(
        provisionalId: String,
        rejectedBy: String?,
        notes: String?
    ): Result<Unit> = try {
        val updatedAt = LocalDate.now()
        dao.updateApprovalStatus(provisionalId, ApprovalStage.REJECTED, rejectedBy, notes, updatedAt)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun approveAndAssignId(
        provisionalId: String,
        approverId: String?,
        notes: String?
    ): Result<String> = try {
        val investment = dao.getByProvisionalId(provisionalId) ?: throw Exception("Investment not found")
        val lastId = dao.getLastApprovedInvestmentId()
        val newId = IdGenerator.nextInvestmentId(lastId)
        val updatedInvestment = investment.copy(
            investmentId = newId,
            approvalStatus = ApprovalStage.ADMIN_APPROVED,
            approvedBy = approverId,
            approvalNotes = notes,
            investmentDate = investment.investmentDate ?: LocalDate.now(),
            updatedAt = LocalDate.now(),
            isSynced = false
        )
        dao.update(updatedInvestment)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(newId)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun markPaymentReleased(
        provisionalId: String,
        treasurerId: String?,
        remarks: String?
    ): Result<Unit> = try {
        val investment = dao.getByProvisionalId(provisionalId) ?: throw Exception("Investment not found")
        val updatedInvestment = investment.copy(
            approvalStatus = ApprovalStage.TREASURER_APPROVED,
            approvedBy = treasurerId,
            approvalNotes = remarks,
            investmentDate = LocalDate.now(),
            updatedAt = LocalDate.now(),
            isSynced = false
        )
        dao.update(updatedInvestment)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // =========================
    // Reports
    // =========================

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

    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<Investment> =
        dao.getApprovalsBetween(start, end)

    suspend fun findById(id: String): Investment? =
        dao.getByProvisionalId(id) ?: dao.getByInvestmentId(id)

    suspend fun updateApprovalStage(provisionalId: String, newStage: ApprovalStage): Result<Unit> = try {
        dao.updateApprovalStage(provisionalId, newStage.name)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
