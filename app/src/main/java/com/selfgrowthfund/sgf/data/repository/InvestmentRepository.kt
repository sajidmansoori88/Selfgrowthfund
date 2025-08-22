package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.InvestmentDao
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class InvestmentRepository @Inject constructor(
    private val dao: InvestmentDao,
    private val dates: Dates
) {
    // ================ CRUD Operations ================
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

    // ================ Get Operations ================
    suspend fun getInvestment(id: String): Result<Investment> = try {
        Result.Success(dao.getById(id) ?: throw Exception("Investment not found"))
    } catch (e: Exception) {
        Result.Error(e)
    }

    fun getAllInvestments(): Flow<List<Investment>> = dao.getAll()

    fun getActiveInvestments(): Flow<List<Investment>> = dao.getByStatus("Active")

    fun getInvestmentsByType(type: String): Flow<List<Investment>> = dao.getByInvesteeType(type)

    // ================ Business Logic ================
    suspend fun changeInvestmentStatus(id: String, newStatus: String): Result<Unit> = try {
        val investment = dao.getById(id) ?: throw Exception("Investment not found")
        dao.update(investment.copy(status = newStatus))
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getInvestmentsDueSoon(daysThreshold: Int = 7): Result<List<Investment>> = try {
        val today = LocalDate.now()
        val thresholdDate = today.plusDays(daysThreshold.toLong())
        Result.Success(dao.getDueBetween(today, thresholdDate))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun searchInvestments(query: String): Result<List<Investment>> = try {
        Result.Success(dao.search(query)) // DAO already handles wildcards
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