package com.selfgrowthfund.sgf.ui.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.data.repository.InvestmentRepository
import com.selfgrowthfund.sgf.data.repository.ShareholderRepository
import com.selfgrowthfund.sgf.model.enums.*
import com.selfgrowthfund.sgf.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import com.selfgrowthfund.sgf.data.repository.ApprovalFlowRepository
import com.selfgrowthfund.sgf.data.local.entities.ApprovalFlow
import com.selfgrowthfund.sgf.model.enums.ApprovalAction
import com.selfgrowthfund.sgf.model.enums.ApprovalType
import com.selfgrowthfund.sgf.model.User


// ---------------- UI STATE ----------------
data class AddInvestmentUiState(
    val provisionalId: String = UUID.randomUUID().toString(),
    val createdAt: LocalDate = LocalDate.now(),             // ✅ NEW: Application Date
    val shareholderId: String? = null,
    val investeeName: String = "",
    val shareholderList: List<Pair<String, String>> = emptyList(),
    val investeeType: InvesteeType = InvesteeType.Shareholder,
    val investmentName: String = "",
    val ownershipType: OwnershipType = OwnershipType.Individual,
    val partnerNames: String = "",
    val investmentDate: LocalDate = LocalDate.now(),
    val investmentType: InvestmentType = InvestmentType.Other,
    val amount: Double = 0.0,
    val expectedProfitPercent: Double = 0.0,
    val expectedProfitAmount: Double = 0.0,
    val expectedReturnPeriod: Int = 0,
    val remarks: String = "",
    val isSubmitting: Boolean = false
)

