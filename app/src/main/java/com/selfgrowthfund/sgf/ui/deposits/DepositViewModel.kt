package com.selfgrowthfund.sgf.ui.deposits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dto.DepositEntrySummaryDTO
import com.selfgrowthfund.sgf.data.local.entities.DepositEntry
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import com.selfgrowthfund.sgf.data.repository.DepositRepository
import com.selfgrowthfund.sgf.model.enums.*
import com.selfgrowthfund.sgf.utils.Result
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import java.util.Locale

class DepositViewModel @AssistedInject constructor(
    private val depositRepository: DepositRepository,
    @Assisted("role") private val currentUserRole: MemberRole,
    @Assisted("shareholderId") private val selectedShareholderId: String,
    @Assisted("shareholderName") private val selectedShareholderName: String,
    @Assisted("lastDepositId") private val lastDepositId: String?
) : ViewModel() {

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
            val dueDate = LocalDate.parse("01-$dueMonth", dueMonthFormatter).withDayOfMonth(10)
            val paymentDate = LocalDate.parse(paymentDateStr, paymentDateFormatter)
            if (paymentDate.isAfter(dueDate)) "Late" else "On-time"
        } catch (_: Exception) {
            "Pending"
        }
    }

    // ---------------- SUBMIT ----------------
    fun submitDeposit() {
        viewModelScope.launch {
            isSubmitting.value = true
            submissionResult.value = null

            try {
                val parsedPaymentDate = LocalDate.parse(_paymentDate.value, paymentDateFormatter)

                val depositEntry = DepositEntry(
                    depositId = DepositEntry.generateNextId(lastDepositId),
                    shareholderId = selectedShareholderId,
                    shareholderName = selectedShareholderName,
                    dueMonth = DueMonth(_dueMonth.value),
                    paymentDate = parsedPaymentDate,
                    shareNos = _shareNos.value,
                    shareAmount = shareAmount,
                    additionalContribution = _additionalContribution.value,
                    penalty = _penalty.value,
                    totalAmount = _totalAmount.value,
                    paymentStatus = _paymentStatus.value,
                    modeOfPayment = _modeOfPayment.value,
                    status = DepositStatus.Pending,
                    approvedBy = null,
                    notes = null,
                    isSynced = false,
                    createdAt = Instant.now(),
                    entrySource = if (currentUserRole == MemberRole.MEMBER_ADMIN)
                        EntrySource.ADMIN else EntrySource.USER
                )

                depositRepository.insertDepositEntry(depositEntry)
                submissionResult.value = Result.Success(Unit)
            } catch (e: Exception) {
                submissionResult.value = Result.Error(e)
            }

            isSubmitting.value = false
        }
    }

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