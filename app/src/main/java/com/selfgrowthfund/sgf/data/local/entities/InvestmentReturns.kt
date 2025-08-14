package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

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
@TypeConverters(AppTypeConverters::class)
data class InvestmentReturns(
    @PrimaryKey
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
    val returnDate: LocalDateTime,
    val remarks: String? = null
    val createdAt: Date = Date()
) {
    constructor(
        returnId: String,
        investment: Investment,
        amountReceived: Double,
        returnDate: LocalDateTime = LocalDateTime.now(),
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
        private fun calculateDaysBetween(startDate: LocalDateTime, endDate: LocalDateTime): Int =
            ChronoUnit.DAYS.between(startDate, endDate).toInt()

        private fun calculateActualProfitPercent(amountInvested: Double, amountReceived: Double): Double =
            ((amountReceived - amountInvested) / amountInvested) * 100
    }
}
