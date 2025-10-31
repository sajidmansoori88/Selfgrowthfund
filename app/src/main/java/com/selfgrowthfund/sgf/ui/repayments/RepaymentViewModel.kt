package com.selfgrowthfund.sgf.ui.repayments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.selfgrowthfund.sgf.data.local.entities.Borrowing
import com.selfgrowthfund.sgf.data.local.entities.Repayment
import com.selfgrowthfund.sgf.data.local.entities.RepaymentEntry
import com.selfgrowthfund.sgf.data.repository.RepaymentRepository
import com.selfgrowthfund.sgf.utils.IdGenerator
import com.selfgrowthfund.sgf.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class RepaymentViewModel @Inject constructor(
    private val repository: RepaymentRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // ---------------- Submission State ----------------
    val isSubmitting = MutableStateFlow(false)
    val submissionResult = MutableStateFlow<Result<Unit>?>(null)
    val nextRepaymentId = MutableStateFlow<String?>(null)

    // ---------------- Approval State ----------------
    val approvalResult = MutableStateFlow<Result<Unit>?>(null)

    // ---------------- Summary State ----------------
    private val _repaymentSummaries = MutableStateFlow<Map<String, RepaymentSummary>>(emptyMap())
    val repaymentSummaries: StateFlow<Map<String, RepaymentSummary>> = _repaymentSummaries

    // ---------------- Borrowing Details State ----------------
    private val _borrowingDetails = MutableStateFlow<BorrowingDetails?>(null)
    val borrowingDetails: StateFlow<BorrowingDetails?> = _borrowingDetails

    // ---------------- Live Repayments (Room + RealtimeSync) ----------------
    val liveRepayments: StateFlow<List<Repayment>> =
        repository.getAllRepayments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveRepaymentSummaries: StateFlow<List<com.selfgrowthfund.sgf.data.local.dto.RepaymentSummaryDTO>> =
        repository.getRepaymentSummaries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // ---------------- ID Preview ----------------
    fun fetchNextRepaymentId() {
        viewModelScope.launch {
            try {
                val lastId = repository.getLastRepaymentId()
                nextRepaymentId.value = IdGenerator.nextRepaymentId(lastId)
            } catch (e: Exception) {
                Timber.e(e, "Error fetching next repayment ID")
                nextRepaymentId.value = "ERROR"
            }
        }
    }

    // ---------------- Load Borrowing Details ----------------
    fun loadBorrowingDetails(borrowId: String) {
        viewModelScope.launch {
            try {
                val borrowing = repository.getBorrowingById(borrowId)
                val previousRepayments = repository.getRepaymentsByBorrowId(borrowId)
                val totalPrincipal = repository.getTotalPrincipalRepaid(borrowId)

                _borrowingDetails.value = BorrowingDetails(
                    borrowId = borrowId,
                    shareholderName = borrowing.shareholderName,
                    outstandingBefore = borrowing.amountRequested - totalPrincipal,
                    borrowStartDate = borrowing.borrowStartDate,
                    dueDate = borrowing.dueDate,
                    previousRepayments = previousRepayments
                )
            } catch (e: Exception) {
                Timber.e(e, "Error loading borrowing details")
                _borrowingDetails.value = null
            }
        }
    }

    // ---------------- Repayment Submission ----------------
    fun submitRepayment(
        entry: RepaymentEntry,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isSubmitting.value = true
            submissionResult.value = null

            try {
                val details = _borrowingDetails.value
                    ?: throw IllegalStateException("Borrowing details not loaded. Please try again.")

                val repayment = entry.toRepayment(
                    outstandingBefore = details.outstandingBefore,
                    borrowStartDate = details.borrowStartDate,
                    dueDate = details.dueDate,
                    previousRepayments = details.previousRepayments
                )

                repository.insert(repayment)
                syncToFirestore(repayment)

                submissionResult.value = Result.Success(Unit)
                onSuccess()
            } catch (e: Exception) {
                submissionResult.value = Result.Error(e)
                onError(e.message ?: "Repayment submission failed")
            }

            isSubmitting.value = false
        }
    }

    // ---------------- Firestore Sync ----------------
    private fun syncToFirestore(repayment: Repayment) {
        val data = mapOf(
            "provisionalId" to repayment.provisionalId,
            "repaymentId" to repayment.repaymentId,
            "borrowId" to repayment.borrowId,
            "shareholderName" to repayment.shareholderName,
            "repaymentDate" to repayment.repaymentDate.toString(),
            "principalRepaid" to repayment.principalRepaid,
            "penaltyPaid" to repayment.penaltyPaid,
            "totalAmountPaid" to repayment.totalAmountPaid,
            "modeOfPayment" to repayment.modeOfPayment.name,
            "finalOutstanding" to repayment.finalOutstanding,
            "borrowingStatus" to repayment.borrowingStatus.name,
            "outstandingBefore" to repayment.outstandingBefore,
            "penaltyDue" to repayment.penaltyDue,
            "notes" to repayment.notes,
            "penaltyCalculationNotes" to repayment.penaltyCalculationNotes,
            "approvalStatus" to repayment.approvalStatus.name,
            "createdBy" to repayment.createdBy
        )

        firestore.collection("repayments")
            .document(repayment.provisionalId)
            .set(data)
            .addOnSuccessListener {
                Timber.d("Repayment synced: ${repayment.provisionalId}")
            }
            .addOnFailureListener { e ->
                Timber.e(e, "Repayment sync failed")
            }
    }

    // ---------------- Approvals ----------------
    fun approveAsTreasurer(provisionalId: String, treasurerId: String, notes: String? = null) {
        viewModelScope.launch {
            when (val result = repository.approve(
                provisionalId,
                approverId = treasurerId,
                notes = notes,
                newStatus = com.selfgrowthfund.sgf.model.enums.ApprovalStage.TREASURER_APPROVED
            )) {
                is Result.Success -> approvalResult.value = Result.Success(Unit)
                is Result.Error -> approvalResult.value = Result.Error(result.exception)
                Result.Loading -> { /* optional: show loading spinner */ }
            }
        }
    }


    fun approveAsAdmin(provisionalId: String, adminId: String, notes: String? = null) {
        viewModelScope.launch {
            when (val result = repository.approveAndAssignId(
                provisionalId,
                approverId = adminId,
                notes = notes
            )) {
                is Result.Success -> approvalResult.value = Result.Success(Unit)
                is Result.Error -> approvalResult.value = Result.Error(result.exception)
                Result.Loading -> { /* optional: show loading spinner */ }
            }
        }
    }


    fun rejectRepayment(provisionalId: String, rejectedBy: String, notes: String? = null) {
        viewModelScope.launch {
            when (val result = repository.reject(provisionalId, rejectedBy, notes)) {
                is Result.Success -> approvalResult.value = Result.Success(Unit)
                is Result.Error -> approvalResult.value = Result.Error(result.exception)
                Result.Loading -> { /* optional: show loading spinner */ }
            }
        }
    }


    // ---------------- Summary Loader ----------------
    fun loadSummaries(borrowings: List<Borrowing>) {
        viewModelScope.launch {
            try {
                val map = borrowings.associate { borrowing ->
                    val key = borrowing.borrowId ?: borrowing.provisionalId // ✅ always non-null
                    val principal = repository.getTotalPrincipalRepaid(key)
                    val penalty = repository.getTotalPenaltyPaid(key)
                    key to RepaymentSummary(principal, penalty)
                }
                _repaymentSummaries.value = map
            } catch (e: Exception) {
                Timber.e(e, "Error loading repayment summaries")
                _repaymentSummaries.value = emptyMap()
            }
        }
    }

    // ---------------- Data Classes ----------------
    data class BorrowingDetails(
        val borrowId: String,
        val shareholderName: String,
        val outstandingBefore: Double,
        val borrowStartDate: LocalDate,
        val dueDate: LocalDate,
        val previousRepayments: List<Repayment>
    )
}
