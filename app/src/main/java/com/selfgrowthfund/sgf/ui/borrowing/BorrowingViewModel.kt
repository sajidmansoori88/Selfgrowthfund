package com.selfgrowthfund.sgf.ui.borrowing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.selfgrowthfund.sgf.data.local.dto.MemberBorrowingStatus
import com.selfgrowthfund.sgf.data.local.entities.Borrowing
import com.selfgrowthfund.sgf.data.local.entities.BorrowingEntry
import com.selfgrowthfund.sgf.data.repository.BorrowingRepository
import com.selfgrowthfund.sgf.model.enums.BorrowingStatus
import com.selfgrowthfund.sgf.utils.Result
import com.selfgrowthfund.sgf.utils.mappers.toFirestoreMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BorrowingViewModel @Inject constructor(
    private val repository: BorrowingRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // ---------------- HISTORY ----------------
    private val _allBorrowings = MutableStateFlow<List<Borrowing>>(emptyList())
    val allBorrowings: StateFlow<List<Borrowing>> = _allBorrowings.asStateFlow()

    private val _overdueBorrowings = MutableStateFlow<List<Borrowing>>(emptyList())
    val overdueBorrowings: StateFlow<List<Borrowing>> = _overdueBorrowings.asStateFlow()

    // ---------------- SUBMISSION STATE ----------------
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submissionResult = MutableStateFlow<Result<Unit>?>(null)
    val submissionResult: StateFlow<Result<Unit>?> = _submissionResult.asStateFlow()

    // ---------------- FORM DATA ----------------
    private val _eligibility = MutableStateFlow(0.0)
    val eligibility: StateFlow<Double> = _eligibility.asStateFlow()

    init {
        loadAllBorrowings()
        loadOverdueBorrowings()
    }

    // ============================================================
    // ===============   DATA LOADERS   ============================
    // ============================================================
    private fun loadAllBorrowings() {
        viewModelScope.launch {
            repository.getAllBorrowings().collect { borrowings ->
                _allBorrowings.value = borrowings
            }
        }
    }

    private fun loadOverdueBorrowings() {
        viewModelScope.launch {
            repository.getOverdueBorrowings().collect { borrowings ->
                _overdueBorrowings.value = borrowings
            }
        }
    }
    private val _memberStatuses = MutableStateFlow<List<MemberBorrowingStatus>>(emptyList())
    val memberStatuses: StateFlow<List<MemberBorrowingStatus>> = _memberStatuses

    fun loadMemberBorrowingStatuses() {
        viewModelScope.launch {
            _memberStatuses.value = repository.getMemberBorrowingStatus()
        }
    }


    // ============================================================
    // ===============   APPLY BORROWING (B2)   ===================
    // ============================================================
    fun applyBorrowing(
        entry: BorrowingEntry,
        onSuccess: (Borrowing) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_isSubmitting.value) return

        viewModelScope.launch {
            _isSubmitting.value = true
            _submissionResult.value = Result.Loading

            try {
                val result = repository.applyForBorrowing(
                    shareholderId = entry.shareholderId,
                    shareholderName = entry.shareholderName,
                    amountRequested = entry.amountRequested,
                    createdBy = entry.createdBy,
                    notes = entry.notes
                )

                when (result) {
                    is Result.Success -> {
                        val borrowing = result.data
                        syncToFirestore(borrowing)
                        triggerApprovalNotification(borrowing)
                        listenForApprovalUpdates(borrowing.provisionalId)

                        _submissionResult.value = Result.Success(Unit)
                        onSuccess(borrowing)
                        Log.d("BorrowingVM", "Borrowing submitted: ${borrowing.provisionalId}")
                    }

                    is Result.Error -> {
                        _submissionResult.value = Result.Error(result.exception)
                        val msg = result.exception.message ?: "Borrowing application failed"
                        Log.e("BorrowingVM", msg, result.exception)
                        onError(msg)
                    }

                    is Result.Loading -> {
                        Log.d("BorrowingVM", "Applying for borrowing...")
                    }
                }
            } catch (e: Exception) {
                Log.e("BorrowingVM", "Error applying for borrowing", e)
                _submissionResult.value = Result.Error(e)
                onError(e.message ?: "Unexpected error during application")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    // ============================================================
    // ===============   FIRESTORE SYNC   ==========================
    // ============================================================
    private fun syncToFirestore(borrowing: Borrowing) {
        val docId = borrowing.provisionalId
        firestore.collection("borrowings")
            .document(docId)
            .set(borrowing.toFirestoreMap())
            .addOnSuccessListener {
                Log.d("Firestore", "Borrowing synced: $docId")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Borrowing sync failed", e)
            }
    }

    private fun triggerApprovalNotification(borrowing: Borrowing) {
        val docId = borrowing.provisionalId
        val notification = mapOf(
            "type" to "BORROWING_REQUEST",
            "provisionalId" to docId,
            "shareholderId" to borrowing.shareholderId,
            "shareholderName" to borrowing.shareholderName,
            "amountRequested" to borrowing.amountRequested,
            "createdAt" to borrowing.createdAt.toString()
        )

        firestore.collection("approvals")
            .document(docId)
            .set(notification)
    }

    // ============================================================
    // ===============   APPROVAL LISTENER   =======================
    // ============================================================
    fun listenForApprovalUpdates(provisionalId: String) {
        firestore.collection("approvals")
            .document(provisionalId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val status = snapshot.getString("status") ?: return@addSnapshotListener

                val newStatus = when (status.lowercase()) {
                    "approved" -> BorrowingStatus.APPROVED
                    "rejected" -> BorrowingStatus.REJECTED
                    "closed" -> BorrowingStatus.CLOSED
                    else -> return@addSnapshotListener
                }

                viewModelScope.launch {
                    repository.updateBorrowingStatus(
                        borrowId = provisionalId,
                        status = newStatus
                    )
                    Log.d("Firestore", "Borrowing status updated: $newStatus")
                    loadAllBorrowings()
                    loadOverdueBorrowings()
                }
            }
    }

    // ============================================================
    // ===============   REFRESH / CLEAR   =========================
    // ============================================================
    fun refreshAllBorrowings() = loadAllBorrowings()
    fun refreshOverdueBorrowings() = loadOverdueBorrowings()
    fun clearSubmissionState() { _submissionResult.value = null }
}
