package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.RepaymentDao
import com.selfgrowthfund.sgf.data.local.dao.RepaymentDao.BorrowingRepaymentSummary
import com.selfgrowthfund.sgf.data.local.dto.RepaymentSummaryDTO
import com.selfgrowthfund.sgf.data.local.entities.Borrowing
import com.selfgrowthfund.sgf.data.local.entities.Repayment
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.utils.IdGenerator
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Refactored RepaymentRepository
 *
 * Uses Room as single source of truth and delegates Firestore sync
 * to RealtimeSyncRepository (which already handles both directions).
 */
@Singleton
class RepaymentRepository @Inject constructor(
    private val dao: RepaymentDao,
    private val borrowingRepository: BorrowingRepository,
    private val realtimeSyncRepository: RealtimeSyncRepository
) {

    // =========================
    // Basic CRUD (Local + Sync)
    // =========================

    suspend fun insert(repayment: Repayment): Result<Unit> = try {
        dao.insert(repayment.copy(isSynced = false))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun update(repayment: Repayment): Result<Unit> = try {
        dao.update(repayment.copy(isSynced = false))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun delete(repayment: Repayment): Result<Unit> = try {
        dao.delete(repayment)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteByProvisionalId(provisionalId: String): Result<Unit> = try {
        dao.deleteByProvisionalId(provisionalId)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // =========================
    // Queries
    // =========================

    suspend fun getByProvisionalId(provisionalId: String): Repayment? =
        dao.getByProvisionalId(provisionalId)

    suspend fun findById(provisionalId: String): Repayment? =
        dao.findById(provisionalId)

    suspend fun getAllByBorrowIdList(borrowId: String): List<Repayment> =
        dao.getByBorrowIdList(borrowId)

    fun getAllByBorrowId(borrowId: String): Flow<List<Repayment>> =
        dao.getByBorrowId(borrowId)

    suspend fun getLastRepayment(borrowId: String): Repayment? =
        dao.getLastRepayment(borrowId)

    suspend fun getTotalPrincipalRepaid(borrowId: String): Double =
        dao.getTotalPrincipalRepaid(borrowId)

    suspend fun getTotalPenaltyPaid(borrowId: String): Double =
        dao.getTotalPenaltyPaid(borrowId)

    suspend fun getBorrowingRepaymentSummary(borrowId: String): BorrowingRepaymentSummary =
        dao.getBorrowingRepaymentSummary(borrowId)

    suspend fun getBorrowingById(borrowId: String): Borrowing =
        borrowingRepository.getBorrowingById(borrowId)

    suspend fun getRepaymentsByBorrowId(borrowId: String): List<Repayment> =
        dao.getByBorrowIdList(borrowId)

    fun getAllRepayments(): Flow<List<Repayment>> = dao.getAllRepayments()

    fun getRepaymentSummaries(): Flow<List<RepaymentSummaryDTO>> =
        dao.getRepaymentSummaries()

    suspend fun getLateRepayments(): List<Repayment> = dao.getLateRepayments()
    suspend fun searchRepayments(query: String): List<Repayment> = dao.searchRepayments(query)
    suspend fun getLastRepaymentId(): String? = dao.getLastRepaymentId()

    suspend fun getPendingForTreasurer(): List<Repayment> =
        dao.getByApprovalStatus(ApprovalStage.PENDING)

    suspend fun getApprovedPendingRelease(): List<Repayment> =
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
        dao.updateApprovalStatus(
            provisionalId = provisionalId,
            status = newStatus,
            approvedBy = approverId,
            notes = notes,
            updatedAt = System.currentTimeMillis()
        )
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
        dao.updateApprovalStatus(
            provisionalId = provisionalId,
            status = ApprovalStage.REJECTED,
            approvedBy = rejectedBy,
            notes = notes,
            updatedAt = System.currentTimeMillis()
        )
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
        val lastId = dao.getLastApprovedRepaymentId()
        val newId = IdGenerator.nextRepaymentId(lastId)
        val updatedAt = LocalDate.now()
        dao.approveRepayment(
            provisionalId = provisionalId,
            newId = newId,
            status = ApprovalStage.ADMIN_APPROVED,
            approvedBy = approverId,
            notes = notes,
            updatedAt = updatedAt
        )
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(newId)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // =========================
// Counting & Reporting
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

    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<Repayment> =
        dao.getApprovalsBetween(start, end)



}
