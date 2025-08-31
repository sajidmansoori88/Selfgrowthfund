package com.selfgrowthfund.sgf.data.local.entities

import com.selfgrowthfund.sgf.model.enums.EntrySource
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.utils.IdGenerator
import java.time.LocalDate

data class InvestmentReturnEntry(
    val investment: Investment,
    val amountReceived: Double,
    val returnDate: LocalDate = LocalDate.now(),
    val modeOfPayment: PaymentMode,
    val remarks: String? = null,
    val entrySource: EntrySource = EntrySource.ADMIN,
    val enteredBy: String? = null
) {
    fun toInvestmentReturn(lastReturnId: String?): InvestmentReturns {
        val newId = IdGenerator.nextInvestmentReturnsId(lastReturnId)
        return InvestmentReturns(
            returnId = newId,
            investment = investment,
            amountReceived = amountReceived,
            modeOfPayment = modeOfPayment,
            returnDate = returnDate,
            remarks = remarks,
            entrySource = entrySource,
            enteredBy = enteredBy
        )
    }
}