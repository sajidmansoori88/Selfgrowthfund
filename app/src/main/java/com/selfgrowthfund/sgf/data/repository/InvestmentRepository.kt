package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.InvestmentDao
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class InvestmentRepository @Inject constructor(
    private val dao: InvestmentDao,
    private val dates: Dates
) {

    suspend fun createInvestment(investment: Investment): Result<Unit> = try {
        dao.insert(investment)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateInvestment(investment: Investment): Result<Unit> = try {
        dao.update(investment)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteInvestment(id: String): Result<Unit> = try {
        dao.delete(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getInvestment(id: String): Result<Investment> = try {
        Result.Success(dao.getById(id) ?: throw Exception("Investment not found"))
    } catch (e: Exception) {
        Result.Error(e)
    }

    fun getAllInvestments(): Flow<List<Investment>> = dao.getAll()
    fun getActiveInvestments(): Flow<List<Investment>> = dao.getByStatus("Active")
    fun getInvestmentsByType(type: String): Flow<List<Investment>> = dao.getByInvesteeType(type)

    suspend fun getInvestmentsDueSoon(daysThreshold: Int = 7): Result<List<Investment>> = try {
        val now = LocalDateTime.ofInstant(dates.now().toInstant(), ZoneId.systemDefault())
        val threshold = now.plusDays(daysThreshold.toLong())
        Result.Success(dao.getDueBetween(now, threshold))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun searchInvestments(query: String): Result<List<Investment>> = try {
        Result.Success(dao.search("%$query%"))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getTotalActiveInvestmentValue(): Result<Double> = try {
        Result.Success(dao.getTotalActiveAmount() ?: 0.0)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getActiveInvestmentCount(): Result<Int> = try {
        Result.Success(dao.getActiveCount())
    } catch (e: Exception) {
        Result.Error(e)
    }
}
