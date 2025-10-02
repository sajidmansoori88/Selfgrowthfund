package com.selfgrowthfund.sgf.ui.deposits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dto.DepositEntrySummaryDTO
import com.selfgrowthfund.sgf.data.local.entities.Deposit
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import com.selfgrowthfund.sgf.data.repository.DepositRepository
import com.selfgrowthfund.sgf.model.enums.*
import com.selfgrowthfund.sgf.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
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

    // --- Nav args ---
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

    private val _paymentStatus = MutableStateFlow<PaymentStatus>(PaymentStatus.PENDING)
    val paymentStatus: StateFlow<PaymentStatus> = _paymentStatus

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
        depositRepository.getDepositEntrySummaries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveDepositSummaries: StateFlow<List<DepositEntrySummaryDTO>> =
        depositRepository.getLiveDepositSummaries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------------- CONSTANTS ----------------
    private val shareAmount = 2000.0
    private val penaltyPerDay = 5.0
    private val dueMonthFormatter = DateTimeFormatter.ofPattern("MMM-yyyy", Locale.US)
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
            LocalDate.parse("01-${_dueMonth.value}", DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
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
        val calculatedTotal =
            calculateTotalAmount(_shareNos.value, _additionalContribution.value, calculatedPenalty)
        val status = getPaymentStatus(_dueMonth.value, _paymentDate.value)

        _penalty.value = calculatedPenalty
        _totalAmount.value = calculatedTotal
        _paymentStatus.value = status
    }

    fun updateTotal() {
        val penalty = calculatePenalty(_dueMonth.value, _paymentDate.value)
        val total =
            calculateTotalAmount(_shareNos.value, _additionalContribution.value, penalty)
        _penalty.value = penalty
        _totalAmount.value = total
    }

    // ---------------- LOGIC HELPERS ----------------
    fun calculatePenalty(dueMonth: String, paymentDateStr: String): Double {
        return try {
            val dueDate =
                LocalDate.parse("01-$dueMonth", DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
                    .withDayOfMonth(10)
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

    fun getPaymentStatus(dueMonth: String, paymentDateStr: String): PaymentStatus {
        return try {
            val dueDateStart =
                LocalDate.parse("01-$dueMonth", DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
            val dueDateEnd = dueDateStart.withDayOfMonth(10)
            val paymentDate = LocalDate.parse(paymentDateStr, paymentDateFormatter)

            when {
                paymentDate.isBefore(dueDateStart) -> PaymentStatus.EARLY
                paymentDate.isAfter(dueDateEnd) -> PaymentStatus.LATE
                else -> PaymentStatus.ON_TIME
            }
        } catch (_: Exception) {
            PaymentStatus.PENDING
        }
    }

    // ---------------- SUBMIT ----------------
    fun submitDeposit(
        notes: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            isSubmitting.value = true
            submissionResult.value = null

            try {
                val parsedPaymentDate =
                    LocalDate.parse(_paymentDate.value, paymentDateFormatter)

                if (role == MemberRole.MEMBER_ADMIN) {
                    depositRepository.submitByTreasurer(
                        shareholderId,
                        shareholderName,
                        DueMonth(_dueMonth.value),
                        parsedPaymentDate,
                        _shareNos.value,
                        _additionalContribution.value,
                        _penalty.value,
                        _modeOfPayment.value ?: PaymentMode.CASH
                    )
                } else {
                    depositRepository.submitByShareholder(
                        shareholderId,
                        shareholderName,
                        DueMonth(_dueMonth.value),
                        parsedPaymentDate,
                        _shareNos.value,
                        _additionalContribution.value,
                        _penalty.value,
                        _modeOfPayment.value ?: PaymentMode.CASH
                    )
                }

                submissionResult.value = Result.Success(Unit)
                onSuccess()
            } catch (e: Exception) {
                submissionResult.value = Result.Error(e)
                onError(e.message ?: "Deposit submission failed")
            }

            isSubmitting.value = false
        }
    }

    // ---------------- Firestore → Room Refresh ----------------
    fun refreshFromFirestore(onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                depositRepository.refreshFromFirestore()
                onComplete(true)
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh deposits from Firestore")
                onComplete(false)
            }
        }
    }

    // ---------------- FORM RESET ----------------
    fun clearForm() {
        _dueMonth.value = ""
        _paymentDate.value = ""
        _shareNos.value = 0
        _additionalContribution.value = 0.0
        _penalty.value = 0.0
        _totalAmount.value = 0.0
        _paymentStatus.value = PaymentStatus.PENDING
        _modeOfPayment.value = null
        dueMonthError.value = null
        paymentDateError.value = null
        isFormValid.value = false
        submissionResult.value = null
    }
}
