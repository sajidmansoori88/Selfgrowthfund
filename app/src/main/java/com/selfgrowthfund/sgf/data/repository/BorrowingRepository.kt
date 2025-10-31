package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.BorrowingDao
import com.selfgrowthfund.sgf.data.local.dao.ShareholderDao
import com.selfgrowthfund.sgf.data.local.dto.MemberBorrowingStatus
import com.selfgrowthfund.sgf.data.local.entities.ApprovalFlow
import com.selfgrowthfund.sgf.data.local.entities.Borrowing
import com.selfgrowthfund.sgf.model.enums.*
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

/**
 * BorrowingRepository (Refactored for Realtime Firestore <-> Room Sync)
 *
 * - Local writes mark isSynced = false.
 * - Triggers realtimeSyncRepository.pushAllUnsynced() to push queued changes.
 * - Firestore changes handled automatically by RealtimeSyncRepository listener.
 */
class BorrowingRepository @Inject constructor(
    private val borrowingDao: BorrowingDao,
    private val shareholderDao: ShareholderDao,
    private val approvalFlowRepository: ApprovalFlowRepository,
    private val dates: Dates,
    private val realtimeSyncRepository: RealtimeSyncRepository
) {

    // =============================
    // Member Borrowing Application
    // =============================
    suspend fun applyForBorrowing(
        shareholderId: String,
        shareholderName: String,
        amountRequested: Double,
        createdBy: String,
        notes: String? = null
    ): Result<Borrowing> = try {
        val shareholder = shareholderDao.getShareholderById(shareholderId)
            ?: throw Exception("Shareholder not found")

        val shareValue = shareholder.shareBalance * shareholder.sharePrice
        val eligibility = Borrowing.calculateEligibility(shareValue)
        val today = dates.today()

        val provisional = Borrowing(
            provisionalId = "PV${System.currentTimeMillis()}",
            borrowId = null,
            shareholderId = shareholderId,
            shareholderName = shareholderName,
            applicationDate = today,
            amountRequested = amountRequested,
            borrowEligibility = eligibility,
            approvedAmount = 0.0,
            borrowStartDate = today,
            dueDate = Borrowing.calculateDueDate(today),
            status = BorrowingStatus.PENDING,
            approvalStatus = ApprovalStage.PENDING,
            approvedBy = null,
            notes = notes,
            createdBy = createdBy,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isSynced = false
        )

        borrowingDao.insertBorrowing(provisional)
        realtimeSyncRepository.pushAllUnsynced() // 🔄 trigger sync

        // Create pending ApprovalFlow
        approvalFlowRepository.recordApproval(
            ApprovalFlow(
                entityType = ApprovalType.BORROWING,
                entityId = provisional.provisionalId,
                role = MemberRole.MEMBER,
                action = ApprovalAction.PENDING,
                approvedBy = shareholderId,
                remarks = "Borrowing request pending review"
            )
        )

        Result.Success(provisional)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // =============================
    // Treasurer/Admin Finalization
    // =============================
    suspend fun finalizeBorrowing(
        provisionalId: String,
        approvedBy: String,
        paymentReleaseDate: LocalDate = dates.today()
    ): Result<Borrowing> = try {
        val borrowing = borrowingDao.getByProvisionalId(provisionalId)
            ?: throw Exception("Borrowing not found")

        val finalId = generateNextBorrowingId()
        val dueDate = Borrowing.calculateDueDate(paymentReleaseDate)

        val finalized = borrowing.copy(
            borrowId = finalId,
            borrowStartDate = paymentReleaseDate,
            dueDate = dueDate,
            approvalStatus = ApprovalStage.APPROVED,
            status = BorrowingStatus.ACTIVE,
            approvedBy = approvedBy,
            updatedAt = Instant.now(),
            isSynced = false
        )

        borrowingDao.updateBorrowing(finalized)
        realtimeSyncRepository.pushAllUnsynced() // 🔄 sync finalized record

        Result.Success(finalized)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // =============================
    // CRUD Operations
    // =============================
    suspend fun createBorrowing(borrowing: Borrowing): Result<String> = try {
        val newId = generateNextBorrowingId()
        val borrowingWithId = borrowing.copy(borrowId = newId, isSynced = false)
        borrowingDao.insertBorrowing(borrowingWithId)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(newId)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateBorrowing(borrowing: Borrowing): Result<Unit> = try {
        borrowingDao.updateBorrowing(borrowing.copy(isSynced = false, updatedAt = Instant.now()))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteBorrowing(borrowId: String): Result<Unit> = try {
        val entity = borrowingDao.getBorrowingById(borrowId)
        if (entity != null) borrowingDao.deleteBorrowing(entity)
        realtimeSyncRepository.pushAllUnsynced() // Firestore listener handles remote delete
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // =============================
    // Treasurer Release / Approval
    // =============================
    suspend fun markPaymentReleased(
        provisionalId: String,
        treasurerId: String,
        note: String
    ): Boolean = try {
        val borrowing = borrowingDao.getByProvisionalId(provisionalId) ?: return false
        val updated = borrowing.copy(
            approvalStatus = ApprovalStage.APPROVED,
            approvedBy = treasurerId,
            notes = note,
            updatedAt = Instant.now(),
            isSynced = false
        )
        borrowingDao.update(updated)
        realtimeSyncRepository.pushAllUnsynced()
        true
    } catch (e: Exception) {
        false
    }

    // =============================
    // Queries
    // =============================
    fun getAllBorrowings(): Flow<List<Borrowing>> = borrowingDao.getAllBorrowings()
    fun getBorrowingsByShareholder(shareholderId: String): Flow<List<Borrowing>> =
        borrowingDao.getBorrowingsByShareholder(shareholderId)
    fun getBorrowingsByStatus(status: BorrowingStatus): Flow<List<Borrowing>> =
        borrowingDao.getBorrowingsByStatus(status.name)

    suspend fun getBorrowingById(borrowId: String): Borrowing =
        borrowingDao.getBorrowingById(borrowId)
            ?: throw Exception("Borrowing not found: $borrowId")

    suspend fun getByProvisionalId(id: String): Borrowing? =
        borrowingDao.getByProvisionalId(id)

    // =============================
    // Status Updates
    // =============================
    suspend fun updateBorrowingStatus(borrowId: String, status: BorrowingStatus): Result<Unit> = try {
        val closedDate = if (BorrowingStatus.getClosed().contains(status)) dates.today() else null
        borrowingDao.updateBorrowingStatus(borrowId, status.name, closedDate)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // =============================
    // Approval Flow Hooks
    // =============================
    suspend fun approve(provisionalId: String, approverId: String? = null, notes: String? = null) {
        borrowingDao.updateApprovalStatus(
            provisionalId = provisionalId,
            status = ApprovalStage.APPROVED,
            approvedBy = approverId,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
        realtimeSyncRepository.pushAllUnsynced()
    }

    suspend fun reject(provisionalId: String, rejectedBy: String? = null, notes: String? = null) {
        borrowingDao.updateApprovalStatus(
            provisionalId = provisionalId,
            status = ApprovalStage.REJECTED,
            approvedBy = rejectedBy,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
        realtimeSyncRepository.pushAllUnsynced()
    }

    // =============================
    // Utility
    // =============================
    private suspend fun generateNextBorrowingId(): String {
        val lastId = borrowingDao.getLastBorrowingId()
        val numeric = lastId?.removePrefix("BR")?.toIntOrNull() ?: 0
        return "BR" + String.format(Locale.US, "%04d", numeric + 1)
    }

    fun getByBorrowIdFlow(id: String): Flow<Borrowing?> = borrowingDao.getByBorrowIdFlow(id)
    suspend fun getMemberBorrowingStatus(): List<MemberBorrowingStatus> = borrowingDao.getMemberBorrowingStatus()

    // --- Helper wrappers for ApprovalRepository compatibility ---
    private suspend fun countByStatus(
        status: ApprovalStage,
        start: LocalDate,
        end: LocalDate
    ): com.selfgrowthfund.sgf.utils.Result<Int> = try {
        val count = borrowingDao.countByStatus(status, start, end)
        com.selfgrowthfund.sgf.utils.Result.Success(count)
    } catch (e: Exception) {
        com.selfgrowthfund.sgf.utils.Result.Error(e)
    }

    suspend fun countApproved(start: LocalDate, end: LocalDate) =
        countByStatus(ApprovalStage.APPROVED, start, end)

    suspend fun countRejected(start: LocalDate, end: LocalDate) =
        countByStatus(ApprovalStage.REJECTED, start, end)

    suspend fun countPending(start: LocalDate, end: LocalDate) =
        countByStatus(ApprovalStage.PENDING, start, end)
    // =============================
    // Treasurer Dashboard Helpers
    // =============================

    /**
     * Returns borrowings that have been approved by admin
     * but not yet released by Treasurer.
     */
    suspend fun getApprovedPendingRelease(): List<Borrowing> = try {
        borrowingDao.getByApprovalStatus(ApprovalStage.TREASURER_APPROVED)
    } catch (e: Exception) {
        emptyList()
    }

    /**
     * Checks if 2/3 of total active members have approved a borrowing.
     */
    suspend fun isBorrowingApprovalQuorumMet(provisionalId: String): Boolean = try {
        val approvedCount = approvalFlowRepository.countApprovedByEntity(
            provisionalId,
            ApprovalType.BORROWING
        )
        val totalMembers = shareholderDao.getActiveMemberCount()
        val required = kotlin.math.ceil(totalMembers * (2.0 / 3.0)).toInt()
        approvedCount >= required
    } catch (e: Exception) {
        false
    }

    /**
     * Exposes active member count safely for ViewModels.
     */
    suspend fun getActiveMemberCount(): Int = try {
        shareholderDao.getActiveMemberCount()
    } catch (e: Exception) {
        0
    }
    // -------------------------------------------------------------
// 🩹 Compatibility wrapper for older ViewModels
// -------------------------------------------------------------
    suspend fun updateApprovalStage(
        borrowId: String,
        newStage: ApprovalStage,
        approvedBy: String? = null,
        notes: String? = null
    ) {
        when (newStage) {
            ApprovalStage.APPROVED -> approve(
                provisionalId = borrowId,
                approverId = approvedBy,
                notes = notes
            )
            ApprovalStage.REJECTED -> reject(
                provisionalId = borrowId,
                rejectedBy = approvedBy,
                notes = notes
            )
            else -> {
                // For other intermediate stages (e.g. TREASURER_APPROVED, MEMBER_APPROVED)
                borrowingDao.updateApprovalStatus(
                    provisionalId = borrowId,
                    status = newStage,
                    approvedBy = approvedBy,
                    notes = notes,
                    timestamp = System.currentTimeMillis()
                )
                realtimeSyncRepository.pushAllUnsynced()
            }
        }
    }
    // ============================================================
// ===============   Overdue Borrowings   ======================
// ============================================================
    fun getOverdueBorrowings(): Flow<List<Borrowing>> {
        return getAllBorrowings().map { borrowings ->
            borrowings.filter { it.isOverdue() }
        }
    }


}
