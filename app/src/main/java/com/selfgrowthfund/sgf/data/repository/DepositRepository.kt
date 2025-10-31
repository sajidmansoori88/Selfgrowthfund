package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.DepositDao
import com.selfgrowthfund.sgf.data.local.dto.DepositEntrySummaryDTO
import com.selfgrowthfund.sgf.data.local.entities.Deposit
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import com.selfgrowthfund.sgf.model.enums.*
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepositRepository @Inject constructor(
    private val depositDao: DepositDao,
    private val realtimeSyncRepository: RealtimeSyncRepository
) {

    suspend fun getAllDeposits(): List<Deposit> = depositDao.getAllDepositsList()

    suspend fun submitByShareholder(
        shareholderId: String,
        shareholderName: String,
        dueMonth: DueMonth,
        paymentDate: LocalDate,
        shareNos: Int,
        additionalContribution: Double = 0.0,
        penalty: Double = 0.0,
        paymentMode: PaymentMode
    ) {
        val shareAmount = 2000.0
        val totalAmount =
            Deposit.calculateTotalAmount(shareNos, shareAmount, additionalContribution, penalty)

        val entry = Deposit(
            shareholderId = shareholderId,
            shareholderName = shareholderName,
            dueMonth = dueMonth,
            paymentDate = paymentDate,
            shareNos = shareNos,
            shareAmount = shareAmount,
            additionalContribution = additionalContribution,
            penalty = penalty,
            totalAmount = totalAmount,
            paymentStatus = PaymentStatus.PENDING,
            modeOfPayment = paymentMode,
            approvalStatus = ApprovalStage.PENDING,
            entrySource = EntrySource.User,
            isSynced = false // mark as pending sync
        )

        depositDao.insert(entry)
        realtimeSyncRepository.pushAllUnsynced()
    }

    suspend fun submitByTreasurer(
        shareholderId: String,
        shareholderName: String,
        dueMonth: DueMonth,
        paymentDate: LocalDate,
        shareNos: Int,
        additionalContribution: Double = 0.0,
        penalty: Double = 0.0,
        paymentMode: PaymentMode
    ) {
        val shareAmount = 2000.0
        val totalAmount =
            Deposit.calculateTotalAmount(shareNos, shareAmount, additionalContribution, penalty)

        val entry = Deposit(
            shareholderId = shareholderId,
            shareholderName = shareholderName,
            dueMonth = dueMonth,
            paymentDate = paymentDate,
            shareNos = shareNos,
            shareAmount = shareAmount,
            additionalContribution = additionalContribution,
            penalty = penalty,
            totalAmount = totalAmount,
            paymentStatus = PaymentStatus.PENDING,
            modeOfPayment = paymentMode,
            approvalStatus = ApprovalStage.TREASURER_APPROVED,
            entrySource = EntrySource.MemberAdmin,
            isSynced = false
        )

        depositDao.insert(entry)
        realtimeSyncRepository.pushAllUnsynced()
    }

    suspend fun approveByAdmin(
        provisionalId: String,
        adminId: String,
        lastDepositId: String?,
        notes: String? = null
    ) {
        val newDepositId = Deposit.generateNextId(lastDepositId)
        depositDao.approveByAdmin(
            provisionalId = provisionalId,
            depositId = newDepositId,
            status = ApprovalStage.ADMIN_APPROVED,
            approvedBy = adminId,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
        realtimeSyncRepository.pushAllUnsynced()
    }

    suspend fun reject(provisionalId: String, rejectedBy: String, notes: String? = null) {
        depositDao.updateStatus(
            provisionalId = provisionalId,
            status = ApprovalStage.REJECTED,
            approvedBy = rejectedBy,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
        realtimeSyncRepository.pushAllUnsynced()
    }

    // --- Queries ---
    suspend fun findById(provisionalId: String): Deposit? =
        depositDao.getByProvisionalId(provisionalId)

    suspend fun getByFinalId(depositId: String): Deposit? = depositDao.getByDepositId(depositId)

    fun getDepositEntrySummaries(): Flow<List<DepositEntrySummaryDTO>> =
        depositDao.getDepositEntrySummaries()

    // ---- Backward Compatibility Layer ----
    @Deprecated("Use depositDao.getDepositEntrySummaries() instead")
    fun getLiveDepositSummaries() = depositDao.getDepositEntrySummaries()

    @Deprecated("Realtime sync is automatic now")
    suspend fun refreshFromFirestore() {
        realtimeSyncRepository.pushAllUnsynced()
    }

    // --- Compatibility Helpers for ApprovalRepository & Dashboard ---

    suspend fun countApproved(start: LocalDate, end: LocalDate): Int {
        return depositDao.countByStatus(ApprovalStage.ADMIN_APPROVED, start, end)
    }

    suspend fun countRejected(start: LocalDate, end: LocalDate): Int {
        return depositDao.countByStatus(ApprovalStage.REJECTED, start, end)
    }

    suspend fun countPending(start: LocalDate, end: LocalDate): Int {
        return depositDao.countByStatus(ApprovalStage.PENDING, start, end)
    }

    /**
     * Called when Treasurer approves a deposit.
     * Keeps backward compatibility with ApprovalRepository.
     */
    suspend fun approveByTreasurer(provisionalId: String, approverId: String, notes: String?): Boolean {
        return try {
            depositDao.updateStatus(
                provisionalId = provisionalId,
                status = ApprovalStage.TREASURER_APPROVED,
                approvedBy = approverId,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
            realtimeSyncRepository.pushAllUnsynced()
            true
        } catch (e: Exception) {
            Timber.e(e, "approveByTreasurer failed for $provisionalId")
            false
        }
    }


    // --- Compatibility helper for TreasurerDashboardViewModel ---
    @Deprecated("Use getByApprovalStage(ApprovalStage.TREASURER_PENDING) instead")
    suspend fun getPendingForTreasurer(): List<Deposit> {
        return depositDao.getByApprovalStage(ApprovalStage.PENDING)
    }


}
