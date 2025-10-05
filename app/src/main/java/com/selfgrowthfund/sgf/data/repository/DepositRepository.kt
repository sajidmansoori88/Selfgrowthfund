package com.selfgrowthfund.sgf.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.selfgrowthfund.sgf.data.local.dao.DepositDao
import com.selfgrowthfund.sgf.data.local.dto.DepositEntrySummaryDTO
import com.selfgrowthfund.sgf.data.local.entities.Deposit
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import com.selfgrowthfund.sgf.model.enums.*
import com.selfgrowthfund.sgf.utils.Dates
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DepositRepository @Inject constructor(
    private val depositDao: DepositDao,
    private val firestore: FirebaseFirestore, // ✅ injected instance
    dates: Dates
) {
    suspend fun getAllDeposits(): List<Deposit> = depositDao.getAllDeposits()

    // --- Create Deposits ---
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
            entrySource = EntrySource.User
        )
        depositDao.insert(entry)
        syncDepositToFirestore(entry)
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
            entrySource = EntrySource.MemberAdmin
        )
        depositDao.insert(entry)
        syncDepositToFirestore(entry)
    }

    // --- Firestore sync ---
    private fun syncDepositToFirestore(deposit: Deposit) {
        val isoDate = DateTimeFormatter.ISO_DATE

        val firestoreData = mapOf(
            "depositId" to deposit.depositId,
            "provisionalId" to deposit.provisionalId,
            "shareholderId" to deposit.shareholderId,
            "shareholderName" to deposit.shareholderName,
            "dueMonth" to deposit.dueMonth.value,
            "paymentDate" to deposit.paymentDate.format(isoDate),
            "shareNos" to deposit.shareNos,
            "shareAmount" to deposit.shareAmount,
            "additionalContribution" to deposit.additionalContribution,
            "penalty" to deposit.penalty,
            "totalAmount" to deposit.totalAmount,
            "paymentStatus" to deposit.paymentStatus.name,
            "modeOfPayment" to deposit.modeOfPayment?.name,
            "approvalStatus" to deposit.approvalStatus.name,
            "approvedBy" to deposit.approvedBy,
            "notes" to deposit.notes,
            "isSynced" to true,
            "createdAt" to deposit.createdAt.toString(),
            "entrySource" to deposit.entrySource.name
        )

        firestore.collection("deposits").document(deposit.provisionalId)
            .set(firestoreData)
            .addOnSuccessListener {
                Log.d("Firestore", "Deposit synced: ${deposit.provisionalId}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Deposit sync failed", e)
            }
    }

    // --- Approval Flow ---
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
    }

    suspend fun reject(provisionalId: String, rejectedBy: String, notes: String? = null) {
        depositDao.updateStatus(
            provisionalId = provisionalId,
            status = ApprovalStage.REJECTED,
            approvedBy = rejectedBy,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun approveByTreasurer(provisionalId: String, treasurerId: String, note: String): Boolean {
        return try {
            val deposit = depositDao.getByProvisionalId(provisionalId) ?: return false
            val updated = deposit.copy(
                approvalStatus = ApprovalStage.TREASURER_APPROVED,
                approvedBy = treasurerId,
                notes = note
            )
            depositDao.update(updated)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun refreshFromFirestore() {
        val snapshot = firestore.collection("deposits").get().await()
        val deposits = snapshot.documents.mapNotNull { doc ->
            try {
                Deposit(
                    provisionalId = doc.getString("provisionalId") ?: return@mapNotNull null,
                    depositId = doc.getString("depositId"),
                    shareholderId = doc.getString("shareholderId") ?: "",
                    shareholderName = doc.getString("shareholderName") ?: "",
                    dueMonth = DueMonth(doc.getString("dueMonth") ?: ""),
                    paymentDate = LocalDate.parse(doc.getString("paymentDate") ?: LocalDate.now().toString()),
                    shareNos = doc.getLong("shareNos")?.toInt() ?: 0,
                    shareAmount = doc.getDouble("shareAmount") ?: 0.0,
                    additionalContribution = doc.getDouble("additionalContribution") ?: 0.0,
                    penalty = doc.getDouble("penalty") ?: 0.0,
                    totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                    paymentStatus = doc.getString("paymentStatus")?.let {
                        try { PaymentStatus.valueOf(it) } catch (_: Exception) { PaymentStatus.PENDING }
                    } ?: PaymentStatus.PENDING,
                    modeOfPayment = doc.getString("modeOfPayment")?.let {
                        try { PaymentMode.valueOf(it) } catch (_: Exception) { PaymentMode.OTHER }
                    } ?: PaymentMode.OTHER,
                    approvalStatus = doc.getString("approvalStatus")?.let {
                        try { ApprovalStage.valueOf(it) } catch (_: Exception) { ApprovalStage.PENDING }
                    } ?: ApprovalStage.PENDING,
                    approvedBy = doc.getString("approvedBy"),
                    notes = doc.getString("notes"),
                    createdAt = doc.getString("createdAt")?.let {
                        try { Instant.parse(it) } catch (_: Exception) { Instant.now() }
                    } ?: Instant.now(),
                    entrySource = doc.getString("entrySource")?.let {
                        try { EntrySource.valueOf(it) } catch (_: Exception) { EntrySource.User }
                    } ?: EntrySource.User
                )
            } catch (e: Exception) {
                null
            }
        }

        depositDao.insertAll(deposits)
    }

    // --- Queries ---
    suspend fun findById(provisionalId: String): Deposit? =
        depositDao.getByProvisionalId(provisionalId)

    suspend fun getByFinalId(depositId: String): Deposit? =
        depositDao.getByDepositId(depositId)

    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<Deposit> =
        depositDao.getApprovalsBetween(start, end)

    suspend fun countApproved(start: LocalDate, end: LocalDate) =
        depositDao.countByStatus(ApprovalStage.ADMIN_APPROVED, start, end)

    suspend fun countRejected(start: LocalDate, end: LocalDate) =
        depositDao.countByStatus(ApprovalStage.REJECTED, start, end)

    suspend fun countPending(start: LocalDate, end: LocalDate) =
        depositDao.countByStatus(ApprovalStage.PENDING, start, end)

    suspend fun countTotal(start: LocalDate, end: LocalDate) =
        depositDao.countTotal(start, end)

    suspend fun getPendingForTreasurer(): List<Deposit> {
        return depositDao.getByApprovalStage(ApprovalStage.PENDING)
    }

    suspend fun getApprovedPendingRelease(): List<Deposit> {
        return depositDao.getByApprovalStage(ApprovalStage.TREASURER_APPROVED)
    }

    // --- Summaries ---
    fun getDepositEntrySummaries(): Flow<List<DepositEntrySummaryDTO>> =
        depositDao.getDepositEntrySummaries()

    fun getLiveDepositSummaries(): Flow<List<DepositEntrySummaryDTO>> = callbackFlow {
        val listener = firestore.collection("deposits")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val summaries = snapshot.documents.mapNotNull { doc ->
                    try {
                        DepositEntrySummaryDTO(
                            provisionalId = doc.getString("provisionalId") ?: "",
                            depositId = doc.getString("depositId"),
                            shareholderId = doc.getString("shareholderId") ?: "Unknown",
                            shareholderName = doc.getString("shareholderName") ?: "Unknown",
                            shareNos = doc.getLong("shareNos")?.toInt() ?: 0,
                            shareAmount = doc.getDouble("shareAmount") ?: 0.0,
                            additionalContribution = doc.getDouble("additionalContribution") ?: 0.0,
                            penalty = doc.getDouble("penalty") ?: 0.0,
                            totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                            paymentStatus = doc.getString("paymentStatus") ?: "PENDING",
                            modeOfPayment = doc.getString("modeOfPayment") ?: "Unknown",
                            dueMonth = doc.getString("dueMonth") ?: "Unknown",
                            paymentDate = doc.getString("paymentDate")?.let {
                                try { LocalDate.parse(it) } catch (_: Exception) { null }
                            } ?: LocalDate.now(),
                            createdAt = doc.getString("createdAt")?.let {
                                try { Instant.parse(it) } catch (_: Exception) { null }
                            } ?: Instant.now()
                        )
                    } catch (_: Exception) {
                        null
                    }
                }

                trySend(summaries)
            }

        awaitClose { listener.remove() }
    }
}
