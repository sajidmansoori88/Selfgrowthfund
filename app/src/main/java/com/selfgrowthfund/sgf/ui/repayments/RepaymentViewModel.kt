package com.selfgrowthfund.sgf.ui.repayments

import android.util.Log
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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

    // ---------------- Summary State ----------------
    private val _repaymentSummaries = MutableStateFlow<Map<String, RepaymentSummary>>(emptyMap())
    val repaymentSummaries: StateFlow<Map<String, RepaymentSummary>> = _repaymentSummaries

    // ---------------- Borrowing Details State ----------------
    private val _borrowingDetails = MutableStateFlow<BorrowingDetails?>(null)
    val borrowingDetails: StateFlow<BorrowingDetails?> = _borrowingDetails

    // ---------------- ID Preview ----------------
    fun fetchNextRepaymentId() {
        viewModelScope.launch {
            try {
                val lastId = repository.getLastRepaymentId()
                nextRepaymentId.value = IdGenerator.nextRepaymentId(lastId)
            } catch (e: Exception) {
                Log.e("RepaymentViewModel", "Error fetching next repayment ID", e)
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
                    borrowStartDate = borrowing.borrowStartDate, // FIXED: Make sure Borrowing entity has startDate field
                    dueDate = borrowing.dueDate,
                    previousRepayments = previousRepayments
                )
            } catch (e: Exception) {
                Log.e("RepaymentViewModel", "Error loading borrowing details", e)
                _borrowingDetails.value = null
            }
        }
    }

    // ---------------- Simplified Repayment Submission ----------------
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
                if (details == null) {
                    throw IllegalStateException("Borrowing details not loaded. Please try again.")
                }

                val lastId = repository.getLastRepaymentId()
                val repayment = entry.toRepayment(
                    lastRepaymentId = lastId,
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
            "repaymentId" to repayment.repaymentId,
            "borrowId" to repayment.borrowId,
            "shareholderName" to repayment.shareholderName,
            "repaymentDate" to repayment.repaymentDate.toString(),
            "principalRepaid" to repayment.principalRepaid,
            "penaltyPaid" to repayment.penaltyPaid,
            "totalAmountPaid" to repayment.totalAmountPaid,
            "modeOfPayment" to repayment.modeOfPayment,
            "finalOutstanding" to repayment.finalOutstanding,
            "borrowingStatus" to repayment.borrowingStatus,
            "notes" to repayment.notes,
            "penaltyCalculationNotes" to repayment.penaltyCalculationNotes
        )

        firestore.collection("repayments")
            .document(repayment.repaymentId)
            .set(data)
            .addOnSuccessListener {
                Log.d("Firestore", "Repayment synced: ${repayment.repaymentId}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Repayment sync failed", e)
            }
    }

    // ---------------- Summary Loader ----------------
    fun loadSummaries(borrowings: List<Borrowing>) {
        viewModelScope.launch {
            try {
                val map = borrowings.associate { borrowing ->
                    val principal = repository.getTotalPrincipalRepaid(borrowing.borrowId)
                    val penalty = repository.getTotalPenaltyPaid(borrowing.borrowId)
                    borrowing.borrowId to RepaymentSummary(principal, penalty)
                }
                _repaymentSummaries.value = map
            } catch (e: Exception) {
                Log.e("RepaymentViewModel", "Error loading repayment summaries", e)
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

    data class RepaymentSummary(
        val totalPrincipal: Double,
        val totalPenalty: Double
    )
}