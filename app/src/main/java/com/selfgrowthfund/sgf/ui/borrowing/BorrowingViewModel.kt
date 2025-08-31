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
    val allBorrowings: StateFlow<List<Borrowing>> =
        borrowingDao.getAllBorrowings()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overdueBorrowings: StateFlow<List<Borrowing>> =
        borrowingDao.getOverdueBorrowings(LocalDate.now())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------------- SUBMISSION STATE ----------------
    val isSubmitting = MutableStateFlow(false)
    val submissionResult = MutableStateFlow<Result<Unit>?>(null)

    // ---------------- ID PREVIEW ----------------
    val nextBorrowId = MutableStateFlow<String?>(null)

    fun fetchNextBorrowId() {
        viewModelScope.launch {
            val lastId = borrowingDao.getLastBorrowingId()
            nextBorrowId.value = IdGenerator.nextBorrowId(lastId)
        }
    }

    // ---------------- SUBMIT ----------------
    fun submitBorrowing(
        entry: BorrowingEntry,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isSubmitting.value = true
            submissionResult.value = null

            try {
                val lastId = borrowingDao.getLastBorrowingId()
                val borrowId = IdGenerator.nextBorrowId(lastId)

                val borrowing = entry.toBorrowing(borrowId)

                borrowingDao.insertBorrowing(borrowing)
                syncToFirestore(borrowing)
                triggerApprovalNotification(borrowing)
                listenForApprovalUpdates(borrowId)

                submissionResult.value = Result.Success(Unit)
                onSuccess()
            } catch (e: Exception) {
                submissionResult.value = Result.Error(e)
                onError(e.message ?: "Borrowing submission failed")
            }

            isSubmitting.value = false
        }
    }

    // ---------------- FIRESTORE SYNC ----------------
    private fun syncToFirestore(borrowing: Borrowing) {
        firestore.collection("borrowings")
            .document(borrowing.borrowId)
            .set(borrowing.toFirestoreMap())
            .addOnSuccessListener {
                Log.d("Firestore", "Borrowing synced: ${borrowing.borrowId}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Borrowing sync failed", e)
            }
    }

    private fun triggerApprovalNotification(borrowing: Borrowing) {
        val notification = mapOf(
            "type" to "BORROWING_REQUEST",
            "borrowId" to borrowing.borrowId,
            "shareholderId" to borrowing.shareholderId,
            "shareholderName" to borrowing.shareholderName,
            "amountRequested" to borrowing.amountRequested,
            "createdAt" to borrowing.createdAt.toString()
        )

        firestore.collection("approvals")
            .document(borrowing.borrowId)
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
                    borrowingDao.updateBorrowingStatus(borrowId, newStatus, closedDate)
                    Log.d("Firestore", "Borrowing status updated: $newStatus")
                }
            }
    }
}