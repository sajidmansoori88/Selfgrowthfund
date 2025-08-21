package com.selfgrowthfund.sgf.domain.validators

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import java.util.Locale

class DepositValidator {

    companion object {
        private val dueMonthFormatter = DateTimeFormatter.ofPattern("MMM-yyyy", Locale.US)
        private val paymentDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US)

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
                LocalDate.parse("01-$dueMonth", DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.US))
                LocalDate.parse(paymentDate, paymentDateFormatter)
                ValidationResult.Success
            } catch (e: DateTimeParseException) {
                ValidationResult.Error("Invalid date format. Use DD-MM-YYYY for payment and MMM-YYYY for due month")
            }
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}