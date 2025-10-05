package com.selfgrowthfund.sgf.ui.borrowing
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.selfgrowthfund.sgf.data.local.dao.BorrowingDao
import com.selfgrowthfund.sgf.data.local.entities.Borrowing
import com.selfgrowthfund.sgf.data.local.entities.BorrowingEntry
import com.selfgrowthfund.sgf.utils.Result
import com.selfgrowthfund.sgf.model.enums.BorrowingStatus
import com.selfgrowthfund.sgf.utils.IdGenerator
import com.selfgrowthfund.sgf.utils.mappers.toFirestoreMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class BorrowingViewModel @Inject constructor(
    private val borrowingDao: BorrowingDao,
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

    // ---------------- ID PREVIEW ----------------
    private val _nextBorrowId = MutableStateFlow<String?>(null)
    val nextBorrowId: StateFlow<String?> = _nextBorrowId.asStateFlow()

    init {
        loadAllBorrowings()
        loadOverdueBorrowings()
    }

    private fun loadAllBorrowings() {
        viewModelScope.launch {
            borrowingDao.getAllBorrowings().collect { borrowings ->
                _allBorrowings.value = borrowings
            }
        }
    }

    private fun loadOverdueBorrowings() {
        viewModelScope.launch {
            borrowingDao.getOverdueBorrowings(LocalDate.now()).collect { borrowings ->
                _overdueBorrowings.value = borrowings
            }
        }
    }

    fun fetchNextBorrowId() {
        viewModelScope.launch {
            val lastId = borrowingDao.getLastBorrowingId()
            _nextBorrowId.value = IdGenerator.nextBorrowId(lastId)
        }
    }

    // ---------------- SUBMIT ----------------
    fun submitBorrowing(
        entry: BorrowingEntry,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (_isSubmitting.value) return@launch

            _isSubmitting.value = true
            _submissionResult.value = null

            try {
                val lastId = borrowingDao.getLastBorrowingId()
                val borrowId = IdGenerator.nextBorrowId(lastId)

                val borrowing = entry.toBorrowing(borrowId)

                borrowingDao.insertBorrowing(borrowing)
                syncToFirestore(borrowing)
                triggerApprovalNotification(borrowing)
                listenForApprovalUpdates(borrowId)

                _submissionResult.value = Result.Success(Unit)
                onSuccess()
            } catch (e: Exception) {
                _submissionResult.value = Result.Error(e)
                onError(e.message ?: "Borrowing submission failed")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    // ---------------- FIRESTORE SYNC ----------------
    private fun syncToFirestore(borrowing: Borrowing) {
        val docId = borrowing.borrowId ?: borrowing.provisionalId // ✅ safe fallback
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
        val docId = borrowing.borrowId ?: borrowing.provisionalId // ✅ use provisional ID

        val notification = mapOf(
            "type" to "BORROWING_REQUEST",
            "borrowId" to docId,
            "shareholderId" to borrowing.shareholderId,
            "shareholderName" to borrowing.shareholderName,
            "amountRequested" to borrowing.amountRequested,
            "createdAt" to borrowing.createdAt.toString()
        )

        firestore.collection("approvals")
            .document(docId)
            .set(notification)
    }

    // ---------------- FIRESTORE LISTENER ----------------
    fun listenForApprovalUpdates(borrowId: String) {
        firestore.collection("approvals")
            .document(borrowId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val status = snapshot.getString("status") ?: return@addSnapshotListener
                val closedDate = snapshot.getString("closedDate")?.let { LocalDate.parse(it) }

                val newStatus = when (status.lowercase()) {
                    "approved" -> BorrowingStatus.APPROVED
                    "rejected" -> BorrowingStatus.REJECTED
                    "closed" -> BorrowingStatus.CLOSED
                    else -> return@addSnapshotListener
                }

                viewModelScope.launch {
                    // FIXED: Convert enum to string for DAO method
                    borrowingDao.updateBorrowingStatus(borrowId, newStatus.name, closedDate)
                    Log.d("Firestore", "Borrowing status updated: $newStatus")

                    // Refresh the lists after status update
                    loadAllBorrowings()
                    loadOverdueBorrowings()
                }
            }
    }

    // ---------------- REFRESH METHODS ----------------
    fun refreshAllBorrowings() {
        loadAllBorrowings()
    }

    fun refreshOverdueBorrowings() {
        loadOverdueBorrowings()
    }

    // ---------------- CLEAR STATE ----------------
    fun clearSubmissionState() {
        _submissionResult.value = null
    }
}