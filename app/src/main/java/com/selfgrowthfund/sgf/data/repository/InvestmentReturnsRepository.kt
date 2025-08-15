// app/src/main/java/com/selfgrowthfund/sgf/data/repository/InvestmentReturnsRepository.kt
package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.InvestmentDao
import com.selfgrowthfund.sgf.data.local.dao.InvestmentReturnsDao
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.Result
import javax.inject.Inject

class InvestmentReturnsRepository @Inject constructor(
    private val returnsDao: InvestmentReturnsDao,
    private val investmentDao: InvestmentDao,
    private val dates: Dates
) {
    // ... other functions remain the same ...

    suspend fun addReturn(
        returnId: String,
        investmentId: String,
        amountReceived: Double,
        remarks: String? = null
    ): Result<Unit> = try {
        val investment = investmentDao.getInvestmentById(investmentId)
            ?: throw Exception("Investment not found")

        // Using dates.now() instead of currentDateTime()
        val returnDate = dates.now()

        returnsDao.insertReturn(
            InvestmentReturns(
                returnId = returnId,
                investment = investment,
                amountReceived = amountReceived,
                returnDate = returnDate,
                remarks = remarks
            )
        )
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}