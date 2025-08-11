package com.selfgrowthfund.sgf.ui.deposits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DepositViewModel : ViewModel() {

    // Form fields
    val dueMonth = MutableStateFlow("") // e.g., "Aug-2025"
    val paymentDate = MutableStateFlow("") // e.g., "11/08/2025"
    val shareNos = MutableStateFlow(1)
    val additionalContribution = MutableStateFlow(0.0)

    // Derived values
    private val _penalty = MutableStateFlow(0.0)
    val penalty: StateFlow<Double> = _penalty

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount

    private val _paymentStatus = MutableStateFlow("")
    val paymentStatus: StateFlow<String> = _paymentStatus

    // Constants
    private val shareAmount = 2000.0
    private val penaltyPerDay = 5.0

    fun updateCalculations() {
        val dm = dueMonth.value
        val pd = paymentDate.value
        val sn = shareNos.value
        val ac = additionalContribution.value

        val calculatedPenalty = calculatePenalty(dm, pd)
        val calculatedTotal = calculateTotalAmount(sn, ac, calculatedPenalty)
        val status = getPaymentStatus(dm, pd)

        _penalty.value = calculatedPenalty
        _totalAmount.value = calculatedTotal
        _paymentStatus.value = status
    }

    private fun calculatePenalty(dueMonth: String, paymentDate: String): Double {
        return try {
            if (DateUtils.isLatePayment(dueMonth, paymentDate)) {
                val daysLate = DateUtils.calculateDaysLate(dueMonth, paymentDate)
                daysLate * penaltyPerDay
            } else {
                0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }

    private fun calculateTotalAmount(
        shareNos: Int,
        additionalContribution: Double,
        penalty: Double
    ): Double {
        return (shareNos * shareAmount) + additionalContribution + penalty
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
            // TODO: Save deposit to Room via DepositRepository
        }
    }
}