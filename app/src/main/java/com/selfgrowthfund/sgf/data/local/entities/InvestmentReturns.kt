package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.selfgrowthfund.sgf.utils.Dates
import java.util.*

@Entity(
    tableName = "investment_returns",
    foreignKeys = [
        ForeignKey(
            entity = Investment::class,
            parentColumns = ["investmentId"],
            childColumns = ["investmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("investmentId")]
)
data class InvestmentReturns(
    val returnId: String,
    val investmentId: String,
    val investmentName: String,
    val expectedReturnPeriod: Int,
    val actualReturnPeriod: Int,
    val amountInvested: Double,
    val amountReceived: Double,
    val expectedProfitPercent: Double,
    val actualProfitPercent: Double,
    val expectedProfitAmount: Double,
    val actualProfitAmount: Double,
    val profitPercentVariance: Double,
    val profitAmountVariance: Double,
    val returnDate: Date,
    val remarks: String? = null
) {
    // Secondary constructor with inline calculations
    constructor(
        returnId: String,
        investment: Investment,
        amountReceived: Double,
        returnDate: Date = Dates.now(),
        remarks: String? = null
    ) : this(
        returnId = returnId,
        investmentId = investment.investmentId,
        investmentName = investment.investmentName,
        expectedReturnPeriod = investment.expectedReturnPeriod,
        actualReturnPeriod = calculateDaysBetween(investment.investmentDate, returnDate),
        amountInvested = investment.amount,
        amountReceived = amountReceived,
        expectedProfitPercent = investment.expectedProfitPercent,
        actualProfitPercent = calculateActualProfitPercent(investment.amount, amountReceived),
        expectedProfitAmount = investment.expectedProfitAmount,
        actualProfitAmount = amountReceived - investment.amount,
        profitPercentVariance = calculateActualProfitPercent(investment.amount, amountReceived) - investment.expectedProfitPercent,
        profitAmountVariance = (amountReceived - investment.amount) - investment.expectedProfitAmount,
        returnDate = returnDate,
        remarks = remarks
    )

    companion object {
        // Static calculation methods
        private fun calculateDaysBetween(startDate: Date, endDate: Date): Int {
            val diff = endDate.time - startDate.time
            return (diff / (1000 * 60 * 60 * 24)).toInt()
        }

        private fun calculateActualProfitPercent(amountInvested: Double, amountReceived: Double): Double {
            return ((amountReceived - amountInvested) / amountInvested) * 100
        }
    }
}