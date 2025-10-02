package com.selfgrowthfund.sgf.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.selfgrowthfund.sgf.data.local.dao.RepaymentDao
import com.selfgrowthfund.sgf.data.local.dao.RepaymentDao.BorrowingRepaymentSummary
import com.selfgrowthfund.sgf.data.local.dto.RepaymentSummaryDTO
import com.selfgrowthfund.sgf.data.local.entities.Borrowing
import com.selfgrowthfund.sgf.data.local.entities.Repayment
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.model.enums.BorrowingStatus
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.utils.IdGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepaymentRepository @Inject constructor(
    private val dao: RepaymentDao,
    private val borrowingRepository: BorrowingRepository,
    private val firestore: FirebaseFirestore
) {

    // --- Basic CRUD ---
    suspend fun insert(repayment: Repayment) = dao.insert(repayment)
    suspend fun update(repayment: Repayment) = dao.update(repayment)
    suspend fun delete(repayment: Repayment) = dao.delete(repayment)
    suspend fun deleteByProvisionalId(provisionalId: String) = dao.deleteByProvisionalId(provisionalId)

    suspend fun getByProvisionalId(provisionalId: String): Repayment? = dao.getByProvisionalId(provisionalId)
    suspend fun findById(provisionalId: String): Repayment? = dao.findById(provisionalId)

    // --- Borrowing-related lookups ---
    suspend fun getAllByBorrowIdList(borrowId: String): List<Repayment> = dao.getByBorrowIdList(borrowId)
    fun getAllByBorrowId(borrowId: String): Flow<List<Repayment>> = dao.getByBorrowId(borrowId)
    suspend fun getLastRepayment(borrowId: String): Repayment? = dao.getLastRepayment(borrowId)

    suspend fun getTotalPrincipalRepaid(borrowId: String): Double = dao.getTotalPrincipalRepaid(borrowId)
    suspend fun getTotalPenaltyPaid(borrowId: String): Double = dao.getTotalPenaltyPaid(borrowId)
    suspend fun getBorrowingRepaymentSummary(borrowId: String): BorrowingRepaymentSummary =
        dao.getBorrowingRepaymentSummary(borrowId)

    suspend fun getBorrowingById(borrowId: String): Borrowing =
        borrowingRepository.getBorrowingById(borrowId)

    suspend fun getRepaymentsByBorrowId(borrowId: String): List<Repayment> =
        dao.getByBorrowIdList(borrowId)

    // --- Reports & queries ---
    suspend fun getLateRepayments(): List<Repayment> = dao.getLateRepayments()
    suspend fun searchRepayments(query: String): List<Repayment> = dao.searchRepayments(query)
    suspend fun getLastRepaymentId(): String? = dao.getLastRepaymentId()

    // --- Approval workflow ---
    suspend fun approve(
        provisionalId: String,
        approverId: String?,
        notes: String?,
        newStatus: ApprovalStage
    ): Boolean = withContext(Dispatchers.IO) {
        val rows = dao.updateApprovalStatus(
            provisionalId = provisionalId,
            status = newStatus,
            approvedBy = approverId,
            notes = notes,
            updatedAt = System.currentTimeMillis()
        )
        rows > 0
    }

    suspend fun reject(
        provisionalId: String,
        rejectedBy: String?,
        notes: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val rows = dao.updateApprovalStatus(
            provisionalId = provisionalId,
            status = ApprovalStage.REJECTED,
            approvedBy = rejectedBy,
            notes = notes,
            updatedAt = System.currentTimeMillis()
        )
        rows > 0
    }

    suspend fun approveAndAssignId(
        provisionalId: String,
        approverId: String?,
        notes: String?
    ): Boolean = withContext(Dispatchers.IO) {
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
        true
    }

    // --- Counting / history ---
    suspend fun countByStatus(status: ApprovalStage, start: LocalDate, end: LocalDate): Int =
        dao.countByStatus(status, start, end)

    suspend fun countApproved(start: LocalDate, end: LocalDate): Int =
        dao.countByStatus(ApprovalStage.APPROVED, start, end)

    suspend fun countRejected(start: LocalDate, end: LocalDate): Int =
        dao.countByStatus(ApprovalStage.REJECTED, start, end)

    suspend fun countPending(start: LocalDate, end: LocalDate): Int =
        dao.countByStatus(ApprovalStage.PENDING, start, end)

    suspend fun countTotal(start: LocalDate, end: LocalDate): Int =
        dao.countTotal(start, end)

    suspend fun getApprovalsBetween(start: LocalDate, end: LocalDate): List<Repayment> =
        dao.getApprovalsBetween(start, end)

    fun getRepaymentSummaries(): Flow<List<RepaymentSummaryDTO>> =
        dao.getRepaymentSummaries()

    suspend fun insertAll(repayments: List<Repayment>) = dao.insertAll(repayments)

    // --- Firestore → Room Refresh ---
    suspend fun refreshFromFirestore() {
        try {
            val snapshot = firestore.collection("repayments").get().await()
            val repayments = snapshot.documents.mapNotNull { doc ->
                try {
                    Repayment(
                        provisionalId = doc.getString("provisionalId") ?: return@mapNotNull null,
                        repaymentId = doc.getString("repaymentId"),
                        borrowId = doc.getString("borrowId") ?: "",
                        shareholderName = doc.getString("shareholderName") ?: "",
                        repaymentDate = LocalDate.parse(
                            doc.getString("repaymentDate") ?: LocalDate.now().toString()
                        ),
                        principalRepaid = doc.getDouble("principalRepaid") ?: 0.0,
                        penaltyPaid = doc.getDouble("penaltyPaid") ?: 0.0,
                        totalAmountPaid = doc.getDouble("totalAmountPaid") ?: 0.0,
                        modeOfPayment = doc.getString("modeOfPayment")?.let {
                            runCatching { PaymentMode.valueOf(it) }.getOrDefault(PaymentMode.OTHER)
                        } ?: PaymentMode.OTHER,
                        finalOutstanding = doc.getDouble("finalOutstanding") ?: 0.0,
                        borrowingStatus = doc.getString("borrowingStatus")?.let {
                            runCatching { BorrowingStatus.valueOf(it) }.getOrDefault(BorrowingStatus.ACTIVE)
                        } ?: BorrowingStatus.ACTIVE,
                        outstandingBefore = doc.getDouble("outstandingBefore") ?: 0.0,
                        penaltyDue = doc.getDouble("penaltyDue") ?: 0.0,
                        notes = doc.getString("notes"),
                        penaltyCalculationNotes = doc.getString("penaltyCalculationNotes"),
                        approvalStatus = doc.getString("approvalStatus")?.let {
                            runCatching { ApprovalStage.valueOf(it) }.getOrDefault(ApprovalStage.PENDING)
                        } ?: ApprovalStage.PENDING,
                        createdBy = doc.getString("createdBy") ?: "system"
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse repayment from Firestore")
                    null
                }
            }
            dao.insertAll(repayments)
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing repayments from Firestore")
        }
    }

    // --- Firestore Live Sync ---
    fun getLiveRepayments(): Flow<List<Repayment>> = callbackFlow {
        val listener = firestore.collection("repayments")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val repayments = snapshot.documents.mapNotNull { doc ->
                    try {
                        Repayment(
                            provisionalId = doc.getString("provisionalId") ?: return@mapNotNull null,
                            repaymentId = doc.getString("repaymentId"),
                            borrowId = doc.getString("borrowId") ?: "",
                            shareholderName = doc.getString("shareholderName") ?: "",
                            repaymentDate = LocalDate.parse(
                                doc.getString("repaymentDate") ?: LocalDate.now().toString()
                            ),
                            principalRepaid = doc.getDouble("principalRepaid") ?: 0.0,
                            penaltyPaid = doc.getDouble("penaltyPaid") ?: 0.0,
                            totalAmountPaid = doc.getDouble("totalAmountPaid") ?: 0.0,
                            modeOfPayment = doc.getString("modeOfPayment")?.let {
                                runCatching { PaymentMode.valueOf(it) }.getOrDefault(PaymentMode.OTHER)
                            } ?: PaymentMode.OTHER,
                            finalOutstanding = doc.getDouble("finalOutstanding") ?: 0.0,
                            borrowingStatus = doc.getString("borrowingStatus")?.let {
                                runCatching { BorrowingStatus.valueOf(it) }.getOrDefault(BorrowingStatus.ACTIVE)
                            } ?: BorrowingStatus.ACTIVE,
                            outstandingBefore = doc.getDouble("outstandingBefore") ?: 0.0,
                            penaltyDue = doc.getDouble("penaltyDue") ?: 0.0,
                            notes = doc.getString("notes"),
                            penaltyCalculationNotes = doc.getString("penaltyCalculationNotes"),
                            approvalStatus = doc.getString("approvalStatus")?.let {
                                runCatching { ApprovalStage.valueOf(it) }.getOrDefault(ApprovalStage.PENDING)
                            } ?: ApprovalStage.PENDING,
                            createdBy = doc.getString("createdBy") ?: "system"
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to parse repayment from Firestore snapshot")
                        null
                    }
                }
                trySend(repayments)
            }
        awaitClose { listener.remove() }
    }

    fun getLiveRepaymentSummaries(): Flow<List<RepaymentSummaryDTO>> = callbackFlow {
        val listener = firestore.collection("repayments")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val summaries = snapshot.documents.mapNotNull { doc ->
                    try {
                        RepaymentSummaryDTO(
                            provisionalId = doc.getString("provisionalId") ?: "",
                            repaymentId = doc.getString("repaymentId"),
                            borrowId = doc.getString("borrowId") ?: "",
                            shareholderName = doc.getString("shareholderName") ?: "Unknown",
                            repaymentDate = LocalDate.parse(doc.getString("repaymentDate") ?: LocalDate.now().toString()),
                            principalRepaid = doc.getDouble("principalRepaid") ?: 0.0,
                            penaltyPaid = doc.getDouble("penaltyPaid") ?: 0.0,
                            totalAmountPaid = doc.getDouble("totalAmountPaid") ?: 0.0,
                            modeOfPayment = doc.getString("modeOfPayment") ?: "OTHER",
                            finalOutstanding = doc.getDouble("finalOutstanding") ?: 0.0,
                            approvalStatus = doc.getString("approvalStatus") ?: "PENDING"
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to parse repayment summary from Firestore")
                        null
                    }
                }
                trySend(summaries)
            }
        awaitClose { listener.remove() }
    }
}
