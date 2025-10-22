package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.BorrowingDao
import com.selfgrowthfund.sgf.data.local.dao.ShareholderDao
import com.selfgrowthfund.sgf.data.local.dto.MemberBorrowingStatus
import com.selfgrowthfund.sgf.data.local.entities.ApprovalFlow
import com.selfgrowthfund.sgf.data.local.entities.Borrowing
import com.selfgrowthfund.sgf.model.enums.ApprovalAction
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.model.enums.ApprovalType
import com.selfgrowthfund.sgf.model.enums.BorrowingStatus
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

class BorrowingRepository @Inject constructor(
    private val borrowingDao: BorrowingDao,
    val shareholderDao: ShareholderDao,
    private val approvalFlowRepository: ApprovalFlowRepository,
    private val dates: Dates
) {

    // ============================================================
    // ===============  B2 — Application (Provisional)  ============
    // ============================================================

    /**
     * Member applies for a borrowing request.
     * - Generates a provisionalId (PV<timestamp>).
     * - Keeps borrowId = null until Treasurer/Admin final approval.
     * - Computes eligibility = 90% of total share value (auto-calculated from DB).
     */
    suspend fun applyForBorrowing(
        shareholderId: String,
        shareholderName: String,
        amountRequested: Double,
        createdBy: String,
        notes: String? = null
    ): Result<Borrowing> = try {
        // ✅ Automatically compute total share value from Shareholder table
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
            borrowStartDate = today, // placeholder, updated on release
            dueDate = Borrowing.calculateDueDate(today),
            status = BorrowingStatus.PENDING,
            approvalStatus = ApprovalStage.PENDING,
            approvedBy = null,
            notes = notes,
            createdBy = createdBy,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        borrowingDao.insertBorrowing(provisional)

        // ✅ Create ApprovalFlow entry for multi-member review
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

    // ============================================================
    // ===============  B4 — Finalization (Treasurer/Admin)  ======
    // ============================================================

    suspend fun finalizeBorrowing(
        provisionalId: String,
        approvedBy: String,
        paymentReleaseDate: LocalDate = dates.today()
    ): Result<Borrowing> = try {
        val borrowing = borrowingDao.getByProvisionalId(provisionalId)
            ?: throw Exception("Borrowing not found for provisionalId: $provisionalId")

        val finalId = generateNextBorrowingId()
        val dueDate = Borrowing.calculateDueDate(paymentReleaseDate)

        val finalized = borrowing.copy(
            borrowId = finalId,
            borrowStartDate = paymentReleaseDate,
            dueDate = dueDate,
            approvalStatus = ApprovalStage.APPROVED,
            status = BorrowingStatus.ACTIVE,
            approvedBy = approvedBy,
            updatedAt = Instant.now()
        )

        borrowingDao.updateBorrowing(finalized)
        Result.Success(finalized)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // ============================================================
    // ===============  B3 — Quorum Calculation ===================
    // ============================================================

    suspend fun isBorrowingApprovalQuorumMet(provisionalId: String): Boolean {
        val totalMembers = shareholderDao.getActiveMemberCount()
        if (totalMembers == 0) return false

        val approvedCount = approvalFlowRepository.countApprovedByEntity(
            provisionalId,
            ApprovalType.BORROWING
        )

        val required = kotlin.math.ceil(totalMembers * (2.0 / 3.0)).toInt()
        return approvedCount >= required
    }

    // ============================================================
    // ===============  CRUD Operations  ===========================
    // ============================================================

    suspend fun createBorrowing(borrowing: Borrowing): Result<String> = try {
        val newId = generateNextBorrowingId()
        val borrowingWithId = borrowing.copy(borrowId = newId)
        borrowingDao.insertBorrowing(borrowingWithId)
        Result.Success(newId)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateBorrowing(borrowing: Borrowing): Result<Unit> = try {
        borrowingDao.updateBorrowing(borrowing)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteBorrowing(borrowId: String): Result<Unit> = try {
        borrowingDao.deleteById(borrowId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // ============================================================
    // ===============  Treasurer Release / Approval  =============
    // ============================================================

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
            updatedAt = Instant.now()
        )
        borrowingDao.update(updated)
        true
    } catch (e: Exception) {
        false
    }

    // ============================================================
    // ===============  Query Operations  ==========================
    // ============================================================

    fun getAllBorrowings(): Flow<List<Borrowing>> = borrowingDao.getAllBorrowings()

    suspend fun getBorrowingById(borrowId: String): Borrowing {
        return borrowingDao.getBorrowingById(borrowId)
            ?: throw Exception("Borrowing not found with ID: $borrowId")
    }

    suspend fun getBorrowingByIdWithResult(borrowId: String): Result<Borrowing> = try {
        val borrowing = borrowingDao.getBorrowingById(borrowId)
            ?: throw Exception("Borrowing not found with ID: $borrowId")
        Result.Success(borrowing)
    } catch (e: Exception) {
        Result.Error(e)
    }

    fun getBorrowingsByShareholder(shareholderId: String): Flow<List<Borrowing>> =
        borrowingDao.getBorrowingsByShareholder(shareholderId)

    fun getBorrowingsByStatus(status: BorrowingStatus): Flow<List<Borrowing>> =
        borrowingDao.getBorrowingsByStatus(status.name)

    suspend fun getPendingForTreasurer(): List<Borrowing> =
        borrowingDao.getByApprovalStatus(ApprovalStage.PENDING)

    suspend fun getApprovedPendingRelease(): List<Borrowing> =
        borrowingDao.getByApprovalStatus(ApprovalStage.TREASURER_APPROVED)

    // ============================================================
    // ===============  Status Management  ========================
    // ============================================================

    suspend fun updateBorrowingStatus(
        borrowId: String,
        status: BorrowingStatus
    ): Result<Unit> = try {
        val closedDate: LocalDate? = if (BorrowingStatus.getClosed().contains(status)) {
            dates.today()
        } else null
        borrowingDao.updateBorrowingStatus(borrowId, status.name, closedDate)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    fun getOverdueBorrowings(): Flow<List<Borrowing>> {
        val today = dates.today()
        return borrowingDao.getOverdueBorrowings(today)
    }

    suspend fun getActiveLoanCount(shareholderId: String): Result<Int> = try {
        Result.Success(borrowingDao.getActiveLoanCount(shareholderId))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getTotalActiveLoanAmount(shareholderId: String): Result<Double> = try {
        Result.Success(borrowingDao.getTotalActiveLoanAmount(shareholderId))
    } catch (e: Exception) {
        Result.Error(e)
    }

    // ============================================================
    // ===============  Approval Flow Methods  ====================
    // ============================================================

    suspend fun approve(provisionalId: String, approverId: String? = null, notes: String? = null) {
        borrowingDao.updateApprovalStatus(
            provisionalId = provisionalId,
            status = ApprovalStage.APPROVED,
            approvedBy = approverId,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun reject(provisionalId: String, rejectedBy: String? = null, notes: String? = null) {
        borrowingDao.updateApprovalStatus(
            provisionalId = provisionalId,
            status = ApprovalStage.REJECTED,
            approvedBy = rejectedBy,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun countApproved(start: LocalDate, end: LocalDate) =
        borrowingDao.countByStatus(ApprovalStage.APPROVED, start, end)

    suspend fun countRejected(start: LocalDate, end: LocalDate) =
        borrowingDao.countByStatus(ApprovalStage.REJECTED, start, end)

    suspend fun countPending(start: LocalDate, end: LocalDate) =
        borrowingDao.countByStatus(ApprovalStage.PENDING, start, end)

    suspend fun countTotal(start: LocalDate, end: LocalDate) =
        borrowingDao.countTotal(start, end)

    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<Borrowing> =
        borrowingDao.getApprovalsBetween(start, end)

    suspend fun findById(borrowingId: String) = borrowingDao.findById(borrowingId)

    // ============================================================
    // ===============  ID Generation Utilities  ==================
    // ============================================================

    private suspend fun generateNextBorrowingId(): String {
        val lastId = borrowingDao.getLastBorrowingId()
        val numeric = lastId?.removePrefix("BR")?.toIntOrNull() ?: 0
        return "BR" + String.format(Locale.US, "%04d", numeric + 1)
    }

    fun getByBorrowIdFlow(id: String): Flow<Borrowing?> = borrowingDao.getByBorrowIdFlow(id)

    suspend fun updateApprovalStage(borrowId: String, newStage: ApprovalStage) {
        borrowingDao.updateApprovalStage(borrowId, newStage.name)
    }
    suspend fun getMemberBorrowingStatus(): List<MemberBorrowingStatus> {
        return borrowingDao.getMemberBorrowingStatus()
    }
}
