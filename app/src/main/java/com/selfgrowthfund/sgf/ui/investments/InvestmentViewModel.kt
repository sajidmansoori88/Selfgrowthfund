package com.selfgrowthfund.sgf.ui.investments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.data.repository.InvestmentRepository
import com.selfgrowthfund.sgf.utils.IdGenerator
import com.selfgrowthfund.sgf.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvestmentViewModel @Inject constructor(
    private val repository: InvestmentRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // ---------------- STATE ----------------
    val isSubmitting = MutableStateFlow(false)
    val submissionResult = MutableStateFlow<Result<Unit>?>(null)
    val nextInvestmentId = MutableStateFlow<String?>(null)

    // ---------------- ID PREVIEW ----------------
    fun fetchNextInvestmentId() {
        viewModelScope.launch {
            val lastId = repository.getLastInvestmentId()
            nextInvestmentId.value = IdGenerator.nextInvestmentId(lastId)
        }
    }

    // ---------------- INVESTMENTS ----------------
    val investments: StateFlow<List<Investment>> =
        repository.getAllInvestments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getInvestmentById(id: String): Flow<Investment?> {
        return repository.getInvestmentById(id)
    }

    // ---------------- SUBMIT ----------------
    fun submitInvestment(
        investment: Investment,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isSubmitting.value = true
            submissionResult.value = null

            try {
                val result = repository.createInvestment(investment)
                if (result is Result.Success) {
                    syncToFirestore(investment)
                    submissionResult.value = Result.Success(Unit)
                    onSuccess()
                } else if (result is Result.Error) {
                    throw result.exception
                }
            } catch (e: Exception) {
                submissionResult.value = Result.Error(e)
                onError(e.message ?: "Investment submission failed")
            }

            isSubmitting.value = false
        }
    }

    // ---------------- FIRESTORE SYNC ----------------
    private fun syncToFirestore(investment: Investment) {
        val data = mapOf(
            "investmentId" to investment.investmentId,
            "investeeType" to investment.investeeType.name,
            "investeeName" to investment.investeeName,
            "ownershipType" to investment.ownershipType.name,
            "partnerNames" to investment.partnerNames,
            "investmentDate" to investment.investmentDate.toString(),
            "investmentType" to investment.investmentType.name,
            "investmentName" to investment.investmentName,
            "amount" to investment.amount,
            "expectedProfitPercent" to investment.expectedProfitPercent,
            "expectedProfitAmount" to investment.expectedProfitAmount,
            "expectedReturnPeriod" to investment.expectedReturnPeriod,
            "returnDueDate" to investment.returnDueDate.toString(),
            "modeOfPayment" to investment.modeOfPayment.name,
            "status" to investment.status.name,
            "remarks" to investment.remarks
        )

        firestore.collection("investments")
            .document(investment.investmentId)
            .set(data)
            .addOnSuccessListener {
                Log.d("Firestore", "Investment synced: ${investment.investmentId}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Investment sync failed", e)
            }
    }
}
