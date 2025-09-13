package com.selfgrowthfund.sgf.ui.deposits

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.selfgrowthfund.sgf.data.local.dto.DepositEntrySummaryDTO
import com.selfgrowthfund.sgf.data.local.entities.DepositEntry
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import com.selfgrowthfund.sgf.data.repository.DepositRepository
import com.selfgrowthfund.sgf.model.enums.*
import com.selfgrowthfund.sgf.utils.IdGenerator
import com.selfgrowthfund.sgf.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DepositViewModel @Inject constructor(
    private val depositRepository: DepositRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Extract navigation arguments from SavedStateHandle
    val shareholderId: String = savedStateHandle["shareholderId"] ?: ""
    val shareholderName: String = savedStateHandle["shareholderName"] ?: ""
    val role: MemberRole = (savedStateHandle["role"] as? String)?.let {
        MemberRole.fromLabel(it)
    } ?: MemberRole.MEMBER
    val lastDepositId: String? = savedStateHandle["lastDepositId"]



    // ---------------- ENTRY STATE ----------------
    private val _dueMonth = MutableStateFlow("")
    val dueMonth: StateFlow<String> = _dueMonth

    private val _paymentDate = MutableStateFlow("")
    val paymentDate: StateFlow<String> = _paymentDate

    private val _shareNos = MutableStateFlow(0)
    val shareNos: StateFlow<Int> = _shareNos

    private val _additionalContribution = MutableStateFlow(0.0)
    val additionalContribution: StateFlow<Double> = _additionalContribution

    private val _penalty = MutableStateFlow(0.0)
    val penalty: StateFlow<Double> = _penalty

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount

    private val _paymentStatus = MutableStateFlow("")
    val paymentStatus: StateFlow<String> = _paymentStatus

    private val _modeOfPayment = MutableStateFlow<PaymentMode?>(null)
    val modeOfPayment: StateFlow<PaymentMode?> = _modeOfPayment

    // ---------------- VALIDATION STATE ----------------
    val dueMonthError = MutableStateFlow<String?>(null)
    val paymentDateError = MutableStateFlow<String?>(null)
    val isFormValid = MutableStateFlow(false)

    // ---------------- SUBMISSION STATE ----------------
    val isSubmitting = MutableStateFlow(false)
    val submissionResult = MutableStateFlow<Result<Unit>?>(null)

    // ---------------- SUMMARY STATE ----------------
    val depositSummaries: StateFlow<List<DepositEntrySummaryDTO>> =
        depositRepository.getDepositEntrySummary()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveDepositSummaries: StateFlow<List<DepositEntrySummaryDTO>> =
        getLiveDepositSummaries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------------- CONSTANTS ----------------
    private val shareAmount = 2000.0
    private val penaltyPerDay = 5.0
    private val dueMonthFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.US)
    private val paymentDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US)

    // ---------------- ENTRY UPDATE METHODS ----------------
    fun setDueMonth(month: String) {
        _dueMonth.value = month
        validateForm()
        updateCalculations()
    }

    fun setPaymentDate(date: String) {
        _paymentDate.value = date
        validateForm()
        updateCalculations()
    }

    fun setShareNos(nos: Int) {
        _shareNos.value = nos.coerceAtLeast(0)
        updateTotal()
    }

    fun setAdditionalContribution(amount: Double) {
        _additionalContribution.value = amount.coerceAtLeast(0.0)
        updateCalculations()
    }

    fun setModeOfPayment(mode: PaymentMode) {
        _modeOfPayment.value = mode
    }

    // ---------------- VALIDATION ----------------
    private fun validateForm() {
        dueMonthError.value = try {
            LocalDate.parse("01-${_dueMonth.value}", dueMonthFormatter)
            null
        } catch (e: DateTimeParseException) {
            "Invalid due month format"
        }

        paymentDateError.value = try {
            LocalDate.parse(_paymentDate.value, paymentDateFormatter)
            null
        } catch (e: DateTimeParseException) {
            "Invalid payment date format"
        }

        isFormValid.value = dueMonthError.value == null && paymentDateError.value == null
    }

    // ---------------- CALCULATIONS ----------------
    fun updateCalculations() {
        val calculatedPenalty = calculatePenalty(_dueMonth.value, _paymentDate.value)
        val calculatedTotal = calculateTotalAmount(_shareNos.value, _additionalContribution.value, calculatedPenalty)
        val status = getPaymentStatus(_dueMonth.value, _paymentDate.value)

        _penalty.value = calculatedPenalty
        _totalAmount.value = calculatedTotal
        _paymentStatus.value = status
    }

    fun updateTotal() {
        val penalty = calculatePenalty(_dueMonth.value, _paymentDate.value)
        val total = calculateTotalAmount(_shareNos.value, _additionalContribution.value, penalty)
        _penalty.value = penalty
        _totalAmount.value = total
    }

    // ---------------- LOGIC HELPERS ----------------
    fun calculatePenalty(dueMonth: String, paymentDateStr: String): Double {
        return try {
            val dueDate = LocalDate.parse("01-$dueMonth", dueMonthFormatter).withDayOfMonth(10)
            val paymentDate = LocalDate.parse(paymentDateStr, paymentDateFormatter)
            val daysLate = (paymentDate.toEpochDay() - dueDate.toEpochDay()).toInt()
            if (daysLate > 0) daysLate * penaltyPerDay else 0.0
        } catch (_: Exception) {
            0.0
        }
    }

    fun calculateTotalAmount(shares: Int, contribution: Double, penalty: Double): Double {
        val base = if (shares <= 0) 0.0 else shares * shareAmount
        return base + contribution + penalty
    }

    fun getPaymentStatus(dueMonth: String, paymentDateStr: String): String {
        return try {
            val dueDateStart = LocalDate.parse("01-$dueMonth", dueMonthFormatter)
            val dueDateEnd = dueDateStart.withDayOfMonth(10)
            val paymentDate = LocalDate.parse(paymentDateStr, paymentDateFormatter)

            when {
                paymentDate.isBefore(dueDateStart) -> "Early"
                paymentDate.isAfter(dueDateEnd) -> "Late"
                else -> "On-time"
            }
        } catch (_: Exception) {
            "Pending"
        }
    }


    fun submitDeposit(
        notes: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            isSubmitting.value = true
            submissionResult.value = null

            try {
                val parsedPaymentDate = LocalDate.parse(_paymentDate.value, paymentDateFormatter)
                val newDepositId = IdGenerator.nextDepositId(lastDepositId)

                val depositEntry = DepositEntry(
                    depositId = newDepositId,
                    shareholderId = shareholderId,
                    shareholderName = shareholderName,
                    dueMonth = DueMonth(_dueMonth.value),
                    paymentDate = parsedPaymentDate,
                    shareNos = _shareNos.value,
                    shareAmount = shareAmount,
                    additionalContribution = _additionalContribution.value,
                    penalty = _penalty.value,
                    totalAmount = _totalAmount.value,
                    paymentStatus = _paymentStatus.value,
                    modeOfPayment = _modeOfPayment.value,
                    status = if (role == MemberRole.MEMBER_ADMIN)
                        DepositStatus.Approved else DepositStatus.Pending,
                    approvedBy = if (role == MemberRole.MEMBER_ADMIN)
                        shareholderName else null,
                    notes = notes.orEmpty(),
                    isSynced = true,
                    createdAt = Instant.now(),
                    entrySource = if (role == MemberRole.MEMBER_ADMIN)
                        EntrySource.ADMIN else EntrySource.USER
                )

                depositRepository.insertDepositEntry(depositEntry)
                syncDepositToFirestore(depositEntry)

                submissionResult.value = Result.Success(Unit)
                onSuccess()
            } catch (e: Exception) {
                submissionResult.value = Result.Error(e)
                onError(e.message ?: "Deposit submission failed")
            }

            isSubmitting.value = false
        }
    }

    private fun syncDepositToFirestore(deposit: DepositEntry) {
        val db = Firebase.firestore
        val isoDate = DateTimeFormatter.ISO_DATE

        val firestoreData = mapOf(
            "depositId" to deposit.depositId,
            "shareholderId" to deposit.shareholderId,
            "shareholderName" to deposit.shareholderName,
            "dueMonth" to deposit.dueMonth.value,
            "paymentDate" to deposit.paymentDate.format(isoDate),
            "shareNos" to deposit.shareNos,
            "shareAmount" to deposit.shareAmount,
            "additionalContribution" to deposit.additionalContribution,
            "penalty" to deposit.penalty,
            "totalAmount" to deposit.totalAmount,
            "paymentStatus" to deposit.paymentStatus,
            "modeOfPayment" to deposit.modeOfPayment?.name,
            "status" to deposit.status.name,
            "approvedBy" to deposit.approvedBy,
            "notes" to deposit.notes,
            "isSynced" to true,
            "createdAt" to deposit.createdAt.toString(),
            "entrySource" to deposit.entrySource.name
        )

        db.collection("deposits").document(deposit.depositId)
            .set(firestoreData)
            .addOnSuccessListener {
                Log.d("Firestore", "Deposit synced: ${deposit.depositId}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Deposit sync failed", e)
            }
    }

    // ---------------- FIRESTORE LISTENER ----------------
    private fun getLiveDepositSummaries(): Flow<List<DepositEntrySummaryDTO>> = callbackFlow {
        val db = Firebase.firestore
        val listener = db.collection("deposits")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val summaries = snapshot.documents.mapNotNull { doc ->
                    try {
                        DepositEntrySummaryDTO(
                            depositId = doc.getString("depositId") ?: return@mapNotNull null,
                            shareholderId = doc.getString("shareholderId") ?: "Unknown",
                            shareholderName = doc.getString("shareholderName") ?: "Unknown",
                            shareNos = doc.getLong("shareNos")?.toInt() ?: 0,
                            shareAmount = doc.getDouble("shareAmount") ?: 0.0,
                            additionalContribution = doc.getDouble("additionalContribution") ?: 0.0,
                            penalty = doc.getDouble("penalty") ?: 0.0,
                            totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                            paymentStatus = doc.getString("paymentStatus") ?: "Pending",
                            modeOfPayment = doc.getString("modeOfPayment") ?: "Unknown",
                            dueMonth = doc.getString("dueMonth") ?: "Unknown",
                            paymentDate = doc.getString("paymentDate")?.let {
                                try {
                                    LocalDate.parse(it)
                                } catch (e: Exception) {
                                    null
                                }
                            } ?: LocalDate.now(),
                            createdAt = doc.getString("createdAt")?.let {
                                try {
                                    Instant.parse(it)
                                } catch (e: Exception) {
                                    null
                                }
                            } ?: Instant.now()
                        )

                    } catch (e: Exception) {
                        null
                    }
                }

                trySend(summaries)
            }

        awaitClose { listener.remove() }
    }

    // ---------------- FORM RESET ----------------
    fun clearForm() {
        _dueMonth.value = ""
        _paymentDate.value = ""
        _shareNos.value = 0
        _additionalContribution.value = 0.0
        _penalty.value = 0.0
        _totalAmount.value = 0.0
        _paymentStatus.value = ""
        _modeOfPayment.value = null
        dueMonthError.value = null
        paymentDateError.value = null
        isFormValid.value = false
        submissionResult.value = null
    }
}