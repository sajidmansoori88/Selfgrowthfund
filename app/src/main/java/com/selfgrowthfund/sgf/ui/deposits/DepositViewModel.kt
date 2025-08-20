package com.selfgrowthfund.sgf.ui.deposits

import androidx.lifecycle.ViewModel
import com.selfgrowthfund.sgf.data.local.dto.DepositEntrySummaryDTO
import com.selfgrowthfund.sgf.data.local.entities.DepositEntry
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import com.selfgrowthfund.sgf.data.repository.DepositRepository
import com.selfgrowthfund.sgf.model.enums.DepositStatus
import com.selfgrowthfund.sgf.model.enums.EntrySource
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.utils.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
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


    // ---------------- SUMMARY STATE ----------------
    val depositSummaries: StateFlow<List<DepositEntrySummaryDTO>> =
        depositRepository.getDepositEntrySummary()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------------- CONSTANTS ----------------
    private val shareAmount = 2000.0
    private val penaltyPerDay = 5.0

    // ---------------- ENTRY UPDATE METHODS ----------------
    fun setDueMonth(month: String) {
        _dueMonth.value = month
        updateCalculations() // Ensure this calls the full calculation
    }
    fun setPaymentDate(date: String) {
        _paymentDate.value = date
        updateCalculations() // Ensure this calls the full calculation
    }

    fun setShareNos(nos: Int) {
        _shareNos.value = nos.coerceAtLeast(0)
        updateTotal()
    }

    fun setAdditionalContribution(amount: Double) {
        _additionalContribution.value = amount
        updateCalculations()
    }

    fun updateCalculations() {
        val calculatedPenalty = calculatePenalty(_dueMonth.value, _paymentDate.value)
        val calculatedTotal = calculateTotalAmount(_shareNos.value, _additionalContribution.value, calculatedPenalty)
        val status = getPaymentStatus(_dueMonth.value, _paymentDate.value)

        _penalty.value = calculatedPenalty
        _totalAmount.value = calculatedTotal
        _paymentStatus.value = status
    }

    fun setModeOfPayment(mode: PaymentMode) {
        _modeOfPayment.value = mode
    }
    // ---------------- ENTRY HELPERS ----------------
    fun calculatePenalty(dueMonth: String, paymentDate: String): Double {
        if (paymentDate.isBlank()) return 0.0

        val monthFormat = SimpleDateFormat("MMM-yyyy", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        return try {
            val dueCal = Calendar.getInstance().apply {
                time = monthFormat.parse(dueMonth)!!
                set(Calendar.DAY_OF_MONTH, 10) // Grace ends on 10th
            }

            val paymentCal = Calendar.getInstance().apply {
                time = dateFormat.parse(paymentDate)!!
            }

            if (paymentCal.after(dueCal)) {
                val millisPerDay = 1000 * 60 * 60 * 24
                val daysLate = ((paymentCal.timeInMillis - dueCal.timeInMillis) / millisPerDay).toInt()
                (daysLate.coerceAtLeast(1)) * 5.0 // ₹5 per day
            } else {
                0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }

    fun updatePenalty() {
        val penalty = calculatePenalty(_dueMonth.value, _paymentDate.value)
        _penalty.value = penalty
    }

    fun updateTotal() {
        val shares = _shareNos.value
        val contribution = _additionalContribution.value
        val penalty = calculatePenalty(_dueMonth.value, _paymentDate.value) // ✅ Fresh calculation
        val total = calculateTotalAmount(shares, contribution, penalty)

        _penalty.value = penalty // ✅ Update penalty here
        _totalAmount.value = total
    }
    fun calculateTotalAmount(shares: Int, contribution: Double, penalty: Double): Double {
        val base = if (shares <= 0) 0 else shares * 2000
        return base + contribution + penalty
    }
    fun getPaymentStatus(dueMonth: String, paymentDate: String): String {
        if (dueMonth.isBlank()) return "Pending"

        val monthFormat = SimpleDateFormat("MMM-yyyy", Locale.getDefault())
        val now = Calendar.getInstance()

        return try {
            val dueCal = Calendar.getInstance().apply {
                time = monthFormat.parse(dueMonth)!!
                set(Calendar.DAY_OF_MONTH, 10) // Grace period ends on 10th
            }

            // If no payment date, check if current date is past due
            if (paymentDate.isBlank()) {
                return if (now.after(dueCal)) "Late" else "Pending"
            }

            // With payment date, use dateFormat
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val paymentCal = Calendar.getInstance().apply {
                time = dateFormat.parse(paymentDate)!!
            }

            if (paymentCal.after(dueCal)) "Late" else "On-time"
        } catch (e: Exception) {
            "Pending"
        }
    }

    // ---------------- ENTRY SUBMIT ----------------
    fun submitDeposit() {
        viewModelScope.launch {
            val parsedDate = try {
                DateUtils.formatterPaymentDate().parse(_paymentDate.value)
            } catch (e: Exception) {
                null
            }

            val formattedDate = parsedDate?.let {
                DateUtils.formatterPaymentDate().format(it)
            } ?: return@launch

            val depositEntry = DepositEntry(
                depositId = DepositEntry.generateNextId(lastDepositId),
                shareholderId = selectedShareholderId,
                shareholderName = selectedShareholderName,
                dueMonth = DueMonth(_dueMonth.value),
                paymentDate = formattedDate,
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
                createdAt = System.currentTimeMillis(),
                entrySource = if (currentUserRole == MemberRole.MEMBER_ADMIN)
                    EntrySource.ADMIN
                else
                    EntrySource.USER
            )

            depositRepository.insertDepositEntry(depositEntry)
        }

        fun updateStatus() {
            val dueMonthStr = _dueMonth.value
            val paymentDateStr = _paymentDate.value

            if (dueMonthStr.isBlank()) {
                _paymentStatus.value = "Pending"
                return
            }

            val monthFormat = SimpleDateFormat("MMM-yyyy", Locale.getDefault())
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

            try {
                val graceCal = Calendar.getInstance().apply {
                    time = monthFormat.parse(dueMonthStr)!!
                    set(Calendar.DAY_OF_MONTH, 10)
                }

                val now = Calendar.getInstance()

                if (paymentDateStr.isBlank()) {
                    // No payment yet
                    _paymentStatus.value = if (now.after(graceCal)) "Late" else "Pending"
                    return
                }

                val paymentCal = Calendar.getInstance().apply {
                    time = dateFormat.parse(paymentDateStr)!!
                }

                _paymentStatus.value = if (paymentCal.after(graceCal)) "Late" else "On-time"

            } catch (e: Exception) {
                _paymentStatus.value = "Pending"
            }
        }
    }
}