@HiltViewModel
class InvestmentViewModel @Inject constructor(
    private val repository: InvestmentRepository,
    private val shareholderRepository: ShareholderRepository,
    private val approvalFlowRepository: ApprovalFlowRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddInvestmentUiState())
    val uiState: StateFlow<AddInvestmentUiState> = _uiState.asStateFlow()
    val submissionResult = MutableStateFlow<Result<Unit>?>(null)

    val investments: StateFlow<List<Investment>> =
        repository.getAllInvestments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onInvesteeTypeSelected(type: InvesteeType) {
        viewModelScope.launch {
            if (type == InvesteeType.Shareholder) {
                shareholderRepository.getAllShareholdersStream().collect { members ->
                    val activeMembers = members.filter { it.isActive() }
                    _uiState.update {
                        it.copy(
                            investeeType = type,
                            shareholderList = activeMembers.map { m -> m.shareholderId to m.fullName },
                            investeeName = "",
                            shareholderId = null
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        investeeType = type,
                        shareholderList = emptyList(),
                        shareholderId = null,
                        investeeName = ""
                    )
                }
            }
        }
    }

    fun onShareholderSelected(id: String, name: String) {
        _uiState.update { it.copy(shareholderId = id, investeeName = name) }
    }

    fun updateField(field: (AddInvestmentUiState) -> AddInvestmentUiState) {
        _uiState.update { field(it) }
    }

    // ---------------- LOOKUP ----------------
    fun getInvestmentByProvisionalId(id: String): Flow<Investment?> =
        repository.getByProvisionalIdFlow(id)

    fun getInvestmentByInvestmentId(id: String): Flow<Investment?> =
        repository.getByInvestmentIdFlow(id)

    // ---------------- SUBMIT ----------------
    fun submitInvestment(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            submissionResult.value = null

            try {
                val state = _uiState.value

                val investment = Investment(
                    investmentId = null,
                    provisionalId = state.provisionalId,
                    shareholderId = state.shareholderId ?: "",
                    investeeType = state.investeeType,
                    investeeName = state.investeeName,
                    ownershipType = state.ownershipType,
                    partnerNames = state.partnerNames,
                    investmentDate = state.investmentDate,
                    investmentType = state.investmentType,
                    investmentName = state.investmentName,
                    amount = state.amount,
                    expectedProfitPercent = state.expectedProfitPercent,
                    expectedProfitAmount = state.expectedProfitAmount,
                    expectedReturnPeriod = state.expectedReturnPeriod,
                    remarks = state.remarks,
                    approvalStatus = ApprovalStage.PENDING,
                    returnDueDate = state.investmentDate.plusDays(state.expectedReturnPeriod.toLong()),
                    createdAt = state.createdAt
                )

                val result = repository.createInvestment(investment)

                if (result is Result.Success) {
                    syncToFirestore(investment)
                    Timber.i("✅ Investment submitted. ProvisionalId = ${investment.provisionalId}")
                    submissionResult.value = Result.Success(Unit)
                    onSuccess()
                } else if (result is Result.Error) {
                    throw result.exception
                }
            } catch (e: Exception) {
                submissionResult.value = Result.Error(e)
                onError(e.message ?: "Investment submission failed")
            }

            _uiState.update { it.copy(isSubmitting = false) }
        }
    }
    fun submitInvestmentWithUser(
        currentUser: User,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            submissionResult.value = null

            try {
                val state = _uiState.value

                val investment = Investment(
                    investmentId = null,
                    provisionalId = state.provisionalId,
                    shareholderId = state.shareholderId ?: "",
                    investeeType = state.investeeType,
                    investeeName = state.investeeName,
                    ownershipType = state.ownershipType,
                    partnerNames = state.partnerNames,
                    investmentDate = state.investmentDate,
                    investmentType = state.investmentType,
                    investmentName = state.investmentName,
                    amount = state.amount,
                    expectedProfitPercent = state.expectedProfitPercent,
                    expectedProfitAmount = state.expectedProfitAmount,
                    expectedReturnPeriod = state.expectedReturnPeriod,
                    remarks = state.remarks,
                    approvalStatus = ApprovalStage.PENDING,
                    returnDueDate = state.investmentDate.plusDays(state.expectedReturnPeriod.toLong()),
                    createdAt = state.createdAt
                )

                val result = repository.createInvestment(investment)

                if (result is Result.Success) {
                    // ✅ Create ApprovalFlow entry
                    val approvalFlow = ApprovalFlow(
                        entityType = ApprovalType.INVESTMENT,
                        entityId = investment.provisionalId,
                        role = MemberRole.MEMBER,
                        action = ApprovalAction.APPROVE,
                        approvedBy = currentUser.shareholderId,
                        remarks = "Investment application submitted"
                    )
                    approvalFlowRepository.recordApproval(approvalFlow)

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

            _uiState.update { it.copy(isSubmitting = false) }
        }
    }



    // ---------------- FIRESTORE SYNC ----------------
    private fun syncToFirestore(investment: Investment) {
        val data = mapOf(
            "provisionalId" to investment.provisionalId,
            "createdAt" to investment.createdAt.toString(),   // ✅ include in Firestore
            "investeeType" to investment.investeeType.name,
            "investeeName" to investment.investeeName,
            "shareholderId" to investment.shareholderId,
            "ownershipType" to investment.ownershipType.name,
            "partnerNames" to investment.partnerNames,
            "investmentDate" to investment.investmentDate.toString(),
            "investmentType" to investment.investmentType.name,
            "investmentName" to investment.investmentName,
            "amount" to investment.amount,
            "expectedProfitPercent" to investment.expectedProfitPercent,
            "expectedProfitAmount" to investment.expectedProfitAmount,
            "expectedReturnPeriod" to investment.expectedReturnPeriod,
            "remarks" to investment.remarks,
            "approvalStatus" to investment.approvalStatus.name,
            "returnDueDate" to investment.returnDueDate.toString()
        )

        firestore.collection("investments")
            .document(investment.provisionalId)
            .set(data)
            .addOnSuccessListener { Timber.d("Investment synced: ${investment.provisionalId}") }
            .addOnFailureListener { e -> Timber.e(e, "Investment sync failed") }
    }
}
