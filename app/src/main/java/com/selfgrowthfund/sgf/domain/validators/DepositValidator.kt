package com.selfgrowthfund.sgf.domain.validators

import com.selfgrowthfund.sgf.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

class DepositValidator {

    companion object {
        fun validateShareNos(shareNos: Int): ValidationResult {
            return if (shareNos > 0) {
                ValidationResult.Success
            } else {
                ValidationResult.Error("Share quantity must be positive")
            }
        }

        fun validateAdditionalContribution(amount: Double): ValidationResult {
            return if (amount >= 0) {
                ValidationResult.Success
            } else {
                ValidationResult.Error("Additional contribution cannot be negative")
            }
        }

        fun validateDates(dueMonth: String, paymentDate: String): ValidationResult {
            return try {
                SimpleDateFormat("MMM-yyyy", Locale.US).parse(dueMonth)
                SimpleDateFormat("ddMMyyyy", Locale.US).parse(paymentDate)
                ValidationResult.Success
            } catch (e: Exception) {
                ValidationResult.Error("Invalid date format. Use DDMMYYYY for payment and MMM-YYYY for due month")
            }
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}