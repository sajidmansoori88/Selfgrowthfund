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

    // ---------------- ID Preview ----------------
    fun fetchNextRepaymentId() {
        viewModelScope.launch {
            val lastId = repository.getLastRepaymentId()
            nextRepaymentId.value = IdGenerator.nextRepaymentId(lastId)
        }
    }

    // ---------------- Repayment Submission ----------------
    fun submitRepayment(
        entry: RepaymentEntry,
        outstandingBefore: Double,
        borrowStartDate: LocalDate,
        dueDate: LocalDate,
        previousRepayments: List<Repayment>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isSubmitting.value = true
            submissionResult.value = null

            try {
                val lastId = repository.getLastRepaymentId()
                val repayment = entry.toRepayment(
                    lastRepaymentId = lastId,
                    outstandingBefore = outstandingBefore,
                    borrowStartDate = borrowStartDate,
                    dueDate = dueDate,
                    previousRepayments = previousRepayments
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
            val map = borrowings.associate { borrowing ->
                val principal = repository.getTotalPrincipalRepaid(borrowing.borrowId)
                val penalty = repository.getTotalPenaltyPaid(borrowing.borrowId)
                borrowing.borrowId to RepaymentSummary(principal, penalty)
            }
            _repaymentSummaries.value = map
        }
    }
}