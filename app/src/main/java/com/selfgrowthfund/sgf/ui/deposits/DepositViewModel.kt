package com.selfgrowthfund.sgf.ui.deposits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DepositViewModel @AssistedInject constructor(
    private val depositRepository: DepositRepository,
    @Assisted("role") private val currentUserRole: MemberRole,
    @Assisted("shareholderId") private val selectedShareholderId: String,
    @Assisted("shareholderName") private val selectedShareholderName: String,
    @Assisted("lastDepositId") private val lastDepositId: String?
) : ViewModel() {

    val dueMonth = MutableStateFlow("")
    val paymentDate = MutableStateFlow("")
    val shareNos = MutableStateFlow(1)
    val additionalContribution = MutableStateFlow(0.0)

    private val _penalty = MutableStateFlow(0.0)
    val penalty: StateFlow<Double> = _penalty

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount

    private val _paymentStatus = MutableStateFlow("")
    val paymentStatus: StateFlow<String> = _paymentStatus

    private val shareAmount = 2000.0
    private val penaltyPerDay = 5.0

    fun updateCalculations() {
        val calculatedPenalty = calculatePenalty(dueMonth.value, paymentDate.value)
        val calculatedTotal = calculateTotalAmount(shareNos.value, additionalContribution.value, calculatedPenalty)
        val status = getPaymentStatus(dueMonth.value, paymentDate.value)

        _penalty.value = calculatedPenalty
        _totalAmount.value = calculatedTotal
        _paymentStatus.value = status
    }

    private fun calculatePenalty(dueMonth: String, paymentDate: String): Double {
        return try {
            if (DateUtils.isLatePayment(dueMonth, paymentDate)) {
                val daysLate = DateUtils.calculateDaysLate(dueMonth, paymentDate)
                daysLate * penaltyPerDay
            } else 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    private fun calculateTotalAmount(sn: Int, ac: Double, penalty: Double): Double {
        return (sn * shareAmount) + ac + penalty
    }

    private fun getPaymentStatus(dueMonth: String, paymentDate: String): String {
        return try {
            if (DateUtils.isLatePayment(dueMonth, paymentDate)) "Late" else "On-time"
        } catch (e: Exception) {
            "On-time"
        }
    }

    fun submitDeposit() {
        viewModelScope.launch {
            val parsedDate = try {
                DateUtils.formatterPaymentDate().parse(paymentDate.value)
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
                dueMonth = DueMonth(dueMonth.value),
                paymentDate = formattedDate,
                shareNos = shareNos.value,
                shareAmount = shareAmount,
                additionalContribution = additionalContribution.value,
                penalty = penalty.value,
                totalAmount = totalAmount.value,
                paymentStatus = paymentStatus.value,
                modeOfPayment = "Cash",
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
    }
}