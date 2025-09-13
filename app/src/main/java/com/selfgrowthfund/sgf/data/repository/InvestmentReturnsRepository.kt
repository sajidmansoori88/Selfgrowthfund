package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.InvestmentDao
import com.selfgrowthfund.sgf.data.local.dao.InvestmentReturnsDao
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.Result
import java.time.LocalDate
import javax.inject.Inject

class InvestmentReturnsRepository @Inject constructor(
    private val returnsDao: InvestmentReturnsDao,
    private val investmentDao: InvestmentDao,
    private val dates: Dates
) {
    suspend fun addReturn(returnEntity: InvestmentReturns): Result<Unit> = try {
        returnsDao.insertReturn(returnEntity)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getReturnsForInvestment(investmentId: String): List<InvestmentReturns> {
        return returnsDao.getReturnsByInvestmentId(investmentId)
    }

    suspend fun getTotalProfitForInvestment(investmentId: String): Double {
        return returnsDao.getReturnsByInvestmentId(investmentId)
            .sumOf { it.actualProfitAmount }
    }

    suspend fun getTotalReturnedAmount(investmentId: String): Double {
        return returnsDao.getReturnsByInvestmentId(investmentId)
            .sumOf { it.amountReceived }
    }

    suspend fun countApproved(start: LocalDate, end: LocalDate) =
        returnsDao.countByStatus("APPROVED", start, end)

    suspend fun countRejected(start: LocalDate, end: LocalDate) =
        returnsDao.countByStatus("REJECTED", start, end)

    suspend fun countPending(start: LocalDate, end: LocalDate) =
        returnsDao.countByStatus("PENDING", start, end)

    suspend fun countTotal(start: LocalDate, end: LocalDate) =
        returnsDao.countTotal(start, end)
}