package com.selfgrowthfund.sgf.ui.investmentreturns

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturnEntry
import com.selfgrowthfund.sgf.data.repository.InvestmentReturnsRepository
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class InvestmentReturnsViewModel @Inject constructor(
    private val repository: InvestmentReturnsRepository,
    private val dates: Dates,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // ---------------- STATE ----------------
    val isSubmitting = MutableStateFlow(false)
    val submissionResult = MutableStateFlow<Result<Unit>?>(null)

    // ---------------- SUBMIT ----------------
    fun submitReturn(
        entry: InvestmentReturnEntry,
        lastReturnId: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (isSubmitting.value) return@launch

            if (entry.amountReceived <= 0.0) {
                submissionResult.value = Result.Error(
                    IllegalArgumentException("Amount must be positive")
                )
                onError("Amount must be positive")
                return@launch
            }

            isSubmitting.value = true
            submissionResult.value = null

            try {
                val returnEntity = entry.toInvestmentReturn(lastReturnId)
                val result = repository.addReturn(returnEntity)

                submissionResult.value = result
                when (result) {
                    is Result.Success -> {
                        syncToFirestore(returnEntity)
                        onSuccess()
                    }
                    is Result.Error -> onError(result.exception.message ?: "Submission failed")
                    else -> {}
                }
            } catch (e: Exception) {
                submissionResult.value = Result.Error(e)
                onError(e.message ?: "Submission failed")
            }

            isSubmitting.value = false
        }
    }

    fun getReturnsByInvestmentId(investmentId: String): Flow<List<InvestmentReturns>> =
        repository.getReturnsByInvestmentId(investmentId)


    // ---------------- FIRESTORE SYNC ----------------
    private fun syncToFirestore(returnEntity: InvestmentReturns) {
        val docId = returnEntity.returnId ?: returnEntity.provisionalId // ✅ fallback to provisionalId

        val data = mapOf(
            "returnId" to (returnEntity.returnId ?: docId),
            "investmentId" to returnEntity.investmentId,
            "amountReceived" to returnEntity.amountReceived,
            "returnDate" to returnEntity.returnDate.toString(),
            "remarks" to returnEntity.remarks
        )

        firestore.collection("investment_returns")
            .document(docId)
            .set(data)
            .addOnSuccessListener {
                Log.d("Firestore", "Return synced: $docId")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Return sync failed", e)
            }
    }

    // ---------------- PREVIEW ----------------
    fun previewReturn(entry: InvestmentReturnEntry): InvestmentReturns {
        val localDate = Instant.ofEpochMilli(dates.now())
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        return entry.copy(returnDate = localDate).toInvestmentReturn(lastReturnId = null)
    }

    // ---------------- CLEAR STATE ----------------
    fun clearState() {
        submissionResult.value = null
        isSubmitting.value = false
    }
}
